package com.travelagency.payment_service.repository;

import com.travelagency.payment_service.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    // Busca el pago asociado a una reserva
    Optional<PaymentEntity> findByReservationId(Long reservationId);

    // Verifica si ya existe un pago para una reserva
    Boolean existsByReservationId(Long reservationId);

    // Busca un pago por su identificador de transacción
    Optional<PaymentEntity> findByTransactionId(String transactionId);
}
