package com.travelagency.package_service.controller;

import com.travelagency.package_service.entity.PromotionEntity;
import com.travelagency.package_service.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    // GET /api/promotions
    // Retorna todas las promociones — solo para administradores
    @GetMapping

    public ResponseEntity<List<PromotionEntity>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    // GET /api/promotions/active
    // Retorna las promociones vigentes hoy — para mostrar al cliente
    // qué promociones están disponibles al momento de reservar
    @GetMapping("/active")
    public ResponseEntity<List<PromotionEntity>> getActivePromotionsToday() {
        return ResponseEntity.ok(promotionService.getActivePromotionsToday());
    }

    // POST /api/promotions
    // Crea una nueva promoción — solo para administradores
    @PostMapping

    public ResponseEntity<PromotionEntity> createPromotion(
            @RequestBody PromotionEntity promotion) {
        return ResponseEntity.ok(promotionService.createPromotion(promotion));
    }

    // PUT /api/promotions/{id}
    // Actualiza una promoción existente — solo para administradores
    @PutMapping("/{id}")

    public ResponseEntity<PromotionEntity> updatePromotion(
            @PathVariable Long id,
            @RequestBody PromotionEntity updated) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, updated));
    }

    // PATCH /api/promotions/{id}/deactivate
    // Desactiva una promoción sin eliminarla físicamente
    @PatchMapping("/{id}/deactivate")

    public ResponseEntity<PromotionEntity> deactivatePromotion(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.deactivatePromotion(id));
    }

    // DELETE /api/promotions/{id}
    // Elimina una promoción físicamente — solo para administradores
    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}