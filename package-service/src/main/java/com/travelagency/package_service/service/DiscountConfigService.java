package com.travelagency.package_service.service;

import com.travelagency.package_service.entity.DiscountConfigEntity;
import com.travelagency.package_service.repository.DiscountConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscountConfigService {

    @Autowired
    private DiscountConfigRepository discountConfigRepository;

    // Returns all configured discounts — administrators only
    public List<DiscountConfigEntity> getAllDiscounts() {
        return discountConfigRepository.findAll();
    }

    // Return only active discounts
    public List<DiscountConfigEntity> getActiveDiscounts() {
        return discountConfigRepository.findByActiveTrue();
    }

    // Returns a specific discount based on its type
    public DiscountConfigEntity getDiscountByType(String type) {
        return discountConfigRepository.findByType(type)
                .orElseThrow(() -> new RuntimeException("Discount config not found for type: " + type));
    }

    // Create a new discount — for administrators only
    public DiscountConfigEntity createDiscount(DiscountConfigEntity discount) {
        if (discount.getPercentage() <= 0) {
            throw new RuntimeException("Discount percentage must be greater than zero");
        }
        // Validate that the type does not already exist
        if (discountConfigRepository.findByType(discount.getType()).isPresent()) {
            throw new RuntimeException("A discount config already exists for type: " + discount.getType());
        }
        return discountConfigRepository.save(discount);
    }

    // Update the settings of an existing discount — for administrators only
    public DiscountConfigEntity updateDiscount(Long id, DiscountConfigEntity updated) {
        DiscountConfigEntity existing = discountConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount config not found"));

        if (updated.getPercentage() <= 0) {
            throw new RuntimeException("Discount percentage must be greater than zero");
        }

        existing.setPercentage(updated.getPercentage());
        existing.setThreshold(updated.getThreshold());
        existing.setCumulative(updated.getCumulative());
        existing.setActive(updated.getActive());
        return discountConfigRepository.save(existing);
    }
}