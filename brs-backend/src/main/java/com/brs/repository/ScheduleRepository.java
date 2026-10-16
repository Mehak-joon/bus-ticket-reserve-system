package com.brs.repository;

import com.brs.entity.Schedule;
import com.brs.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("SELECT s FROM Schedule s " +
           "WHERE (:sourceCity IS NULL OR LOWER(s.route.sourceCity) = LOWER(:sourceCity)) " +
           "AND (:destinationCity IS NULL OR LOWER(s.route.destinationCity) = LOWER(:destinationCity)) " +
           "AND s.departureTime >= :startTime " +
           "AND s.departureTime <= :endTime " +
           "AND s.status = :status " +
           "ORDER BY s.departureTime ASC")
    List<Schedule> searchSchedules(
            @Param("sourceCity") String sourceCity,
            @Param("destinationCity") String destinationCity,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") ScheduleStatus status
    );

    List<Schedule> findByBusId(Long busId);
    List<Schedule> findByRouteId(Long routeId);
    List<Schedule> findByStatus(ScheduleStatus status);
}
