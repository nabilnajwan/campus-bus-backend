package com.sanrio.tripservice.trip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    boolean existsByBusIdAndStatus(Long busId, TripStatus status);
    boolean existsByDriverIdAndStatus(Long driverId, TripStatus status);
    // Used by Admin to monitor all active trips
    List<Trip> findByStatus(TripStatus status);
    // Used by Driver to end their own trip — ensures the driver owns this trip
    Optional<Trip> findByIdAndDriverId(Long id, Long driverId);
}

