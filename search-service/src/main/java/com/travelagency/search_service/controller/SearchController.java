package com.travelagency.search_service.controller;


import com.travelagency.search_service.model.TouristPackageModel;
import com.travelagency.search_service.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    // GET /api/search?destination=Paris&minPrice=100&maxPrice=500&type=aventura&season=verano
    @GetMapping
    public ResponseEntity<List<TouristPackageModel>> searchPackages(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String season) {
        return ResponseEntity.ok(searchService.searchPackages(
                destination, minPrice, maxPrice, type, season));
    }

    // GET /api/search/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TouristPackageModel> getPackageDetail(@PathVariable Long id) {
        TouristPackageModel pkg = searchService.getPackageDetail(id);
        if (pkg == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(pkg);
    }
}