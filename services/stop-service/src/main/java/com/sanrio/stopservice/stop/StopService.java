package com.sanrio.stopservice.stop;

import com.sanrio.stopservice.common.BadRequestException;
import com.sanrio.stopservice.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StopService {
    private final StopRepository stopRepository;

    @Transactional
    public StopResponse createStop(CreateStopRequest request) {
        if (stopRepository.existsByRouteIdAndSequenceNo(request.routeId(), request.sequenceNo())) {
            throw new BadRequestException("Sequence number already exists for this route");
        }
        Stop stop = stopRepository.save(Stop.builder()
                .routeId(request.routeId())
                .stopName(request.stopName())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .sequenceNo(request.sequenceNo())
                .build());
        return toResponse(stop);
    }

    public StopResponse getStopById(Long id) {
        Stop stop = stopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + id));
        return toResponse(stop);
    }

    public List<StopResponse> getStopsByRoute(Long routeId) {
        return stopRepository.findByRouteIdOrderBySequenceNoAsc(routeId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public StopResponse updateStop(Long id, UpdateStopRequest request) {
        Stop stop = stopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + id));
        // Ensure new sequence number doesn't conflict with another stop on the same route
        if (stopRepository.existsByRouteIdAndSequenceNoAndIdNot(stop.getRouteId(), request.sequenceNo(), id)) {
            throw new BadRequestException("Sequence number " + request.sequenceNo() + " is already taken on this route");
        }
        stop.setStopName(request.stopName());
        stop.setLatitude(request.latitude());
        stop.setLongitude(request.longitude());
        stop.setSequenceNo(request.sequenceNo());
        return toResponse(stopRepository.save(stop));
    }

    @Transactional
    public void deleteStop(Long id) {
        if (!stopRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stop not found with id: " + id);
        }
        stopRepository.deleteById(id);
    }

    private StopResponse toResponse(Stop stop) {
        return new StopResponse(stop.getId(), stop.getRouteId(), stop.getStopName(), stop.getLatitude(), stop.getLongitude(), stop.getSequenceNo());
    }
}

