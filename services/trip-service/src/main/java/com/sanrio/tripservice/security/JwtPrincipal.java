package com.sanrio.tripservice.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
