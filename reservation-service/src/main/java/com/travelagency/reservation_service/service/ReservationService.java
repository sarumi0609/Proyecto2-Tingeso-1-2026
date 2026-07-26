package com.travelagency.reservation_service.service;

import com.travelagency.reservation_service.entity.PassengerEntity;
import com.travelagency.reservation_service.entity.ReservationEntity;
import com.travelagency.reservation_service.model.DiscountConfigModel;
import com.travelagency.reservation_service.model.PromotionModel;
import com.travelagency.reservation_service.model.TouristPackageModel;
import com.travelagency.reservation_service.repository.PassengerRepository;
import com.travelagency.reservation_service.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    // RestTemplate con @LoadBalanced — resuelve "package-service" via Eureka
    @Autowired
    private RestTemplate restTemplate;

    // URL base del package-service (nombre de servicio Eureka)
    private static final String PACKAGE_SERVICE = "http://package-service";

    // Tope máximo de descuento acumulado (30%)
    private static final double MAX_DISCOUNT = 0.30;

    // ---------------------------------------------------------------
    // Métodos privados de comunicación con package-service
    // ---------------------------------------------------------------

    /**
     * Obtiene un paquete turístico por ID desde package-service.
     * Lanza RuntimeException si el paquete no existe.
     */
    private TouristPackageModel getPackageById(Long packageId) {
        TouristPackageModel pkg = restTemplate.getForObject(
                PACKAGE_SERVICE + "/api/touristpackages/" + packageId,
                TouristPackageModel.class);
        if (pkg == null) {
            throw new RuntimeException("Package not found: " + packageId);
        }
        return pkg;
    }

    /**
     * Obtiene la lista de descuentos activos desde package-service.
     */
    private List<DiscountConfigModel> getActiveDiscounts() {
        DiscountConfigModel[] arr = restTemplate.getForObject(
                PACKAGE_SERVICE + "/api/discounts/active",
                DiscountConfigModel[].class);
        return arr != null ? Arrays.asList(arr) : new ArrayList<>();
    }

    /**
     * Obtiene las promociones vigentes hoy desde package-service.
     */
    private List<PromotionModel> getActivePromotions() {
        PromotionModel[] arr = restTemplate.getForObject(
                PACKAGE_SERVICE + "/api/promotions/active",
                PromotionModel[].class);
        return arr != null ? Arrays.asList(arr) : new ArrayList<>();
    }

    /**
     * Actualiza los cupos disponibles y el estado de un paquete en package-service.
     * Usa PATCH /api/touristpackages/{id}/spots?availableSpots=N&status=S
     */
    private void updatePackageSpots(Long packageId, int availableSpots, String status) {
        String url = PACKAGE_SERVICE + "/api/touristpackages/" + packageId
                + "/spots?availableSpots=" + availableSpots + "&status=" + status;
        restTemplate.exchange(url, HttpMethod.PATCH, null, TouristPackageModel.class);
    }

    // ---------------------------------------------------------------
    // Lógica de negocio
    // ---------------------------------------------------------------

    /**
     * Crea una nueva reserva aplicando todas las reglas de negocio:
     * - Valida que el paquete esté disponible y con cupos suficientes
     * - Obtiene descuentos activos desde package-service y los aplica
     * - Obtiene promociones vigentes desde package-service y las aplica
     * - Descuenta los cupos del paquete vía PATCH a package-service
     */
    public ReservationEntity createReservation(String keycloakUserId, Long packageId,
                                               Integer passengers,
                                               List<PassengerEntity> passengerList) {

        // Obtener el paquete desde package-service
        TouristPackageModel pkg = getPackageById(packageId);

        // Validar disponibilidad
        if (!"AVAILABLE".equals(pkg.getStatus())) {
            throw new RuntimeException("Package is not available for reservations");
        }
        if (passengers <= 0) {
            throw new RuntimeException("Number of passengers must be greater than zero");
        }
        if (pkg.getAvailableSpots() < passengers) {
            throw new RuntimeException("Not enough spots available");
        }

        // Monto base
        double baseAmount = pkg.getPrice() * passengers;

        // Datos del usuario para evaluar condiciones de descuento
        Integer confirmedReservations = reservationRepository
                .countByKeycloakUserIdAndStatus(keycloakUserId, "CONFIRMED");

        // ---------------------------------------------------------------
        // Paso 1: Descuentos desde DiscountConfig (vía package-service)
        // ---------------------------------------------------------------
        List<DiscountConfigModel> activeDiscounts = getActiveDiscounts();

        List<DiscountConfigModel> cumulativeDiscounts = new ArrayList<>();
        List<DiscountConfigModel> exclusiveDiscounts = new ArrayList<>();

        for (DiscountConfigModel discount : activeDiscounts) {
            boolean applies = false;
            switch (discount.getType()) {
                case "GROUP":
                    applies = passengers >= discount.getThreshold();
                    break;
                case "FREQUENT_CLIENT":
                    applies = confirmedReservations >= discount.getThreshold();
                    break;
                case "MULTI_PACKAGE":
                    if (discount.getPeriodInHours() != null) {
                        LocalDateTime from = LocalDateTime.now()
                                .minusHours(discount.getPeriodInHours());
                        Integer pendingInPeriod = reservationRepository
                                .countByKeycloakUserIdAndStatusAndCreatedAtGreaterThanEqual(
                                        keycloakUserId, "PENDING", from);
                        applies = pendingInPeriod >= discount.getThreshold();
                    } else {
                        Integer pending = reservationRepository
                                .countByKeycloakUserIdAndStatus(keycloakUserId, "PENDING");
                        applies = pending >= discount.getThreshold();
                    }
                    break;
            }
            if (applies) {
                if (discount.getCumulative()) {
                    cumulativeDiscounts.add(discount);
                } else {
                    exclusiveDiscounts.add(discount);
                }
            }
        }

        double cumulativePercentage = Math.min(
                cumulativeDiscounts.stream()
                        .mapToDouble(DiscountConfigModel::getPercentage).sum(),
                MAX_DISCOUNT);

        double exclusivePercentage = exclusiveDiscounts.stream()
                .mapToDouble(DiscountConfigModel::getPercentage)
                .max().orElse(0.0);

        // ---------------------------------------------------------------
        // Paso 2: Promociones vigentes (vía package-service)
        // Solo aplican globales (touristPackage == null) o del paquete reservado
        // ---------------------------------------------------------------
        List<PromotionModel> activePromotions = getActivePromotions().stream()
                .filter(p -> p.getTouristPackage() == null
                        || p.getTouristPackage().getId().equals(packageId))
                .collect(Collectors.toList());

        List<PromotionModel> appliedPromotions = new ArrayList<>();
        double promotionDiscountPercentage = 0.0;

        List<PromotionModel> cumulativePromotions = new ArrayList<>();
        PromotionModel highestExclusivePromotion = null;

        for (PromotionModel promotion : activePromotions) {
            if (promotion.getCumulative()) {
                cumulativePromotions.add(promotion);
            } else {
                if (highestExclusivePromotion == null) {
                    highestExclusivePromotion = promotion;
                } else if ("DISCOUNT".equals(promotion.getBenefitType())
                        && promotion.getPercentage() != null
                        && (!"DISCOUNT".equals(highestExclusivePromotion.getBenefitType())
                        || promotion.getPercentage() > highestExclusivePromotion.getPercentage())) {
                    highestExclusivePromotion = promotion;
                }
            }
        }

        double maxSingleCumulativePromo = 0.0;
        for (PromotionModel promotion : cumulativePromotions) {
            appliedPromotions.add(promotion);
            if ("DISCOUNT".equals(promotion.getBenefitType()) && promotion.getPercentage() != null) {
                // Promoción guarda porcentaje como entero (40 = 40%) → convertir a decimal
                double promoDecimal = promotion.getPercentage() / 100.0;
                cumulativePercentage = Math.min(cumulativePercentage + promoDecimal, MAX_DISCOUNT);
                promotionDiscountPercentage += promoDecimal;
                if (promoDecimal > maxSingleCumulativePromo) {
                    maxSingleCumulativePromo = promoDecimal;
                }
            }
        }

        // Si una promo acumulable sola supera el tope, se respeta su valor completo
        if (maxSingleCumulativePromo > cumulativePercentage) {
            cumulativePercentage = maxSingleCumulativePromo;
        }

        if (highestExclusivePromotion != null) {
            appliedPromotions.add(highestExclusivePromotion);
            if ("DISCOUNT".equals(highestExclusivePromotion.getBenefitType())
                    && highestExclusivePromotion.getPercentage() != null) {
                double highestPromoDecimal = highestExclusivePromotion.getPercentage() / 100.0;
                exclusivePercentage = Math.max(exclusivePercentage, highestPromoDecimal);
                promotionDiscountPercentage += highestPromoDecimal;
            }
        }

        // ---------------------------------------------------------------
        // Paso 3: Calcular monto final
        // El bloque excluyente compite con el acumulable; gana el mayor
        // ---------------------------------------------------------------
        double finalDiscountPercentage = Math.max(cumulativePercentage, exclusivePercentage);
        double discountAmount = baseAmount * finalDiscountPercentage;
        double totalAmount = Math.max(baseAmount - discountAmount, 0);

        // ---------------------------------------------------------------
        // Paso 4: Detalle de descuentos para la boleta
        // ---------------------------------------------------------------
        StringBuilder discountDetails = new StringBuilder();
        boolean exclusiveWon = exclusivePercentage > cumulativePercentage;

        if (!exclusiveWon) {
            for (DiscountConfigModel d : cumulativeDiscounts) {
                discountDetails.append(d.getType())
                        .append(" discount: ").append((int)(d.getPercentage() * 100))
                        .append("% (cumulative). ");
            }
            if (cumulativePercentage >= MAX_DISCOUNT) {
                discountDetails.append("(Maximum cumulative discount limit applied: ")
                        .append((int)(MAX_DISCOUNT * 100)).append("%). ");
            }
        } else {
            exclusiveDiscounts.stream()
                    .max((a, b) -> Double.compare(a.getPercentage(), b.getPercentage()))
                    .ifPresent(d -> discountDetails.append(d.getType())
                            .append(" discount: ").append((int)(d.getPercentage() * 100))
                            .append("% (exclusive). "));
        }

        // ---------------------------------------------------------------
        // Paso 5: Porcentajes individuales para transparencia en la boleta
        // ---------------------------------------------------------------
        double groupDiscountPct = cumulativeDiscounts.stream()
                .filter(d -> "GROUP".equals(d.getType()))
                .mapToDouble(DiscountConfigModel::getPercentage).sum()
                + exclusiveDiscounts.stream()
                .filter(d -> "GROUP".equals(d.getType()))
                .mapToDouble(DiscountConfigModel::getPercentage).max().orElse(0.0);

        double frequentDiscountPct = cumulativeDiscounts.stream()
                .filter(d -> "FREQUENT_CLIENT".equals(d.getType()))
                .mapToDouble(DiscountConfigModel::getPercentage).sum()
                + exclusiveDiscounts.stream()
                .filter(d -> "FREQUENT_CLIENT".equals(d.getType()))
                .mapToDouble(DiscountConfigModel::getPercentage).max().orElse(0.0);

        double multiDiscountPct = cumulativeDiscounts.stream()
                .filter(d -> "MULTI_PACKAGE".equals(d.getType()))
                .mapToDouble(DiscountConfigModel::getPercentage).sum()
                + exclusiveDiscounts.stream()
                .filter(d -> "MULTI_PACKAGE".equals(d.getType()))
                .mapToDouble(DiscountConfigModel::getPercentage).max().orElse(0.0);

        // Armar el texto de promociones aplicadas
        StringBuilder promotionsText = new StringBuilder();
        for (PromotionModel p : appliedPromotions) {
            promotionsText.append("Promoción '").append(p.getName()).append("': ")
                    .append(p.getBenefitDescription());
            if ("DISCOUNT".equals(p.getBenefitType()) && p.getPercentage() != null) {
                promotionsText.append(" (").append(p.getPercentage().intValue()).append("% off)");
            }
            promotionsText.append(". ");
            discountDetails.append(promotionsText);
            promotionsText.setLength(0); // reusar el buffer
        }

        // ---------------------------------------------------------------
        // Paso 6: Descontar cupos en package-service vía HTTP PATCH
        // ---------------------------------------------------------------
        int newSpots = pkg.getAvailableSpots() - passengers;
        String newStatus = (newSpots == 0) ? "SOLD_OUT" : pkg.getStatus();
        updatePackageSpots(packageId, newSpots, newStatus);

        // ---------------------------------------------------------------
        // Paso 7: Crear y guardar la reserva
        // ---------------------------------------------------------------
        ReservationEntity reservation = new ReservationEntity();
        reservation.setKeycloakUserId(keycloakUserId);
        reservation.setPackageId(packageId);           // solo guardamos el ID
        reservation.setPassengers(passengers);
        reservation.setBaseAmount(baseAmount);
        reservation.setDiscountAmount(discountAmount);
        reservation.setTotalAmount(totalAmount);
        reservation.setGroupDiscountPercentage(groupDiscountPct);
        reservation.setFrequentClientDiscountPercentage(frequentDiscountPct);
        reservation.setMultiPackageDiscountPercentage(multiDiscountPct);
        reservation.setPromotionDiscountPercentage(promotionDiscountPercentage);
        reservation.setDiscountDetails(discountDetails.toString());
        // Guardamos los nombres de las promociones aplicadas como texto
        reservation.setAppliedPromotionsDetails(
                appliedPromotions.stream().map(PromotionModel::getName)
                        .collect(Collectors.joining(", ")));
        reservation.setStatus("PENDING");
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setExpiresAt(LocalDateTime.now().plusHours(24));

        ReservationEntity saved = reservationRepository.save(reservation);

        // Asociar y guardar cada pasajero
        for (PassengerEntity passenger : passengerList) {
            passenger.setReservation(saved);
            passengerRepository.save(passenger);
        }

        return saved;
    }

    // Retorna todas las reservas — para administradores
    public List<ReservationEntity> getAllReservations() {
        return reservationRepository.findAll();
    }

    // Retorna todas las reservas de un usuario específico
    public List<ReservationEntity> getReservationsByUser(String keycloakUserId) {
        return reservationRepository.findByKeycloakUserId(keycloakUserId);
    }

    // Retorna una reserva específica por su id
    public ReservationEntity getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
    }

    // Cancela una reserva y libera los cupos del paquete en package-service
    public ReservationEntity cancelReservation(Long id) {
        ReservationEntity reservation = getReservationById(id);
        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new RuntimeException("Reservation is already cancelled");
        }

        // Obtener el paquete actual para saber los cupos y estado vigentes
        TouristPackageModel pkg = getPackageById(reservation.getPackageId());
        int newSpots = pkg.getAvailableSpots() + reservation.getPassengers();
        String newStatus = "SOLD_OUT".equals(pkg.getStatus()) ? "AVAILABLE" : pkg.getStatus();
        updatePackageSpots(reservation.getPackageId(), newSpots, newStatus);

        reservation.setStatus("CANCELLED");
        return reservationRepository.save(reservation);
    }

    // Confirma una reserva tras pago exitoso (llamado por payment-service)
    public ReservationEntity confirmReservation(Long id) {
        ReservationEntity reservation = getReservationById(id);
        if (!"PENDING".equals(reservation.getStatus())) {
            throw new RuntimeException("Only pending reservations can be confirmed");
        }
        reservation.setStatus("CONFIRMED");
        return reservationRepository.save(reservation);
    }

    // Retorna el comprobante — solo disponible para reservas confirmadas
    public ReservationEntity getReservationReceipt(Long id) {
        ReservationEntity reservation = getReservationById(id);
        if (!"CONFIRMED".equals(reservation.getStatus())) {
            throw new RuntimeException("Receipt is only available for confirmed reservations");
        }
        return reservation;
    }

    // Expira reservas PENDING vencidas y libera sus cupos en package-service.
    // Se ejecuta automáticamente cada 8 horas.
    @Scheduled(fixedRateString = "PT8H")
    public void expireReservations() {
        List<ReservationEntity> expired = reservationRepository
                .findByStatusAndExpiresAtBefore("PENDING", LocalDateTime.now());

        for (ReservationEntity reservation : expired) {
            try {
                TouristPackageModel pkg = getPackageById(reservation.getPackageId());
                int newSpots = pkg.getAvailableSpots() + reservation.getPassengers();
                String newStatus = "SOLD_OUT".equals(pkg.getStatus()) ? "AVAILABLE" : pkg.getStatus();
                updatePackageSpots(reservation.getPackageId(), newSpots, newStatus);
            } catch (Exception e) {
                // Si package-service no está disponible, igual expiramos la reserva
                // Los cupos se reconciliarán cuando el paquete se actualice manualmente
            }
            reservation.setStatus("EXPIRED");
            reservationRepository.save(reservation);
        }
    }

    // Verifica que la reserva pertenezca al usuario autenticado.
    // Un ADMIN puede acceder a cualquier reserva.
    public void checkOwnership(Long reservationId, String keycloakUserId, boolean isAdmin) {
        if (isAdmin) return;
        ReservationEntity reservation = getReservationById(reservationId);
        if (!reservation.getKeycloakUserId().equals(keycloakUserId)) {
            throw new RuntimeException("Access denied: this reservation does not belong to you");
        }
    }
}
