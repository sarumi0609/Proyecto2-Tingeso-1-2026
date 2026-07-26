package com.travelagency.package_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "discount_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //discount type: GROUP, FREQUENT_CLIENT, MULTI_PACKAGE
    @Column(nullable = false, unique = true)
    private String type;

    //discount type(ej: 0.10 = 10%)
    @Column(nullable = false)
    private Double percentage;

    // Threshold required for the discount to apply
    // for GROUP: minimum number of passengers
    // fot FREQUENT_CLIENT: minimum number of confirmed reservations
    @Column(nullable = false)
    private Integer threshold;

    // Define whether this discount can be combined with others (true) or is exclusive (false)
    @Column(nullable = false)
    private Boolean cumulative;

    // Define whether this discount is active or deactivated
    @Column(nullable = false)
    private Boolean active;

    // Period in hours within which the discount condition is evaluated.
    @Column
    private Integer periodInHours;
}