package com.sanrio.routeservice.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
