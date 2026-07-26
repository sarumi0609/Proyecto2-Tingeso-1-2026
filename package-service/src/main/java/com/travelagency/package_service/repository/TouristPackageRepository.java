package com.travelagency.package_service.repository;

import com.travelagency.package_service.entity.TouristPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TouristPackageRepository extends JpaRepository<TouristPackageEntity, Long> {

    // Search for packages by state
    List<TouristPackageEntity> findByStatus(String status);

    // Search for available packages by city
    List<TouristPackageEntity> findByCityAndStatus(String city, String status);

    // Search for available packages by country — for international travelers
    List<TouristPackageEntity> findByCountryAndStatus(String country, String status);

    // Search for available packages by trip type (NATIONAL o INTERNATIONAL)
    List<TouristPackageEntity> findByTravelTypeAndStatus(String travelType, String status);

    // Search for packages whose price falls within a defined range and are available
    List<TouristPackageEntity> findByPriceBetweenAndStatus(
            Double minPrice, Double maxPrice, String status);

    //Search for packages by category and status
    List<TouristPackageEntity> findByCategoryAndStatus(String category, String status);

    // Search for packages with a start date after a given date and that are available
    List<TouristPackageEntity> findByStartDateAfterAndStatus(LocalDate date, String status);
}
