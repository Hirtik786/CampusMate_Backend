package com.campusmate.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health/details")
    public String home() {
        return "Backend is running 🚀";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    // Add debug endpoints to test connectivity
    @GetMapping("/debug/actuator-test")
    public ResponseEntity<Map<String, Object>> actuatorTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "If you can see this, the app is running");
        response.put("timestamp", System.currentTimeMillis());
        response.put("expectedHealthPath", "/actuator/health/liveness");
        return ResponseEntity.ok(response);
    }
}