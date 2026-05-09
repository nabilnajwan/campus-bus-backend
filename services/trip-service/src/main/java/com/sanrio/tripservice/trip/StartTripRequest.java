package com.sanrio.tripservice.trip;

import jakarta.validation.constraints.NotNull;

public record StartTripRequest(@NotNull Long busId) {
}
