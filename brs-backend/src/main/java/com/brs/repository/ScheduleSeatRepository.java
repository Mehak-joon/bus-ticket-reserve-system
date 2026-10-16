package com.brs.repository;

import com.brs.entity.ScheduleSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleSeatRepository extends JpaRepository<ScheduleSeat, Long> {

    List<ScheduleSeat> findByScheduleIdOrderBySeatNumberAsc(Long scheduleId);

    Optional<ScheduleSeat> findByScheduleIdAndSeatNumber(Long scheduleId, String seatNumber);

    List<ScheduleSeat> findByScheduleIdAndSeatNumberIn(Long scheduleId, List<String> seatNumbers);

    @Query("SELECT COUNT(s) FROM ScheduleSeat s WHERE s.schedule.id = :scheduleId AND s.booked = false AND s.locked = false")
    long countAvailableSeats(@Param("scheduleId") Long scheduleId);

    @Query("SELECT s FROM ScheduleSeat s WHERE s.locked = true AND s.booked = false AND s.lockedAt < :cutoffTime")
    List<ScheduleSeat> findExpiredLocks(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Modifying
    @Query("UPDATE ScheduleSeat s SET s.locked = false, s.lockedAt = null, s.lockedByUser = null " +
           "WHERE s.locked = true AND s.booked = false AND s.lockedAt < :cutoffTime")
    int releaseExpiredLocks(@Param("cutoffTime") LocalDateTime cutoffTime);
}
