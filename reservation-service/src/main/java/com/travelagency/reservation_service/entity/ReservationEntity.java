package com.travelagency.reservation_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User ID in Keycloak — comes from the JWT token via gateway header
    @Column(nullable = false)
    private String keycloakUserId;

    // ID of the booked tour package (plain Long — no JPA relationship).
    // The full package data lives in package-service; we only store the ID here.
    @Column(name = "package_id", nullable = false)
    private Long packageId;

    // Number of passengers, must be greater than zero
    @Column(nullable = false)
    private Integer passengers;

    // List with the details of each passenger
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PassengerEntity> passengerList = new ArrayList<>();

    // Total base price without discounts
    @Column(nullable = false)
    private Double baseAmount;

    // Discount amount applied
    @Column(nullable = false)
    private Double discountAmount;

    // Final amount after applying discount
    @Column(nullable = false)
    private Double totalAmount;

    // Percentage discount per group
    private Double groupDiscountPercentage;

    // Percentage discount for frequent customers
    private Double frequentClientDiscountPercentage;

    // Percentage discount for multiple packages
    private Double multiPackageDiscountPercentage;

    // Details of the discounts applied to show to the customer
    @Column(length = 500)
    private String discountDetails;

    // Reservation status: PENDING, CONFIRMED, CANCELLED, EXPIRED
    @Column(nullable = false)
    private String status;

    // Date and time the reservation was created
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Deadline for making the payment
    private LocalDateTime expiresAt;

    // Discount percentage applied by DISCOUNT promotions
    private Double promotionDiscountPercentage;

    // Text description of promotions applied — stored here so the receipt can be
    // shown without calling package-service again.
    @Column(length = 1000)
    private String appliedPromotionsDetails;
}
