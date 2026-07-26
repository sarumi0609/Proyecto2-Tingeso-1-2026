package com.travelagency.payment_service.service;

import com.travelagency.payment_service.entity.PaymentEntity;
import com.travelagency.payment_service.model.ReservationModel;
import com.travelagency.payment_service.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // RestTemplate con @LoadBalanced — resuelve "reservation-service" via Eureka
    @Autowired
    private RestTemplate restTemplate;

    private static final String RESERVATION_SERVICE = "http://reservation-service";

    // ---------------------------------------------------------------
    // Comunicación con reservation-service
    // ---------------------------------------------------------------

    private ReservationModel getReservationById(Long reservationId) {
        ReservationModel res = restTemplate.getForObject(
                RESERVATION_SERVICE + "/api/reservations/" + reservationId + "/internal",
                ReservationModel.class);
        if (res == null) {
            throw new RuntimeException("Reservation not found: " + reservationId);
        }
        return res;
    }

    private void confirmReservation(Long reservationId) {
        restTemplate.exchange(
                RESERVATION_SERVICE + "/api/reservations/" + reservationId + "/confirm",
                HttpMethod.PATCH, null, Void.class);
    }

    // ---------------------------------------------------------------
    // Lógica de negocio
    // ---------------------------------------------------------------

    /**
     * Procesa un pago simulado para una reserva.
     * Asume que todo pago es exitoso (simulación).
     * Tras aprobar el pago, confirma la reserva en reservation-service.
     */
    public PaymentEntity processPayment(Long reservationId, String cardNumber,
                                        String cardExpirationDate, String cvv) {

        // Obtener la reserva desde reservation-service
        ReservationModel reservation = getReservationById(reservationId);

        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new RuntimeException("Cannot process payment for a cancelled reservation");
        }
        if ("CONFIRMED".equals(reservation.getStatus())) {
            throw new RuntimeException("Reservation is already paid and confirmed");
        }
        if (paymentRepository.existsByReservationId(reservationId)) {
            throw new RuntimeException("A payment already exists for this reservation");
        }
        if (reservation.getTotalAmount() <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }

        // Crear el pago simulado
        PaymentEntity payment = new PaymentEntity();
        payment.setReservationId(reservationId);
        payment.setAmount(reservation.getTotalAmount());
        payment.setPaymentMethod("CREDIT_CARD");
        // Solo guardamos los últimos 4 dígitos por seguridad
        payment.setCardNumber("****" + cardNumber.substring(cardNumber.length() - 4));
        payment.setCardExpirationDate(cardExpirationDate);
        payment.setCvv("***");
        payment.setStatus("APPROVED");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionId(UUID.randomUUID().toString());

        PaymentEntity saved = paymentRepository.save(payment);

        // Confirmar la reserva automáticamente tras el pago exitoso
        confirmReservation(reservationId);

        return saved;
    }

    // Retorna todos los pagos — para administradores
    public List<PaymentEntity> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Retorna el pago asociado a una reserva específica
    public PaymentEntity getPaymentByReservation(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new RuntimeException("No payment found for this reservation"));
    }

    // Retorna un pago por su ID
    public PaymentEntity getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}
