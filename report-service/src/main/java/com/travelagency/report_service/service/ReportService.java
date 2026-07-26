package com.travelagency.report_service.service;

import com.travelagency.report_service.model.ReservationModel;
import com.travelagency.report_service.model.TouristPackageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String RESERVATION_SERVICE = "http://reservation-service";
    private static final String PACKAGE_SERVICE    = "http://package-service";

    private List<ReservationModel> getAllReservations() {
        ReservationModel[] arr = restTemplate.getForObject(
                RESERVATION_SERVICE + "/api/reservations",
                ReservationModel[].class);
        return arr != null ? Arrays.asList(arr) : new ArrayList<>();
    }

    private TouristPackageModel getPackageById(Long packageId) {
        return restTemplate.getForObject(
                PACKAGE_SERVICE + "/api/touristpackages/" + packageId,
                TouristPackageModel.class);
    }

    // Reporte 1: ventas confirmadas en un rango de fechas
    public List<ReservationModel> getSalesByPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be after end date");
        }
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end   = endDate.atTime(23, 59, 59);

        return getAllReservations().stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .filter(r -> r.getCreatedAt().isAfter(start) && r.getCreatedAt().isBefore(end))
                .sorted(Comparator.comparing(ReservationModel::getCreatedAt))
                .collect(Collectors.toList());
    }

    // Reporte 2: ranking de paquetes por reservas confirmadas en el período
    public List<Map<String, Object>> getPackageRankingByPeriod(
            LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be after end date");
        }
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end   = endDate.atTime(23, 59, 59);

        List<ReservationModel> confirmed = getAllReservations().stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .filter(r -> r.getCreatedAt().isAfter(start) && r.getCreatedAt().isBefore(end))
                .collect(Collectors.toList());

        Map<Long, Map<String, Object>> rankingMap = new LinkedHashMap<>();

        for (ReservationModel reservation : confirmed) {
            Long packageId = reservation.getPackageId();
            if (!rankingMap.containsKey(packageId)) {
                // Obtener datos del paquete desde package-service
                TouristPackageModel pkg = getPackageById(packageId);
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("packageId",   packageId);
                data.put("packageName", pkg != null ? pkg.getName()       : "Unknown");
                data.put("travelType",  pkg != null ? pkg.getTravelType() : "Unknown");
                data.put("country",     pkg != null ? pkg.getCountry()    : "");
                data.put("city",        pkg != null ? pkg.getCity()       : "");
                data.put("totalReservations", 0);
                data.put("totalPassengers",   0);
                data.put("totalRevenue",      0.0);
                rankingMap.put(packageId, data);
            }
            Map<String, Object> data = rankingMap.get(packageId);
            data.put("totalReservations", (int)  data.get("totalReservations") + 1);
            data.put("totalPassengers",   (int)  data.get("totalPassengers")   + reservation.getPassengers());
            data.put("totalRevenue",      (double)data.get("totalRevenue")     + reservation.getTotalAmount());
        }

        return rankingMap.values().stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(
                            (int) b.get("totalReservations"),
                            (int) a.get("totalReservations"));
                    return cmp != 0 ? cmp :
                            ((String) a.get("packageName")).compareTo((String) b.get("packageName"));
                })
                .collect(Collectors.toList());
    }
}