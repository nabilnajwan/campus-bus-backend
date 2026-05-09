package com.sanrio.locationservice.location;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.sanrio.locationservice.client.TripClient;
import com.sanrio.locationservice.client.TripResponse;
import com.sanrio.locationservice.common.BadRequestException;
import com.sanrio.locationservice.common.ResourceNotFoundException;

@DisplayName("LocationService Integration Tests")
class LocationServiceTest {

    private LocationService locationService;

    @Mock
    private BusLocationRepository busLocationRepository;

    @Mock
    private TripClient tripClient;

    @Mock
    private LocationEventEmitter locationEventEmitter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        locationService = new LocationService(busLocationRepository, tripClient, locationEventEmitter);
    }

    /**
     * SCRUM-86: Validate that driver has an active trip before accepting GPS ping.
     * Test case: Driver sends GPS ping for valid active trip → Should succeed.
     */
    @Test
    @DisplayName("SCRUM-86: Accept GPS ping when driver has active trip")
    void testSaveLocationWithValidActiveTrip() {
        // Arrange
        Long driverId = 1L;
        Long busId = 100L;
        Long tripId = 50L;
        Double latitude = 40.7128;
        Double longitude = -74.0060;

        CreateLocationRequest request = new CreateLocationRequest(busId, tripId, latitude, longitude);

        TripResponse activeTrip = new TripResponse(
                tripId,
                busId,
                driverId,
                Instant.now().minusSeconds(600),
                null,
                "ACTIVE"
        );

        BusLocation savedLocation = BusLocation.builder()
                .busId(busId)
                .tripId(tripId)
                .driverId(driverId)
                .latitude(latitude)
                .longitude(longitude)
                .recordedAt(Instant.now())
                .build();

        when(tripClient.getActiveTripForDriver(tripId, driverId)).thenReturn(activeTrip);
        when(busLocationRepository.findById(busId)).thenReturn(Optional.empty());
        when(busLocationRepository.save(any(BusLocation.class))).thenReturn(savedLocation);

        // Act
        LocationResponse result = locationService.saveLocation(driverId, request);

        // Assert
        assertNotNull(result);
        assertEquals(busId, result.busId());
        assertEquals(tripId, result.tripId());
        assertEquals(latitude, result.latitude());
        assertEquals(longitude, result.longitude());

        // Verify the trip was validated
        verify(tripClient).getActiveTripForDriver(tripId, driverId);
        // Verify location was saved
        verify(busLocationRepository).save(any(BusLocation.class));
        // Verify broadcast occurred
        verify(locationEventEmitter).broadcastLocationUpdate(any(LocationResponse.class));
    }

    /**
     * SCRUM-86: Validate that driver has an active trip before accepting GPS ping.
     * Test case: Driver sends GPS ping for inactive trip → Should fail.
     */
    @Test
    @DisplayName("SCRUM-86: Reject GPS ping when trip is not ACTIVE")
    void testSaveLocationWithInactiveTrip() {
        // Arrange
        Long driverId = 1L;
        Long busId = 100L;
        Long tripId = 50L;
        Double latitude = 40.7128;
        Double longitude = -74.0060;

        CreateLocationRequest request = new CreateLocationRequest(busId, tripId, latitude, longitude);

        // Simulate trip-service returning an ENDED trip
        when(tripClient.getActiveTripForDriver(tripId, driverId))
                .thenThrow(new ResourceNotFoundException("Trip is not active"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            locationService.saveLocation(driverId, request);
        });

        // Verify no location was saved
        verify(busLocationRepository, never()).save(any(BusLocation.class));
    }

    /**
     * SCRUM-87: Auto-link GPS ping to the driver's current active trip.
     * Test case: Update existing location with new coordinates → Trip ID should be auto-linked.
     */
    @Test
    @DisplayName("SCRUM-87: Auto-link GPS ping to driver's active trip on update")
    void testAutoLinkTripOnLocationUpdate() {
        // Arrange
        Long driverId = 1L;
        Long busId = 100L;
        Long tripId = 50L;
        Double newLatitude = 40.7580;
        Double newLongitude = -73.9855;

        CreateLocationRequest request = new CreateLocationRequest(busId, tripId, newLatitude, newLongitude);

        TripResponse activeTrip = new TripResponse(
                tripId,
                busId,
                driverId,
                Instant.now().minusSeconds(600),
                null,
                "ACTIVE"
        );

        // Existing location with old coordinates
        BusLocation existingLocation = BusLocation.builder()
                .busId(busId)
                .tripId(tripId)
                .driverId(driverId)
                .latitude(40.7000)
                .longitude(-74.0000)
                .recordedAt(Instant.now().minusSeconds(60))
                .build();

        when(tripClient.getActiveTripForDriver(tripId, driverId)).thenReturn(activeTrip);
        when(busLocationRepository.findById(busId)).thenReturn(Optional.of(existingLocation));
        when(busLocationRepository.save(any(BusLocation.class))).thenReturn(existingLocation);

        // Act
        LocationResponse result = locationService.saveLocation(driverId, request);

        // Assert
        assertNotNull(result);
        assertEquals(tripId, result.tripId());
        assertEquals(driverId, result.driverId());

        // Verify the location was updated with new coordinates
        verify(busLocationRepository).save(any(BusLocation.class));
    }

    /**
     * SCRUM-87: Verify bus belongs to trip.
     * Test case: Driver sends GPS ping for bus not in current trip → Should fail.
     */
    @Test
    @DisplayName("SCRUM-87: Reject GPS ping when bus doesn't belong to trip")
    void testRejectLocationWhenBusNotInTrip() {
        // Arrange
        Long driverId = 1L;
        Long wrongBusId = 200L; // Different from trip's bus
        Long tripId = 50L;
        Long correctBusId = 100L;

        CreateLocationRequest request = new CreateLocationRequest(wrongBusId, tripId, 40.7128, -74.0060);

        TripResponse activeTrip = new TripResponse(
                tripId,
                correctBusId, // This bus is assigned to the trip
                driverId,
                Instant.now().minusSeconds(600),
                null,
                "ACTIVE"
        );

        when(tripClient.getActiveTripForDriver(tripId, driverId)).thenReturn(activeTrip);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            locationService.saveLocation(driverId, request);
        });

        // Verify no location was saved
        verify(busLocationRepository, never()).save(any(BusLocation.class));
    }

    /**
     * Test: Get all live bus locations.
     */
    @Test
    @DisplayName("Retrieve all live bus locations")
    void testGetAllLocations() {
        // Arrange
        BusLocation bus1 = BusLocation.builder()
                .busId(1L)
                .tripId(10L)
                .driverId(100L)
                .latitude(40.7128)
                .longitude(-74.0060)
                .recordedAt(Instant.now())
                .build();

        BusLocation bus2 = BusLocation.builder()
                .busId(2L)
                .tripId(11L)
                .driverId(101L)
                .latitude(40.7580)
                .longitude(-73.9855)
                .recordedAt(Instant.now())
                .build();

        when(busLocationRepository.findAll()).thenReturn(java.util.List.of(bus1, bus2));

        // Act
        java.util.List<LocationResponse> result = locationService.getAllLocations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(busLocationRepository).findAll();
    }

    /**
     * Test: Get locations by trip.
     */
    @Test
    @DisplayName("Retrieve locations by trip ID")
    void testGetLocationsByTrip() {
        // Arrange
        Long tripId = 50L;
        BusLocation location1 = BusLocation.builder()
                .busId(1L)
                .tripId(tripId)
                .driverId(100L)
                .latitude(40.7128)
                .longitude(-74.0060)
                .recordedAt(Instant.now())
                .build();

        when(busLocationRepository.findByTripId(tripId)).thenReturn(java.util.List.of(location1));

        // Act
        java.util.List<LocationResponse> result = locationService.getLocationsByTrip(tripId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tripId, result.get(0).tripId());
        verify(busLocationRepository).findByTripId(tripId);
    }
}
