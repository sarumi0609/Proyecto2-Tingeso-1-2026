package com.travelagency.package_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Descriptive name of the promotion (e.g., "Summer Promotion")
    @Column(nullable = false)
    private String name;

    // Type of benefit that the promotion provides
    // Possible values: DISCOUNT, FREE_LUGGAGE, FREE_TRANSFER, FREE_NIGHT, FREE_BREAKFAST, OTHER
    @Column(nullable = false)
    private String benefitType;

    // Clear description of the benefit to show the customer on the receipt
    // Ex: "Includes free airport transfer," "23kg suitcase included"
    @Column(nullable = false, length = 500)
    private String benefitDescription;

    // Discount percentage — only applies if benefitType is DISCOUNT
    // For other benefit types, leave it as null
    @Column
    private Double percentage;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Boolean cumulative;

    @ManyToOne
    @JoinColumn(name = "package_id")
    private TouristPackageEntity touristPackage;
}