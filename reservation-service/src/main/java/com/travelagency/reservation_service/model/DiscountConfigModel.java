package com.travelagency.reservation_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO que representa un descuento recibido desde package-service.
 * No es una entidad JPA — solo se usa para deserializar la respuesta HTTP de RestTemplate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountConfigModel {
    private Long id;
    // Tipo: GROUP, FREQUENT_CLIENT, MULTI_PACKAGE
    private String type;
    // Porcentaje como decimal (ej: 0.10 = 10%)
    private Double percentage;
    // Umbral mínimo para que aplique el descuento
    private Integer threshold;
    // true = acumulable con otros descuentos; false = excluyente
    private Boolean cumulative;
    // true = activo
    private Boolean active;
    // Periodo en horas para evaluar MULTI_PACKAGE
    private Integer periodInHours;
}
