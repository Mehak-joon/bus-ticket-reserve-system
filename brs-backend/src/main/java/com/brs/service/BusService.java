package com.brs.service;

import com.brs.dto.BusRequest;
import com.brs.entity.Bus;
import com.brs.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    public List<Bus> getActiveBuses() {
        return busRepository.findByActiveTrue();
    }

    public Bus getBusById(Long id) {
        return busRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found with id: " + id));
    }

    @Transactional
    public Bus createBus(BusRequest request) {
        if (busRepository.existsByBusNumber(request.getBusNumber().trim())) {
            throw new IllegalArgumentException("Bus with registration number " + request.getBusNumber() + " already exists");
        }

        Bus bus = Bus.builder()
                .busNumber(request.getBusNumber().trim().toUpperCase())
                .busName(request.getBusName().trim())
                .busType(request.getBusType())
                .totalSeats(request.getTotalSeats())
                .amenities(request.getAmenities())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        return busRepository.save(bus);
    }

    @Transactional
    public Bus updateBus(Long id, BusRequest request) {
        Bus bus = getBusById(id);

        if (!bus.getBusNumber().equalsIgnoreCase(request.getBusNumber().trim())
                && busRepository.existsByBusNumber(request.getBusNumber().trim())) {
            throw new IllegalArgumentException("Bus with registration number " + request.getBusNumber() + " already exists");
        }

        bus.setBusNumber(request.getBusNumber().trim().toUpperCase());
        bus.setBusName(request.getBusName().trim());
        bus.setBusType(request.getBusType());
        bus.setTotalSeats(request.getTotalSeats());
        bus.setAmenities(request.getAmenities());
        if (request.getActive() != null) {
            bus.setActive(request.getActive());
        }

        return busRepository.save(bus);
    }

    @Transactional
    public void deleteBus(Long id) {
        Bus bus = getBusById(id);
        busRepository.delete(bus);
    }
}
