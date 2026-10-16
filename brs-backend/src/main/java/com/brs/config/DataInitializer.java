package com.brs.config;

import com.brs.dto.ScheduleRequest;
import com.brs.entity.*;
import com.brs.repository.*;
import com.brs.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeUsers();
        initializeBusesAndRoutes();
        initializeSchedules();
    }

    private void initializeUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@brs.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .fullName("System Administrator")
                    .phoneNumber("9876543210")
                    .role(Role.ROLE_ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("Initialized default ADMIN user: username='admin', password='Admin@123'");
        }

        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .email("user@brs.com")
                    .passwordHash(passwordEncoder.encode("User@123"))
                    .fullName("Regular Passenger")
                    .phoneNumber("9876543211")
                    .role(Role.ROLE_USER)
                    .active(true)
                    .build();
            userRepository.save(user);
            log.info("Initialized default USER: username='user', password='User@123'");
        }
    }

    private void initializeBusesAndRoutes() {
        if (busRepository.count() == 0) {
            busRepository.save(Bus.builder()
                    .busNumber("DL-01-AB-1234")
                    .busName("Royal Scania Multi-Axle")
                    .busType(BusType.VOLVO_MULTI_AXLE)
                    .totalSeats(32)
                    .amenities("AC, High-Speed WiFi, Mineral Water, Charging Port, Blanket")
                    .active(true)
                    .build());

            busRepository.save(Bus.builder()
                    .busNumber("MH-02-CD-5678")
                    .busName("Orange Sleeper Comfort")
                    .busType(BusType.AC_SLEEPER)
                    .totalSeats(28)
                    .amenities("AC, Sleeper Berths, Reading Light, Pillow")
                    .active(true)
                    .build());

            busRepository.save(Bus.builder()
                    .busNumber("KA-03-EF-9012")
                    .busName("City Express Deluxe")
                    .busType(BusType.AC_SEATER)
                    .totalSeats(40)
                    .amenities("AC, Pushback Seats, USB Charging")
                    .active(true)
                    .build());

            log.info("Initialized sample buses");
        }

        if (routeRepository.count() == 0) {
            routeRepository.save(Route.builder()
                    .sourceCity("Delhi")
                    .destinationCity("Jaipur")
                    .distanceKm(new BigDecimal("280.00"))
                    .estimatedDurationHours(new BigDecimal("5.50"))
                    .active(true)
                    .build());

            routeRepository.save(Route.builder()
                    .sourceCity("Jaipur")
                    .destinationCity("Delhi")
                    .distanceKm(new BigDecimal("280.00"))
                    .estimatedDurationHours(new BigDecimal("5.50"))
                    .active(true)
                    .build());

            routeRepository.save(Route.builder()
                    .sourceCity("Mumbai")
                    .destinationCity("Pune")
                    .distanceKm(new BigDecimal("150.00"))
                    .estimatedDurationHours(new BigDecimal("3.50"))
                    .active(true)
                    .build());

            routeRepository.save(Route.builder()
                    .sourceCity("Bangalore")
                    .destinationCity("Hyderabad")
                    .distanceKm(new BigDecimal("570.00"))
                    .estimatedDurationHours(new BigDecimal("9.00"))
                    .active(true)
                    .build());

            log.info("Initialized sample routes");
        }
    }

    private void initializeSchedules() {
        if (scheduleRepository.count() == 0) {
            List<Bus> buses = busRepository.findAll();
            List<Route> routes = routeRepository.findAll();

            Bus bus1 = buses.get(0); // Scania Multi-Axle
            Bus bus2 = buses.size() > 1 ? buses.get(1) : bus1; // Orange Sleeper
            Bus bus3 = buses.size() > 2 ? buses.get(2) : bus1; // City Express

            LocalDateTime now = LocalDateTime.now();

            // Schedule 1: Today evening (Delhi -> Jaipur)
            ScheduleRequest req1 = new ScheduleRequest();
            req1.setBusId(bus1.getId());
            req1.setRouteId(routes.get(0).getId());
            req1.setDepartureTime(now.plusHours(3));
            req1.setArrivalTime(now.plusHours(8).plusMinutes(30));
            req1.setBaseFare(new BigDecimal("850.00"));
            req1.setStatus(ScheduleStatus.SCHEDULED);
            scheduleService.createSchedule(req1);

            // Schedule 2: Tomorrow morning (Delhi -> Jaipur)
            ScheduleRequest req2 = new ScheduleRequest();
            req2.setBusId(bus1.getId());
            req2.setRouteId(routes.get(0).getId());
            req2.setDepartureTime(now.plusDays(1).withHour(7).withMinute(0));
            req2.setArrivalTime(now.plusDays(1).withHour(12).withMinute(30));
            req2.setBaseFare(new BigDecimal("750.00"));
            req2.setStatus(ScheduleStatus.SCHEDULED);
            scheduleService.createSchedule(req2);

            if (routes.size() > 1) {
                // Schedule 3: Tomorrow afternoon (Jaipur -> Delhi)
                ScheduleRequest req3 = new ScheduleRequest();
                req3.setBusId(bus1.getId());
                req3.setRouteId(routes.get(1).getId());
                req3.setDepartureTime(now.plusDays(1).withHour(14).withMinute(0));
                req3.setArrivalTime(now.plusDays(1).withHour(19).withMinute(30));
                req3.setBaseFare(new BigDecimal("750.00"));
                req3.setStatus(ScheduleStatus.SCHEDULED);
                scheduleService.createSchedule(req3);
            }

            if (routes.size() > 2) {
                // Schedule 4: Tomorrow morning (Mumbai -> Pune)
                ScheduleRequest req4 = new ScheduleRequest();
                req4.setBusId(bus2.getId());
                req4.setRouteId(routes.get(2).getId());
                req4.setDepartureTime(now.plusDays(1).withHour(8).withMinute(30));
                req4.setArrivalTime(now.plusDays(1).withHour(12).withMinute(0));
                req4.setBaseFare(new BigDecimal("550.00"));
                req4.setStatus(ScheduleStatus.SCHEDULED);
                scheduleService.createSchedule(req4);
            }

            if (routes.size() > 3) {
                // Schedule 5: Tomorrow evening (Bangalore -> Hyderabad)
                ScheduleRequest req5 = new ScheduleRequest();
                req5.setBusId(bus3.getId());
                req5.setRouteId(routes.get(3).getId());
                req5.setDepartureTime(now.plusDays(1).withHour(21).withMinute(0));
                req5.setArrivalTime(now.plusDays(2).withHour(6).withMinute(0));
                req5.setBaseFare(new BigDecimal("1150.00"));
                req5.setStatus(ScheduleStatus.SCHEDULED);
                scheduleService.createSchedule(req5);
            }

            log.info("Initialized sample schedules with seat layout across all routes");
        }
    }
}
