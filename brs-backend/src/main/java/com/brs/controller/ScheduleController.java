package com.brs.controller;

import com.brs.dto.*;
import com.brs.entity.Schedule;
import com.brs.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/schedules/search")
    public ResponseEntity<ApiResponse<List<ScheduleDetailResponse>>> searchSchedules(@ModelAttribute ScheduleSearchRequest searchRequest) {
        List<ScheduleDetailResponse> results = scheduleService.searchSchedules(searchRequest);
        return ResponseEntity.ok(ApiResponse.ok("Schedules retrieved successfully", results));
    }

    @GetMapping("/schedules/{id}/seats")
    public ResponseEntity<ApiResponse<ScheduleDetailResponse>> getSeats(@PathVariable("id") Long id) {
        ScheduleDetailResponse response = scheduleService.getScheduleDetails(id);
        return ResponseEntity.ok(ApiResponse.ok("Seat layout retrieved successfully", response));
    }

    @GetMapping("/schedules/{id}")
    public ResponseEntity<ApiResponse<ScheduleDetailResponse>> getScheduleDetails(@PathVariable("id") Long id) {
        ScheduleDetailResponse response = scheduleService.getScheduleDetails(id);
        return ResponseEntity.ok(ApiResponse.ok("Schedule details retrieved", response));
    }

    @PostMapping("/schedules/lock-seats")
    public ResponseEntity<ApiResponse<SeatLockResponse>> lockSeats(
            @Valid @RequestBody SeatLockRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SeatLockResponse response = scheduleService.lockSeats(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Seats locked successfully for 10 minutes", response));
    }

    @GetMapping("/admin/schedules")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<Schedule>>> getAllSchedulesForAdmin() {
        return ResponseEntity.ok(ApiResponse.ok(scheduleService.getAllSchedules()));
    }

    @PostMapping("/admin/schedules")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Schedule>> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        Schedule schedule = scheduleService.createSchedule(request);
        return ResponseEntity.ok(ApiResponse.ok("Schedule created successfully with seat layout", schedule));
    }

    @DeleteMapping("/admin/schedules/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable("id") Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.ok("Schedule deleted successfully", null));
    }
}
