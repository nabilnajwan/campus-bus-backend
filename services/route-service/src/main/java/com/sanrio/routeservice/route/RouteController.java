package com.sanrio.routeservice.route;

import com.sanrio.routeservice.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("Route created successfully", routeService.createRoute(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getRoutes() {
        return ResponseEntity.ok(new ApiResponse<>("Routes retrieved successfully", routeService.getRoutes()));
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<ApiResponse<RouteResponse>> getRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(new ApiResponse<>("Route retrieved successfully", routeService.getRoute(routeId)));
    }
}
