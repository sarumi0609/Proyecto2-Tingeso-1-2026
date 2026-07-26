package com.travelagency.package_service.repository;

import com.travelagency.package_service.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {

    // Find all active promotions valid on a given date
    List<PromotionEntity> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate startDate, LocalDate endDate);
}