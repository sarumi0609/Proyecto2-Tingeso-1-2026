package com.travelagency.tracking_service.controller;

import com.travelagency.tracking_service.model.ReservationModel;
import com.travelagency.tracking_service.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    @Autowired
    private TrackingService trackingService;

    // GET /api/tracking/{reservationId}
    // Retorna el estado actual de una reserva específica
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationModel> getReservationStatus(
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(trackingService.getReservationStatus(reservationId));
    }

    // GET /api/tracking/user/{keycloakUserId}
    // Retorna todas las reservas de un usuario con su estado actual
    @GetMapping("/user/{keycloakUserId}")
    public ResponseEntity<List<ReservationModel>> getReservationsByUser(
            @PathVariable String keycloakUserId) {
        return ResponseEntity.ok(trackingService.getReservationsByUser(keycloakUserId));
    }
}