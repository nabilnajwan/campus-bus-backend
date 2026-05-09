package com.sanrio.stopservice;

import com.sanrio.stopservice.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class StopServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StopServiceApplication.class, args);
    }
}
