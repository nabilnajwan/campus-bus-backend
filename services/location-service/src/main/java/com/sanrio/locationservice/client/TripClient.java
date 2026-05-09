package com.sanrio.locationservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sanrio.locationservice.common.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Client for inter-service communication with trip-service.
 * Validates driver's active trips and retrieves trip details.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TripClient {
    private final RestTemplate restTemplate;

    @Value("${services.trip-service.url:http://trip-service:8080}")
    private String tripServiceUrl;

    /**
     * Get a trip by ID and verify it belongs to the given driver and is ACTIVE.
     * @param tripId the trip ID
     * @param driverId the driver ID
     * @return the trip response if valid
     * @throws ResourceNotFoundException if trip not found or doesn't belong to driver
     */
    public TripResponse getActiveTripForDriver(Long tripId, Long driverId) {
        try {
            String url = tripServiceUrl + "/api/trips/" + tripId;
            log.debug("Fetching trip {} from {}", tripId, url);
            
            TripResponse trip = restTemplate.getForObject(url, TripResponse.class);
            
            if (trip == null) {
                throw new ResourceNotFoundException("Trip not found with id: " + tripId);
            }
            
            // Verify trip belongs to this driver
            if (!trip.driverId().equals(driverId)) {
                log.warn("Driver {} attempted to ping location for trip {} that belongs to driver {}",
                        driverId, tripId, trip.driverId());
                throw new ResourceNotFoundException("Trip does not belong to this driver");
            }
            
            // Verify trip is ACTIVE
            if (!"ACTIVE".equalsIgnoreCase(trip.status())) {
                log.warn("Driver {} attempted to ping location for inactive trip {} (status: {})",
                        driverId, tripId, trip.status());
                throw new ResourceNotFoundException("Trip is not active");
            }
            
            return trip;
        } catch (RestClientException e) {
            log.error("Failed to fetch trip {} from trip-service: {}", tripId, e.getMessage());
            throw new ResourceNotFoundException("Unable to validate trip - trip-service unavailable");
        }
    }

    /**
     * Get active trip for a driver (fetches their current ACTIVE trip).
     * @param driverId the driver ID
     * @return the active trip for this driver
     * @throws ResourceNotFoundException if no active trip found
     */
    public TripResponse getActiveTrip(Long driverId) {
        try {
            // This endpoint would need to exist in trip-service
            // For now, we rely on the controller passing the tripId
            throw new UnsupportedOperationException("Use getActiveTripForDriver with tripId instead");
        } catch (RestClientException e) {
            log.error("Failed to fetch active trip for driver {}: {}", driverId, e.getMessage());
            throw new ResourceNotFoundException("No active trip found for driver");
        }
    }
}
