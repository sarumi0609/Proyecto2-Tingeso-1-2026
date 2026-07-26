package com.travelagency.report_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationModel {
    private Long id;
    private String keycloakUserId;
    private Long packageId;
    private Integer passengers;
    private Double totalAmount;
    private String status;
    private LocalDateTime createdAt;
}