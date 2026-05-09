package com.sanrio.tripservice.trip;

import com.sanrio.tripservice.common.BadRequestException;
import com.sanrio.tripservice.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {
    private final TripRepository tripRepository;

    @Transactional
    public TripResponse startTrip(Long driverId, StartTripRequest request) {
        if (tripRepository.existsByBusIdAndStatus(request.busId(), TripStatus.ACTIVE)) {
            throw new BadRequestException("This bus already has an active trip");
        }
        if (tripRepository.existsByDriverIdAndStatus(driverId, TripStatus.ACTIVE)) {
            throw new BadRequestException("This driver already has an active trip");
        }
        Trip trip = tripRepository.save(Trip.builder()
                .busId(request.busId())
                .driverId(driverId)
                .startTime(Instant.now())
                .status(TripStatus.ACTIVE)
                .build());
        return toResponse(trip);
    }

    public TripResponse getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));
        return toResponse(trip);
    }

    // Admin use case: monitor all currently running trips
    public List<TripResponse> getActiveTrips() {
        return tripRepository.findByStatus(TripStatus.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TripResponse endTrip(Long tripId, Long driverId) {
        // findByIdAndDriverId ensures a driver cannot end someone else's trip
        Trip trip = tripRepository.findByIdAndDriverId(tripId, driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Active trip not found or you are not the assigned driver"));

        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new BadRequestException("This trip has already been completed");
        }

        trip.setStatus(TripStatus.COMPLETED);
        trip.setEndTime(Instant.now());

        return toResponse(tripRepository.save(trip));
    }

    private TripResponse toResponse(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getBusId(),
                trip.getDriverId(),
                trip.getStartTime(),
                trip.getEndTime(),
                trip.getStatus().name());
    }
}

