package com.sanrio.busservice.bus;

public record BusResponse(Long id, String busCode, String plateNumber, Long routeId) {
}
