package com.sanrio.routeservice.route;

import com.sanrio.routeservice.common.BadRequestException;
import com.sanrio.routeservice.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {
    private final RouteRepository routeRepository;

    @Transactional
    public RouteResponse createRoute(CreateRouteRequest request) {
        if (routeRepository.existsByRouteName(request.routeName())) {
            throw new BadRequestException("Route name already exists");
        }
        Route route = routeRepository.save(Route.builder().routeName(request.routeName()).description(request.description()).build());
        return toResponse(route);
    }

    public List<RouteResponse> getRoutes() {
        return routeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public RouteResponse getRoute(Long routeId) {
        return routeRepository.findById(routeId).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + routeId));
    }

    private RouteResponse toResponse(Route route) {
        return new RouteResponse(route.getId(), route.getRouteName(), route.getDescription());
    }
}
