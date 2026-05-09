package com.sanrio.stopservice.stop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStopRequest(@NotNull Long routeId, @NotBlank String stopName, @NotNull Double latitude, @NotNull Double longitude, @NotNull Integer sequenceNo) {
}
