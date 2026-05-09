package com.sanrio.locationservice.location;

import java.time.Instant;

/**
 * DTO for aggregated live bus data with route/stop information.
 * Combines real-time location with route context.
 */
public record LiveBusLocationResponse(
        Long busId,
        Long tripId,
        Long driverId,
        Double latitude,
        Double longitude,
        Instant recordedAt,
        Long routeId,
        String routeName
) {
    /**
     * Create from a BusLocation with route info.
     */
    public static LiveBusLocationResponse from(BusLocation location, Long routeId, String routeName) {
        return new LiveBusLocationResponse(
                location.getBusId(),
                location.getTripId(),
                location.getDriverId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getRecordedAt(),
                routeId,
                routeName
        );
    }
}
