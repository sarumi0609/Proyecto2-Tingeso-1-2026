package com.travelagency.reservation_service.controller;

import com.travelagency.reservation_service.entity.PassengerEntity;
import com.travelagency.reservation_service.entity.ReservationEntity;
import com.travelagency.reservation_service.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // GET /api/reservations
    // Retorna todas las reservas — solo para administradores
    @GetMapping
    public ResponseEntity<List<ReservationEntity>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    // GET /api/reservations/my
    // Retorna las reservas del usuario autenticado.
    // El gateway inyecta el ID del usuario en el header X-User-Id.
    @GetMapping("/my")
    public ResponseEntity<List<ReservationEntity>> getMyReservations(
            @RequestHeader("X-User-Id") String keycloakUserId) {
        return ResponseEntity.ok(reservationService.getReservationsByUser(keycloakUserId));
    }

    // GET /api/reservations/{id}
    // Un USER solo puede ver sus propias reservas; un ADMIN puede ver cualquiera.
    @GetMapping("/{id}")
    public ResponseEntity<ReservationEntity> getReservationById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String keycloakUserId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        boolean isAdmin = "ADMIN".equals(userRole);
        reservationService.checkOwnership(id, keycloakUserId, isAdmin);
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    // GET /api/reservations/{id}/receipt
    // Un USER solo puede ver el comprobante de sus propias reservas.
    @GetMapping("/{id}/receipt")
    public ResponseEntity<ReservationEntity> getReservationReceipt(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String keycloakUserId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        boolean isAdmin = "ADMIN".equals(userRole);
        reservationService.checkOwnership(id, keycloakUserId, isAdmin);
        return ResponseEntity.ok(reservationService.getReservationReceipt(id));
    }

    // POST /api/reservations
    // Crea una nueva reserva con los datos de los pasajeros.
    // El gateway inyecta el ID del usuario en X-User-Id.
    @PostMapping
    public ResponseEntity<ReservationEntity> createReservation(
            @RequestHeader("X-User-Id") String keycloakUserId,
            @RequestParam Long packageId,
            @RequestParam Integer passengers,
            @RequestBody List<PassengerEntity> passengerList) {
        return ResponseEntity.ok(reservationService.createReservation(
                keycloakUserId, packageId, passengers, passengerList));
    }

    // PATCH /api/reservations/{id}/cancel
    // Un USER solo puede cancelar sus propias reservas.
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationEntity> cancelReservation(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String keycloakUserId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        boolean isAdmin = "ADMIN".equals(userRole);
        reservationService.checkOwnership(id, keycloakUserId, isAdmin);
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    // PATCH /api/reservations/{id}/confirm
    // Confirma una reserva — llamado por payment-service o un administrador
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ReservationEntity> confirmReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmReservation(id));
    }

    // GET /api/reservations/{id}/internal
    // Endpoint interno para llamadas service-to-service (sin headers de usuario).
    // Lo usa payment-service para obtener el totalAmount y status de una reserva.
    @GetMapping("/{id}/internal")
    public ResponseEntity<ReservationEntity> getReservationByIdInternal(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }
}
