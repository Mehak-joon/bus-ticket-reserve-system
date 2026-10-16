package com.brs.repository;

import com.brs.entity.Ticket;
import com.brs.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    Optional<Ticket> findByBookingId(Long bookingId);
    Optional<Ticket> findByBookingBookingReference(String bookingReference);
}
