package com.sanrio.locationservice.client;

import java.time.Instant;

/**
 * DTO representing a Trip from trip-service.
 * Used for inter-service communication to validate active trips.
 */
public record TripResponse(
        Long id,
        Long busId,
        Long driverId,
        Instant startTime,
        Instant endTime,
        String status
) {}
