package com.sanrio.routeservice.route;

import jakarta.validation.constraints.NotBlank;

public record CreateRouteRequest(@NotBlank String routeName, @NotBlank String description) {
}
