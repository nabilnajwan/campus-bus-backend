package com.sanrio.locationservice.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
