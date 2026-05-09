package com.sanrio.locationservice.location;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

// One row per bus — when a driver pings a new GPS coordinate,
// we UPDATE this row rather than insert a new one (matches the ERD PK design).
@Entity
@Table(name = "bus_locations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusLocation {

    // PK is bus_id — matches the ERD (one live location row per bus)
    @Id
    @Column(name = "bus_id")
    private Long busId;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
}

