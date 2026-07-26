package com.travelagency.tracking_service.service;

import com.travelagency.tracking_service.model.ReservationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class TrackingService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String RESERVATION_SERVICE = "http://reservation-service";

    // Obtiene el estado actual de una reserva específica
    public ReservationModel getReservationStatus(Long reservationId) {
        ReservationModel res = restTemplate.getForObject(
                RESERVATION_SERVICE + "/api/reservations/" + reservationId + "/internal",
                ReservationModel.class);
        if (res == null) {
            throw new RuntimeException("Reservation not found: " + reservationId);
        }
        return res;
    }

    // Obtiene todas las reservas de un usuario para ver su historial
    public List<ReservationModel> getReservationsByUser(String keycloakUserId) {
        ReservationModel[] arr = restTemplate.getForObject(
                RESERVATION_SERVICE + "/api/reservations/my",
                ReservationModel[].class);
        return arr != null ? Arrays.asList(arr) : List.of();
    }
}