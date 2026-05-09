package com.sanrio.locationservice.location;

/**
 * DTO for monitoring active SSE connections.
 */
public record SseConnectionMetrics(
        int activeConnections,
        String status
) {}
