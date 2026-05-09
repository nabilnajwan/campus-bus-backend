package com.sanrio.authservice.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedUsers() {
        return args -> {
            if (!userRepository.existsByEmail("admin@campusbus.com")) {
                userRepository.save(User.builder()
                        .name("System Admin")
                        .email("admin@campusbus.com")
                        .password(passwordEncoder.encode("Admin123!"))
                        .role(Role.ADMIN)
                        .build());
            }
            if (!userRepository.existsByEmail("driver@campusbus.com")) {
                userRepository.save(User.builder()
                        .name("Campus Driver")
                        .email("driver@campusbus.com")
                        .password(passwordEncoder.encode("Driver123!"))
                        .role(Role.DRIVER)
                        .build());
            }
        };
    }
}
