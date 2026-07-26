package com.travelagency.package_service.controller;

import com.travelagency.package_service.entity.DiscountConfigEntity;
import com.travelagency.package_service.service.DiscountConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
public class DiscountConfigController {

    @Autowired
    private DiscountConfigService discountConfigService;

    // GET /api/discounts
    // Retorna todos los descuentos configurados — solo para administradores
    @GetMapping

    public ResponseEntity<List<DiscountConfigEntity>> getAllDiscounts() {
        return ResponseEntity.ok(discountConfigService.getAllDiscounts());
    }

    // GET /api/discounts/active
    // Retorna solo los descuentos activos
    @GetMapping("/active")

    public ResponseEntity<List<DiscountConfigEntity>> getActiveDiscounts() {
        return ResponseEntity.ok(discountConfigService.getActiveDiscounts());
    }

    // POST /api/discounts
    // Crea un nuevo descuento — solo para administradores
    @PostMapping

    public ResponseEntity<DiscountConfigEntity> createDiscount(
            @RequestBody DiscountConfigEntity discount) {
        return ResponseEntity.ok(discountConfigService.createDiscount(discount));
    }

    // PUT /api/discounts/{id}
    // Actualiza la configuración de un descuento — solo para administradores
    // Permite cambiar porcentaje, umbral, acumulable y activo
    @PutMapping("/{id}")

    public ResponseEntity<DiscountConfigEntity> updateDiscount(
            @PathVariable Long id,
            @RequestBody DiscountConfigEntity updated) {
        return ResponseEntity.ok(discountConfigService.updateDiscount(id, updated));
    }
}