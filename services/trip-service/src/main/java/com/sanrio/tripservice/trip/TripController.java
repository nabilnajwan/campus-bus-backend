package com.sanrio.tripservice.trip;

import com.sanrio.tripservice.common.ApiResponse;
import com.sanrio.tripservice.security.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;

    // Driver: start a new trip — driverId comes from JWT, not request body
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<TripResponse>> startTrip(
            Authentication authentication,
            @Valid @RequestBody StartTripRequest request) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Trip started successfully", tripService.startTrip(principal.userId(), request)));
    }

    // Driver: end their own trip — verifies ownership inside the service
    @PutMapping("/{id}/end")
    public ResponseEntity<ApiResponse<TripResponse>> endTrip(
            Authentication authentication,
            @PathVariable Long id) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(new ApiResponse<>("Trip ended successfully", tripService.endTrip(id, principal.userId())));
    }

    // Admin & Driver: get details for a specific trip
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponse>> getTripById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>("Trip retrieved successfully", tripService.getTripById(id)));
    }

    // Admin: monitor all currently active trips
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TripResponse>>> getActiveTrips() {
        return ResponseEntity.ok(new ApiResponse<>("Active trips retrieved successfully", tripService.getActiveTrips()));
    }
}

