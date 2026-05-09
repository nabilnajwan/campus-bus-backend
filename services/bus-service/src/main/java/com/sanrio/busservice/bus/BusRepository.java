package com.sanrio.busservice.bus;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRepository extends JpaRepository<Bus, Long> {
    boolean existsByBusCode(String busCode);
    boolean existsByPlateNumber(String plateNumber);
}
