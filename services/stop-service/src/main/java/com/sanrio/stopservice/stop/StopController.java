package com.sanrio.stopservice.stop;

import com.sanrio.stopservice.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StopController {
    private final StopService stopService;

    // Admin: create a new stop
    @PostMapping("/stops")
    public ResponseEntity<ApiResponse<StopResponse>> createStop(@Valid @RequestBody CreateStopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Stop created successfully", stopService.createStop(request)));
    }

    // All authenticated: get a single stop by its ID
    @GetMapping("/stops/{id}")
    public ResponseEntity<ApiResponse<StopResponse>> getStop(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>("Stop retrieved successfully", stopService.getStopById(id)));
    }

    // Student/All: view all stops along a route, ordered by sequence
    @GetMapping("/routes/{routeId}/stops")
    public ResponseEntity<ApiResponse<List<StopResponse>>> getStopsByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(new ApiResponse<>("Stops retrieved successfully", stopService.getStopsByRoute(routeId)));
    }

    // Admin: update a stop's details
    @PutMapping("/stops/{id}")
    public ResponseEntity<ApiResponse<StopResponse>> updateStop(@PathVariable Long id, @Valid @RequestBody UpdateStopRequest request) {
        return ResponseEntity.ok(new ApiResponse<>("Stop updated successfully", stopService.updateStop(id, request)));
    }

    // Admin: delete a stop
    @DeleteMapping("/stops/{id}")
    public ResponseEntity<Void> deleteStop(@PathVariable Long id) {
        stopService.deleteStop(id);
        return ResponseEntity.noContent().build();
    }
}

