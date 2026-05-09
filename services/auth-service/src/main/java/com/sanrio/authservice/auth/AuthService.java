package com.sanrio.authservice.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        AuthenticatedUser user = (AuthenticatedUser) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        ).getPrincipal();
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, "Bearer", user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
