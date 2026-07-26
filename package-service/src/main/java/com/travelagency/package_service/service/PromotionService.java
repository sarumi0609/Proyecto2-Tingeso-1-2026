package com.travelagency.package_service.service;

import com.travelagency.package_service.entity.PromotionEntity;
import com.travelagency.package_service.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    // Retorna todas las promociones — solo para administradores
    public List<PromotionEntity> getAllPromotions() {
        return promotionRepository.findAll();
    }

    // Retorna las promociones vigentes hoy.
    // Una promoción es vigente si está activa y la fecha actual
    // está dentro de su rango startDate - endDate.
    public List<PromotionEntity> getActivePromotionsToday() {
        LocalDate today = LocalDate.now();
        return promotionRepository
                .findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        today, today);
    }

    // Crea una nueva promoción — solo para administradores
    public PromotionEntity createPromotion(PromotionEntity promotion) {
        // Validar que la fecha de término sea posterior a la de inicio
        if (!promotion.getEndDate().isAfter(promotion.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }
        // Si el beneficio es DISCOUNT, validar que tenga porcentaje mayor que cero
        if ("DISCOUNT".equals(promotion.getBenefitType())) {
            if (promotion.getPercentage() == null || promotion.getPercentage() <= 0) {
                throw new RuntimeException("Discount promotion must have a percentage greater than zero");
            }
        }
        return promotionRepository.save(promotion);
    }

    // Actualiza una promoción existente — solo para administradores
    public PromotionEntity updatePromotion(Long id, PromotionEntity updated) {
        PromotionEntity existing = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        if (!updated.getEndDate().isAfter(updated.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }
        if ("DISCOUNT".equals(updated.getBenefitType())) {
            if (updated.getPercentage() == null || updated.getPercentage() <= 0) {
                throw new RuntimeException("Discount promotion must have a percentage greater than zero");
            }
        }

        existing.setName(updated.getName());
        existing.setBenefitType(updated.getBenefitType());
        existing.setBenefitDescription(updated.getBenefitDescription());
        existing.setPercentage(updated.getPercentage());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setActive(updated.getActive());
        existing.setCumulative(updated.getCumulative());
        return promotionRepository.save(existing);
    }

    // Desactiva una promoción sin eliminarla físicamente
    public PromotionEntity deactivatePromotion(Long id) {
        PromotionEntity promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        promotion.setActive(false);
        return promotionRepository.save(promotion);
    }

    // Elimina una promoción físicamente — solo para administradores
    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion not found");
        }
        promotionRepository.deleteById(id);
    }
}