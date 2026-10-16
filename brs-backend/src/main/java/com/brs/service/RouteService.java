package com.brs.service;

import com.brs.dto.RouteRequest;
import com.brs.entity.Route;
import com.brs.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public List<Route> getActiveRoutes() {
        return routeRepository.findByActiveTrue();
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found with id: " + id));
    }

    public List<String> getSourceCities() {
        return routeRepository.findDistinctSourceCities();
    }

    public List<String> getDestinationCities() {
        return routeRepository.findDistinctDestinationCities();
    }

    @Transactional
    public Route createRoute(RouteRequest request) {
        String source = request.getSourceCity().trim();
        String dest = request.getDestinationCity().trim();

        if (source.equalsIgnoreCase(dest)) {
            throw new IllegalArgumentException("Source and destination city cannot be identical");
        }

        if (routeRepository.findBySourceCityIgnoreCaseAndDestinationCityIgnoreCase(source, dest).isPresent()) {
            throw new IllegalArgumentException("Route from " + source + " to " + dest + " already exists");
        }

        Route route = Route.builder()
                .sourceCity(source)
                .destinationCity(dest)
                .distanceKm(request.getDistanceKm())
                .estimatedDurationHours(request.getEstimatedDurationHours())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        return routeRepository.save(route);
    }

    @Transactional
    public Route updateRoute(Long id, RouteRequest request) {
        Route route = getRouteById(id);
        String source = request.getSourceCity().trim();
        String dest = request.getDestinationCity().trim();

        if (source.equalsIgnoreCase(dest)) {
            throw new IllegalArgumentException("Source and destination city cannot be identical");
        }

        routeRepository.findBySourceCityIgnoreCaseAndDestinationCityIgnoreCase(source, dest)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException("Route from " + source + " to " + dest + " already exists");
                    }
                });

        route.setSourceCity(source);
        route.setDestinationCity(dest);
        route.setDistanceKm(request.getDistanceKm());
        route.setEstimatedDurationHours(request.getEstimatedDurationHours());
        if (request.getActive() != null) {
            route.setActive(request.getActive());
        }

        return routeRepository.save(route);
    }

    @Transactional
    public void deleteRoute(Long id) {
        Route route = getRouteById(id);
        routeRepository.delete(route);
    }
}
