package com.travelagency.reservation_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * POJO que representa los datos de un paquete turístico recibidos desde package-service.
 * No es una entidad JPA — solo se usa para deserializar la respuesta HTTP de RestTemplate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TouristPackageModel {
    private Long id;
    private String name;
    private String travelType;
    private String country;
    private String city;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double price;
    private Integer totalSpots;
    private Integer availableSpots;
    private String includedServices;
    private String conditions;
    private String category;
    private String status;
}
