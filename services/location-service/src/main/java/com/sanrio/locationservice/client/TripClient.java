package com.sanrio.locationservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sanrio.locationservice.common.ApiResponse;
import com.sanrio.locationservice.common.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripClient {
    private final RestTemplate restTemplate;

    @Value("${services.trip-service.url:http://trip-service:8080}")
    private String tripServiceUrl;

    public TripResponse getActiveTripForDriver(Long tripId, Long driverId) {
        try {
            String url = tripServiceUrl + "/api/trips/" + tripId;
            log.debug("Fetching trip {} from {}", tripId, url);
            
            // FIX: We must tell RestTemplate to unwrap the ApiResponse<TripResponse>
            ParameterizedTypeReference<ApiResponse<TripResponse>> responseType = 
                    new ParameterizedTypeReference<ApiResponse<TripResponse>>() {};
                    
            ResponseEntity<ApiResponse<TripResponse>> response = 
                    restTemplate.exchange(url, HttpMethod.GET, null, responseType);
            
            // Get the actual trip data out of the wrapper
            TripResponse trip = response.getBody() != null ? response.getBody().data() : null;
            
            if (trip == null || trip.id() == null) {
                throw new ResourceNotFoundException("Trip not found with id: " + tripId);
            }
            
            if (!trip.driverId().equals(driverId)) {
                throw new ResourceNotFoundException("Trip does not belong to this driver");
            }
            
            if (!"ACTIVE".equalsIgnoreCase(trip.status())) {
                throw new ResourceNotFoundException("Trip is not active");
            }
            
            return trip;
        } catch (RestClientException e) {
            log.error("Failed to fetch trip {} from trip-service: {}", tripId, e.getMessage());
            throw new ResourceNotFoundException("Unable to validate trip - trip-service unavailable");
        }
    }

    public TripResponse getActiveTrip(Long driverId) {
        throw new ResourceNotFoundException("Use getActiveTripForDriver with tripId instead");
    }
}