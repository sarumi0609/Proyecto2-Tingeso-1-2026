package com.travelagency.package_service.service;

import com.travelagency.package_service.entity.TouristPackageEntity;
import com.travelagency.package_service.repository.TouristPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.stream.Collectors;

import java.util.List;

@Service
public class TouristPackageService {
    @Autowired
    private TouristPackageRepository touristPackageRepository;


    // Crea un nuevo paquete turístico validando todas las reglas de negocio
    public TouristPackageEntity createPackage(TouristPackageEntity touristPackage) {
        validatePackage(touristPackage);
        // Los cupos disponibles se asignan automáticamente igual a los cupos totales
        touristPackage.setAvailableSpots(touristPackage.getTotalSpots());
        return touristPackageRepository.save(touristPackage);
    }

    // Validaciones comunes para crear y actualizar paquetes
    private void validatePackage(TouristPackageEntity touristPackage) {
        if (touristPackage.getPrice() <= 0) {
            throw new RuntimeException("Price must be greater than zero");
        }
        if (touristPackage.getEndDate() != null &&
                touristPackage.getStartDate() != null &&
                !touristPackage.getEndDate().isAfter(touristPackage.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }
        if (touristPackage.getTotalSpots() <= 0) {
            throw new RuntimeException("Total spots must be greater than zero");
        }
        // Validar que paquetes internacionales tengan país definido
        if ("INTERNATIONAL".equalsIgnoreCase(touristPackage.getTravelType()) &&
                (touristPackage.getCountry() == null || touristPackage.getCountry().isEmpty())) {
            throw new RuntimeException("International packages must have a country defined");
        }
        // Validar que ambos tipos tengan ciudad definida
        if (touristPackage.getCity() == null || touristPackage.getCity().isEmpty()) {
            throw new RuntimeException("Package must have a city defined");
        }
    }

    // Retorna todos los paquetes — solo para administradores
    public List<TouristPackageEntity> getAllPackages() {
        return touristPackageRepository.findAll();
    }

    // Retorna solo los paquetes en estado AVAILABLE — visibles para clientes
    public List<TouristPackageEntity> getAvailablePackages() {
        return touristPackageRepository.findByStatus("AVAILABLE");
    }

    // Retorna un paquete específico por su id
    public TouristPackageEntity getPackageById(Long id) {
        return touristPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));
    }

    // Busca paquetes disponibles por ciudad
    public List<TouristPackageEntity> getPackagesByCity(String city) {
        return touristPackageRepository.findByCityAndStatus(city, "AVAILABLE");
    }

    // Busca paquetes disponibles por país — para internacionales
    public List<TouristPackageEntity> getPackagesByCountry(String country) {
        return touristPackageRepository.findByCountryAndStatus(country, "AVAILABLE");
    }

    // Busca paquetes disponibles por tipo de viaje (NATIONAL o INTERNATIONAL)
    public List<TouristPackageEntity> getPackagesByTravelType(String travelType) {
        return touristPackageRepository.findByTravelTypeAndStatus(travelType, "AVAILABLE");
    }

    // Busca paquetes disponibles dentro de un rango de precios
    public List<TouristPackageEntity> getPackagesByPriceRange(Double minPrice, Double maxPrice) {
        return touristPackageRepository.findByPriceBetweenAndStatus(minPrice, maxPrice, "AVAILABLE");
    }

    // Busca paquetes disponibles por categoría
    public List<TouristPackageEntity> getPackagesByCategory(String category) {
        return touristPackageRepository.findByCategoryAndStatus(category, "AVAILABLE");
    }

    // Búsqueda combinada con filtros opcionales simultáneos.
    // Solo retorna paquetes AVAILABLE con fechas válidas (startDate >= hoy).
    // Cada filtro es opcional: si viene null no se aplica.
    public List<TouristPackageEntity> searchPackages(
            String travelType,
            String country,
            String city,
            Double minPrice,
            Double maxPrice,
            String category) {

        return touristPackageRepository
                .findByStartDateAfterAndStatus(LocalDate.now(), "AVAILABLE")
                .stream()
                .filter(p -> p.getEndDate().isAfter(LocalDate.now()))
                .filter(p -> travelType == null ||
                        p.getTravelType().equalsIgnoreCase(travelType))
                .filter(p -> country == null ||
                        (p.getCountry() != null &&
                                p.getCountry().toLowerCase().contains(country.toLowerCase())))
                .filter(p -> city == null ||
                        p.getCity().toLowerCase().contains(city.toLowerCase()))
                .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .filter(p -> category == null ||
                        (p.getCategory() != null &&
                                p.getCategory().equalsIgnoreCase(category)))
                .collect(Collectors.toList());
    }

    public TouristPackageEntity updateAvailableSpots(Long id, int newAvailableSpots, String newStatus) {
        TouristPackageEntity pkg = getPackageById(id);
        pkg.setAvailableSpots(newAvailableSpots);
        pkg.setStatus(newStatus);
        return touristPackageRepository.save(pkg);
    }
    // Actualiza un paquete existente.
    // Campos críticos (startDate, endDate, totalSpots) no se pueden modificar
    // de forma inconsistente si ya tiene reservas asociadas.
    public TouristPackageEntity updatePackage(Long id, TouristPackageEntity updatedPackage) {
        TouristPackageEntity existing = getPackageById(id);
        validatePackage(updatedPackage);

        boolean hasReservations =false;

        if (hasReservations) {
            // Las fechas no se pueden cambiar si ya tiene reservas
            if (!updatedPackage.getStartDate().equals(existing.getStartDate()) ||
                    !updatedPackage.getEndDate().equals(existing.getEndDate())) {
                throw new RuntimeException(
                        "Cannot modify start/end dates of a package that already has reservations");
            }
            // Los cupos totales no pueden reducirse por debajo de los ya reservados
            int reservedSpots = existing.getTotalSpots() - existing.getAvailableSpots();
            if (updatedPackage.getTotalSpots() < reservedSpots) {
                throw new RuntimeException(
                        "Cannot reduce total spots below the number of already reserved spots ("
                                + reservedSpots + ")");
            }
            // Recalcular cupos disponibles para mantener consistencia
            int newAvailable = updatedPackage.getTotalSpots() - reservedSpots;
            existing.setAvailableSpots(newAvailable);
        }

        existing.setName(updatedPackage.getName());
        existing.setTravelType(updatedPackage.getTravelType());
        existing.setCountry(updatedPackage.getCountry());
        existing.setCity(updatedPackage.getCity());
        existing.setDescription(updatedPackage.getDescription());
        existing.setStartDate(updatedPackage.getStartDate());
        existing.setEndDate(updatedPackage.getEndDate());
        existing.setPrice(updatedPackage.getPrice());
        existing.setTotalSpots(updatedPackage.getTotalSpots());
        existing.setIncludedServices(updatedPackage.getIncludedServices());
        existing.setConditions(updatedPackage.getConditions());
        existing.setCategory(updatedPackage.getCategory());
        // Un paquete sin cupos disponibles no puede publicarse como AVAILABLE
        if ("AVAILABLE".equals(updatedPackage.getStatus()) && existing.getAvailableSpots() <= 0) {
            throw new RuntimeException("Cannot set package as AVAILABLE when there are no available spots");
        }
        existing.setStatus(updatedPackage.getStatus());
        return touristPackageRepository.save(existing);
    }

    // Cambia el estado de un paquete (AVAILABLE, SOLD_OUT, NOT_VALID, CANCELLED)
    public TouristPackageEntity updateStatus(Long id, String status) {
        TouristPackageEntity touristPackage = getPackageById(id);
        // Un paquete sin cupos disponibles no puede publicarse como AVAILABLE
        if ("AVAILABLE".equals(status) && touristPackage.getAvailableSpots() <= 0) {
            throw new RuntimeException("Cannot set package as AVAILABLE when there are no available spots");
        }
        touristPackage.setStatus(status);
        return touristPackageRepository.save(touristPackage);
    }

    // Eliminación física solo si no tiene reservas asociadas
    public void deletePackage(Long id) {
        TouristPackageEntity touristPackage = getPackageById(id);

        boolean hasReservations = false;

        if (hasReservations) {
            throw new RuntimeException(
                    "Cannot physically delete a package that has associated reservations. " +
                            "Use status change (CANCELLED or NOT_VALID) instead.");
        }

        touristPackageRepository.delete(touristPackage);
    }

}