package com.sanrio.stopservice.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
