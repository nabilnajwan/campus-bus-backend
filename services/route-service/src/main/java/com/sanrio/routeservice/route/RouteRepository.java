package com.sanrio.routeservice.route;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
    boolean existsByRouteName(String routeName);
}
