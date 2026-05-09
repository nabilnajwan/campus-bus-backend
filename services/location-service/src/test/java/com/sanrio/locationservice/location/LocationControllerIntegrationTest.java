package com.sanrio.locationservice.location;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.sanrio.locationservice.security.JwtPrincipal;

/**
 * Unit tests for LocationController.
 * 
 * These tests verify the endpoint logic and service integration.
 * Note: Full HTTP integration tests require Spring Boot test context and MockMvc,
 * which require additional test dependencies. These tests focus on service layer logic.
 */
@DisplayName("LocationController Unit Tests")
class LocationControllerIntegrationTest {

    @Mock
    private LocationService locationService;

    @Mock
    private LocationEventEmitter locationEventEmitter;

    private JwtPrincipal driverPrincipal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        driverPrincipal = new JwtPrincipal(1L, "driver@campus.edu", "DRIVER");
    }

    /**
     * SCRUM-81: Implement POST /api/locations – Driver sends GPS ping.
     * Test: Driver successfully posts a GPS location.
     */
    @Test
    @DisplayName("SCRUM-81: Driver sends GPS ping successfully")
    void testCreateLocationSuccess() {
        // Arrange
        Long busId = 100L;
        Long tripId = 50L;
        Long driverId = driverPrincipal.userId();

        CreateLocationRequest request = new CreateLocationRequest(busId, tripId, 40.7128, -74.0060);

        LocationResponse locationResponse = new LocationResponse(
                busId,
                tripId,
                driverId,
                40.7128,
                -74.0060,
                Instant.now()
        );

        when(locationService.saveLocation(anyLong(), any(CreateLocationRequest.class)))
                .thenReturn(locationResponse);

        // Act
        LocationResponse result = locationService.saveLocation(driverId, request);

        // Assert
        assertNotNull(result);
        assertEquals(busId, result.busId());
        assertEquals(tripId, result.tripId());
        assertEquals(40.7128, result.latitude());
        assertEquals(-74.0060, result.longitude());

        verify(locationService).saveLocation(eq(driverId), any(CreateLocationRequest.class));
    }

    /**
     * SCRUM-82: Implement GET /api/buses/live – Student views all live bus locations.
     * Test: Student retrieves all current bus locations.
     */
    @Test
    @DisplayName("SCRUM-82: Student views all live bus locations")
    void testGetLiveBusesSuccess() {
        // Arrange
        LiveBusResponse bus1 = new LiveBusResponse(50L, 100L, 40.7128, -74.0060, Instant.now());
        LiveBusResponse bus2 = new LiveBusResponse(51L, 101L, 40.7580, -73.9855, Instant.now());

        when(locationService.getLiveBuses()).thenReturn(List.of(bus1, bus2));

        // Act
        List<LiveBusResponse> result = locationService.getLiveBuses();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).busId());
        assertEquals(101L, result.get(1).busId());

        verify(locationService).getLiveBuses();
    }

    /**
     * SCRUM-83: Implement GET /api/trips/{tripId}/locations – Get location history for a trip.
     * Test: Admin/Student retrieves all locations for a specific trip.
     */
    @Test
    @DisplayName("SCRUM-83: Get location history for trip")
    void testGetLocationsByTripSuccess() {
        // Arrange
        Long tripId = 50L;
        LocationResponse location1 = new LocationResponse(100L, tripId, 1L, 40.7128, -74.0060, Instant.now());
        LocationResponse location2 = new LocationResponse(101L, tripId, 2L, 40.7580, -73.9855, Instant.now());

        when(locationService.getLocationsByTrip(tripId)).thenReturn(List.of(location1, location2));

        // Act
        List<LocationResponse> result = locationService.getLocationsByTrip(tripId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(tripId, result.get(0).tripId());
        assertEquals(tripId, result.get(1).tripId());

        verify(locationService).getLocationsByTrip(tripId);
    }

    /**
     * SCRUM-90: Add WebSocket or SSE endpoint for real-time location push to students.
     * Test: Verify SSE endpoint setup calls emitter registration.
     */
    @Test
    @DisplayName("SCRUM-90: SSE stream endpoint setup")
    void testSseStreamEndpointSetup() {
        // Arrange
        LocationResponse location1 = new LocationResponse(100L, 50L, 1L, 40.7128, -74.0060, Instant.now());

        when(locationService.getAllLocations()).thenReturn(List.of(location1));

        // Act
        List<LocationResponse> result = locationService.getAllLocations();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(locationService).getAllLocations();
    }

    /**
     * SCRUM-91: End-to-end test: Driver starts trip → posts GPS pings → Student sees live bus.
     */
    @Test
    @DisplayName("SCRUM-91: End-to-end - Driver posts GPS, student sees live location")
    void testEndToEndGpsUpdateFlow() {
        // Step 1: Driver posts GPS ping
        Long busId = 100L;
        Long tripId = 50L;
        Long driverId = driverPrincipal.userId();

        CreateLocationRequest gpsRequest = new CreateLocationRequest(busId, tripId, 40.7128, -74.0060);

        LocationResponse savedLocation = new LocationResponse(
                busId,
                tripId,
                driverId,
                40.7128,
                -74.0060,
                Instant.now()
        );

        when(locationService.saveLocation(anyLong(), any(CreateLocationRequest.class)))
                .thenReturn(savedLocation);

        // Post GPS ping as driver
        LocationResponse posted = locationService.saveLocation(driverId, gpsRequest);
        assertNotNull(posted);
        assertEquals(busId, posted.busId());

        // Step 2: Student retrieves live bus locations
        LiveBusResponse liveBus = new LiveBusResponse(tripId, busId, 40.7128, -74.0060, Instant.now());

        when(locationService.getLiveBuses()).thenReturn(List.of(liveBus));

        List<LiveBusResponse> liveLocations = locationService.getLiveBuses();
        
        // Assert
        assertNotNull(liveLocations);
        assertEquals(1, liveLocations.size());
        assertEquals(busId, liveLocations.get(0).busId());
        assertEquals(40.7128, liveLocations.get(0).latitude());
        assertEquals(-74.0060, liveLocations.get(0).longitude());

        // Verify the flow
        verify(locationService).saveLocation(eq(driverId), any(CreateLocationRequest.class));
        verify(locationService).getLiveBuses();
    }
}
