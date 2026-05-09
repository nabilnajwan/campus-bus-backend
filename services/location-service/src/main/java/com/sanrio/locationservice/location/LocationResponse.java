package com.sanrio.locationservice.location;

import java.time.Instant;

public record LocationResponse(Long busId, Long tripId, Long driverId, Double latitude, Double longitude, Instant recordedAt) {
}

