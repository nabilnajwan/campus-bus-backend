package com.sanrio.locationservice.location;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sanrio.locationservice.common.ApiResponse;
import com.sanrio.locationservice.security.JwtPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class LocationController {
    private final LocationService locationService;
    private final LocationEventEmitter locationEventEmitter;

    // Driver: send a GPS ping — updates (or creates) the bus's live location row
    @PostMapping("/locations")
    public ResponseEntity<ApiResponse<LocationResponse>> createLocation(
            Authentication authentication,
            @Valid @RequestBody CreateLocationRequest request) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Location recorded successfully", locationService.saveLocation(principal.userId(), request)));
    }

    // Student: see where ALL buses are right now (one marker per bus on the map)
    @GetMapping("/buses/live")
    public ResponseEntity<ApiResponse<List<LiveBusResponse>>> getLiveBuses() {
        return ResponseEntity.ok(new ApiResponse<>("Live bus locations retrieved successfully", locationService.getLiveBuses()));
    }

    // Admin/Student: see which bus(es) are on a specific trip
    @GetMapping("/trips/{tripId}/locations")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getLocationsByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(new ApiResponse<>("Trip locations retrieved successfully", locationService.getLocationsByTrip(tripId)));
    }

    /**
     * SCRUM-90: Server-Sent Events (SSE) endpoint for real-time location push to students.
     * Students connect to this endpoint and receive live bus location updates as they occur.
     * 
     * Usage: GET /api/locations/stream
     * Returns: EventStream with real-time bus location updates
     */
    @GetMapping(value = "/locations/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLiveLocations() {
        log.debug("Student connected to live location stream");
        
        SseEmitter emitter = locationEventEmitter.registerEmitter();
        
        // Send all current bus locations to the newly connected client
        List<LocationResponse> currentLocations = locationService.getAllLocations();
        locationEventEmitter.sendInitialLocations(emitter, currentLocations);
        
        return emitter;
    }

    /**
     * Get SSE connection metrics (for monitoring/debugging).
     */
    @GetMapping("/locations/stream/metrics")
    public ResponseEntity<SseConnectionMetrics> getStreamMetrics() {
        int activeConnections = locationEventEmitter.getActiveConnectionCount();
        return ResponseEntity.ok(new SseConnectionMetrics(
                activeConnections,
                activeConnections > 0 ? "ACTIVE" : "IDLE"
        ));
    }
}

