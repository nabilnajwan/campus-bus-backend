package com.sanrio.busservice.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
