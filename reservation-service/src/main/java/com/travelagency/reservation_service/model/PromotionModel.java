package com.travelagency.reservation_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * POJO que representa una promoción recibida desde package-service.
 * No es una entidad JPA — solo se usa para deserializar la respuesta HTTP de RestTemplate.
 * El campo touristPackage viene anidado en el JSON del package-service; lo mapeamos
 * como TouristPackageModel para poder leer su ID y filtrar por paquete.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionModel {
    private Long id;
    private String name;
    // DISCOUNT, FREE_LUGGAGE, FREE_TRANSFER, FREE_NIGHT, FREE_BREAKFAST, OTHER
    private String benefitType;
    private String benefitDescription;
    // Solo aplica si benefitType es DISCOUNT (ej: 40.0 = 40%)
    private Double percentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private Boolean cumulative;
    // Paquete asociado (null = promoción global para todos los paquetes)
    private TouristPackageModel touristPackage;
}
