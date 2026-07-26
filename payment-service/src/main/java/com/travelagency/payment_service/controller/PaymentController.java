package com.travelagency.payment_service.controller;

import com.travelagency.payment_service.entity.PaymentEntity;
import com.travelagency.payment_service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // GET /api/payments
    // Retorna todos los pagos — para administradores
    @GetMapping
    public ResponseEntity<List<PaymentEntity>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // GET /api/payments/{id}
    // Retorna un pago específico por su ID
    @GetMapping("/{id}")
    public ResponseEntity<PaymentEntity> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // GET /api/payments/reservation/{reservationId}
    // Retorna el pago asociado a una reserva
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentEntity> getPaymentByReservation(
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getPaymentByReservation(reservationId));
    }

    // POST /api/payments
    // Procesa un pago simulado para una reserva.
    // El sistema asume que todo pago es exitoso.
    // Confirma la reserva automáticamente tras el pago.
    @PostMapping
    public ResponseEntity<PaymentEntity> processPayment(
            @RequestParam Long reservationId,
            @RequestParam String cardNumber,
            @RequestParam String cardExpirationDate,
            @RequestParam String cvv) {
        return ResponseEntity.ok(paymentService.processPayment(
                reservationId, cardNumber, cardExpirationDate, cvv));
    }
}
