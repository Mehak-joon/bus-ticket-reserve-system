package com.brs.controller;

import com.brs.dto.ApiResponse;
import com.brs.dto.RouteRequest;
import com.brs.entity.Route;
import com.brs.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping("/routes")
    public ResponseEntity<ApiResponse<List<Route>>> getActiveRoutes() {
        return ResponseEntity.ok(ApiResponse.ok(routeService.getActiveRoutes()));
    }

    @GetMapping("/routes/{id}")
    public ResponseEntity<ApiResponse<Route>> getRouteById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(routeService.getRouteById(id)));
    }

    @GetMapping("/routes/cities/sources")
    public ResponseEntity<ApiResponse<List<String>>> getSourceCities() {
        return ResponseEntity.ok(ApiResponse.ok(routeService.getSourceCities()));
    }

    @GetMapping("/routes/cities/destinations")
    public ResponseEntity<ApiResponse<List<String>>> getDestinationCities() {
        return ResponseEntity.ok(ApiResponse.ok(routeService.getDestinationCities()));
    }

    @GetMapping("/admin/routes")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<Route>>> getAllRoutesForAdmin() {
        return ResponseEntity.ok(ApiResponse.ok(routeService.getAllRoutes()));
    }

    @PostMapping("/admin/routes")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Route>> createRoute(@Valid @RequestBody RouteRequest request) {
        Route route = routeService.createRoute(request);
        return ResponseEntity.ok(ApiResponse.ok("Route created successfully", route));
    }

    @PutMapping("/admin/routes/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Route>> updateRoute(@PathVariable("id") Long id, @Valid @RequestBody RouteRequest request) {
        Route route = routeService.updateRoute(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Route updated successfully", route));
    }

    @DeleteMapping("/admin/routes/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable("id") Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.ok(ApiResponse.ok("Route deleted successfully", null));
    }
}
