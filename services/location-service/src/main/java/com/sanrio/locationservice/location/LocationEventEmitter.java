package com.sanrio.locationservice.location;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages Server-Sent Events (SSE) connections for real-time bus location updates.
 * Enables students to receive live bus locations via WebSocket/SSE push.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocationEventEmitter {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Register a new SSE connection.
     */
    public SseEmitter registerEmitter() {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 minute timeout
        
        emitter.onCompletion(() -> {
            log.debug("SSE emitter completed");
            emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE emitter timed out");
            emitters.remove(emitter);
        });
        emitter.onError(throwable -> {
            log.error("SSE emitter error: {}", throwable.getMessage());
            emitters.remove(emitter);
        });
        
        emitters.add(emitter);
        log.debug("SSE emitter registered. Total emitters: {}", emitters.size());
        return emitter;
    }

    /**
     * Broadcast live bus location update to all connected students.
     * Called whenever a driver posts a GPS ping.
     */
    public void broadcastLocationUpdate(LocationResponse location) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        for (SseEmitter emitter : emitters) {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(location.busId().toString())
                        .name("busLocationUpdate")
                        .data(location);
                
                emitter.send(event);
            } catch (IOException e) {
                log.debug("Failed to send SSE event to emitter: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }
        
        // Clean up dead emitters
        emitters.removeAll(deadEmitters);
    }

    /**
     * Send all current live bus locations to a newly connected student.
     */
    public void sendInitialLocations(SseEmitter emitter, List<LocationResponse> allLocations) {
        try {
            for (LocationResponse location : allLocations) {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(location.busId().toString())
                        .name("busLocationUpdate")
                        .data(location);
                
                emitter.send(event);
            }
        } catch (IOException e) {
            log.error("Failed to send initial locations: {}", e.getMessage());
            emitters.remove(emitter);
        }
    }

    /**
     * Get count of active connections (for monitoring).
     */
    public int getActiveConnectionCount() {
        return emitters.size();
    }
}
