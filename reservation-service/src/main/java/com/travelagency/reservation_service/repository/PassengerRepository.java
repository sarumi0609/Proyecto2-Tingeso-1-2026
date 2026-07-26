package com.travelagency.reservation_service.repository;

import com.travelagency.reservation_service.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerRepository extends JpaRepository<PassengerEntity, Long> {

    //Search for all passengers on a specific booking
    List<PassengerEntity> findByReservationId(Long reservationId);
}