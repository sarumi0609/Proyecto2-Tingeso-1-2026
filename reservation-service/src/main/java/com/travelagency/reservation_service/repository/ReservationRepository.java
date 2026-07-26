package com.travelagency.reservation_service.repository;

import com.travelagency.reservation_service.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    // Search all of a user's reservations by their Keycloak ID
    List<ReservationEntity> findByKeycloakUserId(String keycloakUserId);

    // Search for a user's reservations by status
    List<ReservationEntity> findByKeycloakUserIdAndStatus(String keycloakUserId, String status);

    // Count of a user's reservations per status
    Integer countByKeycloakUserIdAndStatus(String keycloakUserId, String status);

    // Count of pending reservations within a time period
    Integer countByKeycloakUserIdAndStatusAndCreatedAtGreaterThanEqual(
            String keycloakUserId, String status, LocalDateTime from);

    // Search all reservations for a specific tour package (uses packageId field)
    List<ReservationEntity> findByPackageId(Long packageId);

    // Count non-cancelled reservations for a package
    Integer countByPackageIdAndStatusNot(Long packageId, String status);

    // Search for overdue pending reservations (for the expiration scheduler)
    List<ReservationEntity> findByStatusAndExpiresAtBefore(String status, LocalDateTime dateTime);

    // Search all reservations by status — for administrators
    List<ReservationEntity> findByStatus(String status);
}
