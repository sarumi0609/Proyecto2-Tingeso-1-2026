package com.travelagency.report_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TouristPackageModel {
    private Long id;
    private String name;
    private String travelType;
    private String country;
    private String city;
}