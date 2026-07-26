package com.travelagency.package_service.controller;

import com.travelagency.package_service.entity.TouristPackageEntity;
import com.travelagency.package_service.service.TouristPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/touristpackages")

public class TouristPackageController {
    @Autowired
    private TouristPackageService touristPackageService;

    // GET /api/touristpackages
    // Retorna todos los paquetes — solo para administradores
    @GetMapping

    public ResponseEntity<List<TouristPackageEntity>> getAllPackages() {
        return ResponseEntity.ok(touristPackageService.getAllPackages());
    }

    // GET /api/touristpackages/available
    // Retorna solo los paquetes en estado AVAILABLE — para clientes
    @GetMapping("/available")
    public ResponseEntity<List<TouristPackageEntity>> getAvailablePackages() {
        return ResponseEntity.ok(touristPackageService.getAvailablePackages());
    }

    // GET /api/touristpackages/{id}
    // Retorna un paquete específico por su id
    @GetMapping("/{id}")
    public ResponseEntity<TouristPackageEntity> getPackageById(@PathVariable Long id) {
        return ResponseEntity.ok(touristPackageService.getPackageById(id));
    }

    // GET /api/touristpackages/search
    // Búsqueda combinada con filtros opcionales. Ejemplos:
    //   /search?travelType=NATIONAL&city=Iquique
    //   /search?travelType=INTERNATIONAL&country=Francia&minPrice=500
    //   /search?category=aventura&maxPrice=1000
    // Todos los parámetros son opcionales y se pueden combinar.
    // Solo retorna paquetes AVAILABLE con fechas válidas.
    @GetMapping("/search")
    public ResponseEntity<List<TouristPackageEntity>> searchPackages(
            @RequestParam(required = false) String travelType,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(touristPackageService.searchPackages(
                travelType, country, city, minPrice, maxPrice, category));
    }

    // GET /api/touristpackages/city/{city}
    // Busca paquetes disponibles por ciudad
    @GetMapping("/city/{city}")
    public ResponseEntity<List<TouristPackageEntity>> getPackagesByCity(
            @PathVariable String city) {
        return ResponseEntity.ok(touristPackageService.getPackagesByCity(city));
    }

    // GET /api/touristpackages/country/{country}
    // Busca paquetes disponibles por país — para internacionales
    @GetMapping("/country/{country}")
    public ResponseEntity<List<TouristPackageEntity>> getPackagesByCountry(
            @PathVariable String country) {
        return ResponseEntity.ok(touristPackageService.getPackagesByCountry(country));
    }

    // GET /api/touristpackages/type/{travelType}
    // Busca paquetes disponibles por tipo (NATIONAL o INTERNATIONAL)
    @GetMapping("/type/{travelType}")
    public ResponseEntity<List<TouristPackageEntity>> getPackagesByTravelType(
            @PathVariable String travelType) {
        return ResponseEntity.ok(touristPackageService.getPackagesByTravelType(travelType));
    }

    // GET /api/touristpackages/price?min=100&max=500
    // Busca paquetes disponibles dentro de un rango de precios
    @GetMapping("/price")
    public ResponseEntity<List<TouristPackageEntity>> getPackagesByPriceRange(
            @RequestParam Double min,
            @RequestParam Double max) {
        return ResponseEntity.ok(touristPackageService.getPackagesByPriceRange(min, max));
    }

    // GET /api/touristpackages/category/{category}
    // Busca paquetes disponibles por categoría
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TouristPackageEntity>> getPackagesByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(touristPackageService.getPackagesByCategory(category));
    }

    // POST /api/touristpackages
    // Crea un nuevo paquete turístico — solo para administradores
    @PostMapping

    public ResponseEntity<TouristPackageEntity> createPackage(
            @RequestBody TouristPackageEntity touristPackage) {
        return ResponseEntity.ok(touristPackageService.createPackage(touristPackage));
    }

    // PUT /api/touristpackages/{id}
    // Actualiza un paquete existente — solo para administradores
    @PutMapping("/{id}")

    public ResponseEntity<TouristPackageEntity> updatePackage(
            @PathVariable Long id,
            @RequestBody TouristPackageEntity updatedPackage) {
        return ResponseEntity.ok(touristPackageService.updatePackage(id, updatedPackage));
    }

    // PATCH /api/touristpackages/{id}/status
    // Cambia el estado de un paquete (AVAILABLE, SOLD_OUT, NOT_VALID, CANCELLED)
    @PatchMapping("/{id}/status")

    public ResponseEntity<TouristPackageEntity> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(touristPackageService.updateStatus(id, status));
    }

    // DELETE /api/touristpackages/{id}
    // Elimina físicamente un paquete solo si no tiene reservas asociadas
    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        touristPackageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/touristpackages/{id}/spots
    // Actualiza los cupos disponibles y estado de un paquete.
    // Llamado internamente por reservation-service al crear/cancelar reservas.
    @PatchMapping("/{id}/spots")
    public ResponseEntity<TouristPackageEntity> updateSpots(
            @PathVariable Long id,
            @RequestParam Integer availableSpots,
            @RequestParam String status) {
        return ResponseEntity.ok(
                touristPackageService.updateAvailableSpots(id, availableSpots, status));
    }
}
