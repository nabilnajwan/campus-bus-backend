package com.sanrio.busservice.bus;

import com.sanrio.busservice.common.ApiResponse;
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
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusController {
    private final BusService busService;

    @PostMapping
    public ResponseEntity<ApiResponse<BusResponse>> createBus(@Valid @RequestBody CreateBusRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("Bus created successfully", busService.createBus(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BusResponse>>> getBuses() {
        return ResponseEntity.ok(new ApiResponse<>("Buses retrieved successfully", busService.getBuses()));
    }

    @GetMapping("/{busId}")
    public ResponseEntity<ApiResponse<BusResponse>> getBus(@PathVariable Long busId) {
        return ResponseEntity.ok(new ApiResponse<>("Bus retrieved successfully", busService.getBus(busId)));
    }
}
