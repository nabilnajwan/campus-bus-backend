package com.sanrio.locationservice.location;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusLocationRepository extends JpaRepository<BusLocation, Long> {
    // Since bus_id is the PK, findAll() already returns the latest location per bus.
    // This method fetches all location pings associated with a specific active trip.
    List<BusLocation> findByTripId(Long tripId);
}

