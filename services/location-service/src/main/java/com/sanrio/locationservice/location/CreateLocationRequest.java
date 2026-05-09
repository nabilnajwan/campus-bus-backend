package com.sanrio.locationservice.location;

import jakarta.validation.constraints.NotNull;

public record CreateLocationRequest(@NotNull Long tripId, @NotNull Long busId, @NotNull Double latitude, @NotNull Double longitude) {
}
