package com.sanrio.stopservice.stop;

public record StopResponse(Long id, Long routeId, String stopName, Double latitude, Double longitude, Integer sequenceNo) {
}
