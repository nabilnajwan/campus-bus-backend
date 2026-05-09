package com.sanrio.locationservice.location;

import java.time.Instant;

public record LiveBusResponse(Long tripId, Long busId, Double latitude, Double longitude, Instant recordedAt) {
}
