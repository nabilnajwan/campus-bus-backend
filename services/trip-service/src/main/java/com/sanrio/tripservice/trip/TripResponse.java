package com.sanrio.tripservice.trip;

import java.time.Instant;

public record TripResponse(Long id, Long busId, Long driverId, Instant startTime, Instant endTime, String status) {
}

