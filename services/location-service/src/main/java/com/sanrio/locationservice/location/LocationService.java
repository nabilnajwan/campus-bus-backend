package com.sanrio.locationservice.location;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanrio.locationservice.client.TripClient;
import com.sanrio.locationservice.client.TripResponse;
import com.sanrio.locationservice.common.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {
    private final BusLocationRepository busLocationRepository;
    private final TripClient tripClient;
    private final LocationEventEmitter locationEventEmitter;

    /**
     * SCRUM-86: Validate that driver has an active trip before accepting GPS ping.
     * SCRUM-87: Auto-link GPS ping to the driver's current active trip.
     * 
     * Driver sends GPS ping — upsert: update if bus already exists, insert if first ping
     */
    @Transactional
    public LocationResponse saveLocation(Long driverId, CreateLocationRequest request) {
        // SCRUM-86: Validate that the trip exists, is active, and belongs to this driver
        TripResponse validTrip = tripClient.getActiveTripForDriver(request.tripId(), driverId);
        
        // SCRUM-87: Verify the bus belongs to this trip
        if (!validTrip.busId().equals(request.busId())) {
            throw new BadRequestException("Bus does not belong to this trip");
        }

        // findById uses bus_id as PK — returns the existing row if present
        BusLocation location = busLocationRepository.findById(request.busId())
                .map(existing -> {
                    // Bus already has a row — just update its coordinates and timestamp
                    // Auto-link to the driver's current trip (could differ from request if trip changed)
                    existing.setTripId(validTrip.id());
                    existing.setDriverId(driverId);
                    existing.setLatitude(request.latitude());
                    existing.setLongitude(request.longitude());
                    existing.setRecordedAt(Instant.now());
                    return existing;
                })
                .orElse(BusLocation.builder()
                        // First GPS ping for this bus — create the row
                        .busId(request.busId())
                        .tripId(validTrip.id())
                        .driverId(driverId)
                        .latitude(request.latitude())
                        .longitude(request.longitude())
                        .recordedAt(Instant.now())
                        .build());

        LocationResponse response = toLocationResponse(busLocationRepository.save(location));
        
        // SCRUM-90: Broadcast location update to all connected students in real-time
        locationEventEmitter.broadcastLocationUpdate(response);
        
        return response;
    }

    // Student use case: get latest location of ALL buses (one row per bus = instant)
    public List<LiveBusResponse> getLiveBuses() {
        return busLocationRepository.findAll().stream()
                .map(this::toLiveBusResponse)
                .toList();
    }

    // Get all bus locations as LocationResponse (used for SSE initial send)
    public List<LocationResponse> getAllLocations() {
        return busLocationRepository.findAll().stream()
                .map(this::toLocationResponse)
                .toList();
    }

    // Admin/Student: see which buses are currently on a specific trip
    public List<LocationResponse> getLocationsByTrip(Long tripId) {
        return busLocationRepository.findByTripId(tripId).stream()
                .map(this::toLocationResponse)
                .toList();
    }

    private LocationResponse toLocationResponse(BusLocation location) {
        return new LocationResponse(
                location.getBusId(),
                location.getTripId(),
                location.getDriverId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getRecordedAt());
    }

    private LiveBusResponse toLiveBusResponse(BusLocation location) {
        return new LiveBusResponse(
                location.getTripId(),
                location.getBusId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getRecordedAt());
    }
}

