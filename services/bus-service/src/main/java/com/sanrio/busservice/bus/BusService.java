package com.sanrio.busservice.bus;

import com.sanrio.busservice.common.BadRequestException;
import com.sanrio.busservice.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusService {
    private final BusRepository busRepository;

    @Transactional
    public BusResponse createBus(CreateBusRequest request) {
        if (busRepository.existsByBusCode(request.busCode())) {
            throw new BadRequestException("Bus code already exists");
        }
        if (busRepository.existsByPlateNumber(request.plateNumber())) {
            throw new BadRequestException("Plate number already exists");
        }
        Bus bus = busRepository.save(Bus.builder().busCode(request.busCode()).plateNumber(request.plateNumber()).routeId(request.routeId()).build());
        return toResponse(bus);
    }

    public List<BusResponse> getBuses() {
        return busRepository.findAll().stream().map(this::toResponse).toList();
    }

    public BusResponse getBus(Long busId) {
        return busRepository.findById(busId).map(this::toResponse).orElseThrow(() -> new ResourceNotFoundException("Bus not found: " + busId));
    }

    private BusResponse toResponse(Bus bus) {
        return new BusResponse(bus.getId(), bus.getBusCode(), bus.getPlateNumber(), bus.getRouteId());
    }
}
