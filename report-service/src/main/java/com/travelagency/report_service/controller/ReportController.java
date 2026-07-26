package com.travelagency.report_service.controller;

import com.travelagency.report_service.model.ReservationModel;
import com.travelagency.report_service.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // GET /api/reports/sales?startDate=2026-01-01&endDate=2026-12-31
    @GetMapping("/sales")
    public ResponseEntity<List<ReservationModel>> getSalesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getSalesByPeriod(startDate, endDate));
    }

    // GET /api/reports/ranking?startDate=2026-01-01&endDate=2026-12-31
    @GetMapping("/ranking")
    public ResponseEntity<List<Map<String, Object>>> getPackageRankingByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getPackageRankingByPeriod(startDate, endDate));
    }
}