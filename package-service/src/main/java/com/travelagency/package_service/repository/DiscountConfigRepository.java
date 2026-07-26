package com.travelagency.package_service.repository;

import com.travelagency.package_service.entity.DiscountConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountConfigRepository extends JpaRepository<DiscountConfigEntity, Long> {

    // Look for a discount for your type (GROUP, FREQUENT_CLIENT, MULTI_PACKAGE)
    Optional<DiscountConfigEntity> findByType(String type);

    // Return all active discounts
    List<DiscountConfigEntity> findByActiveTrue();

}
