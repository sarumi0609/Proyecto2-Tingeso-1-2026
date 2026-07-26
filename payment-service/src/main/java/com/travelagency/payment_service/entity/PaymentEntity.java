package com.travelagency.payment_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID de la reserva asociada (plain Long — la reserva vive en reservation-service)
    @Column(name = "reservation_id", nullable = false, unique = true)
    private Long reservationId;

    // Monto del pago
    @Column(nullable = false)
    private Double amount;

    // Medio de pago
    @Column(nullable = false)
    private String paymentMethod;

    // Número de tarjeta simulado (solo últimos 4 dígitos se guardan)
    @Column(nullable = false)
    private String cardNumber;

    // Fecha de expiración simulada (MM/YY)
    @Column(nullable = false)
    private String cardExpirationDate;

    // CVV (nunca se guarda completo)
    @Column(nullable = false)
    private String cvv;

    // Estado del pago: APPROVED, REJECTED
    @Column(nullable = false)
    private String status;

    // Fecha y hora del pago
    @Column(nullable = false)
    private LocalDateTime paymentDate;

    // Identificador único de la transacción
    @Column(nullable = false, unique = true)
    private String transactionId;
}
