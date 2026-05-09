package com.sanrio.stopservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Swagger docs — always public
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        // Students & public can read stops/routes without logging in
                        .requestMatchers(HttpMethod.GET, "/api/routes/*/stops").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stops/*").permitAll()
                        // Only admins can create, update, or delete stops
                        .requestMatchers(HttpMethod.POST, "/api/stops").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/stops/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/stops/*").hasRole("ADMIN")
                        // Every other request must at least be authenticated
                        .anyRequest().authenticated());
        return http.build();
    }
}

