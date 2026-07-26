package com.travelagency.search_service.service;

import com.travelagency.search_service.model.TouristPackageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String PACKAGE_SERVICE_URL = "http://package-service/api/packages";

    public List<TouristPackageModel> searchPackages(String destination, Double minPrice,
                                                    Double maxPrice, String type, String season) {
        TouristPackageModel[] packages = restTemplate.getForObject(
                PACKAGE_SERVICE_URL + "/available", TouristPackageModel[].class);

        if (packages == null) return List.of();

        return Arrays.stream(packages)
                .filter(p -> destination == null || p.getDestination()
                        .toLowerCase().contains(destination.toLowerCase()))
                .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .filter(p -> type == null || type.equalsIgnoreCase(p.getType()))
                .filter(p -> season == null || season.equalsIgnoreCase(p.getSeason()))
                .filter(p -> p.getStartDate() != null && !p.getStartDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
    }

    public TouristPackageModel getPackageDetail(Long id) {
        return restTemplate.getForObject(
                PACKAGE_SERVICE_URL + "/" + id, TouristPackageModel.class);
    }
}