package com.travelagency.payment_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO que representa los datos de una reserva recibidos desde reservation-service.
 * Solo incluye los campos que payment-service necesita.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationModel {
    private Long id;
    private String status;       // PENDING, CONFIRMED, CANCELLED, EXPIRED
    private Double totalAmount;  // monto a cobrar
}
