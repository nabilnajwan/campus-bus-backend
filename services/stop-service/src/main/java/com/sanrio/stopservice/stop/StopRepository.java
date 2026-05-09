package com.sanrio.stopservice.stop;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long> {
    List<Stop> findByRouteIdOrderBySequenceNoAsc(Long routeId);
    boolean existsByRouteIdAndSequenceNo(Long routeId, Integer sequenceNo);
    boolean existsByRouteIdAndSequenceNoAndIdNot(Long routeId, Integer sequenceNo, Long excludedId);
}
