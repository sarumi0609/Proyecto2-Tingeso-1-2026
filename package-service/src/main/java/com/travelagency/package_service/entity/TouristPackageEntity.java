package com.travelagency.package_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "Touristpackages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TouristPackageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Name of the tour package
    @Column(nullable = false)
    private String name;

    // Trip type: NATIONAL o INTERNATIONAL
    @Column(nullable = false)
    private String travelType;

    @Column
    private String country;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer totalSpots;

    @Column(nullable = false)
    private Integer availableSpots;

    @Column(length = 1000)
    private String includedServices;


    @Column(length = 1000)
    private String conditions;

    // category: aventura, relax, cultural, etc.
    private String category;

    // Package status: AVAILABLE, SOLD_OUT, NOT_VALID, CANCELLED
    @Column(nullable = false)
    private String status;
}
