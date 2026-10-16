package com.brs.controller;

import com.brs.dto.ApiResponse;
import com.brs.dto.BusRequest;
import com.brs.entity.Bus;
import com.brs.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;

    @GetMapping("/buses")
    public ResponseEntity<ApiResponse<List<Bus>>> getActiveBuses() {
        return ResponseEntity.ok(ApiResponse.ok(busService.getActiveBuses()));
    }

    @GetMapping("/buses/{id}")
    public ResponseEntity<ApiResponse<Bus>> getBusById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(busService.getBusById(id)));
    }

    @GetMapping("/admin/buses")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<Bus>>> getAllBusesForAdmin() {
        return ResponseEntity.ok(ApiResponse.ok(busService.getAllBuses()));
    }

    @PostMapping("/admin/buses")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Bus>> createBus(@Valid @RequestBody BusRequest request) {
        Bus bus = busService.createBus(request);
        return ResponseEntity.ok(ApiResponse.ok("Bus created successfully", bus));
    }

    @PutMapping("/admin/buses/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Bus>> updateBus(@PathVariable("id") Long id, @Valid @RequestBody BusRequest request) {
        Bus bus = busService.updateBus(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Bus updated successfully", bus));
    }

    @DeleteMapping("/admin/buses/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBus(@PathVariable("id") Long id) {
        busService.deleteBus(id);
        return ResponseEntity.ok(ApiResponse.ok("Bus deleted successfully", null));
    }
}
