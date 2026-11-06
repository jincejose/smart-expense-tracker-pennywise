package com.pennywise.tracker.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthApiController {
    @GetMapping("/api/health") public Map<String, String> health() { return Map.of("status", "ok", "service", "pennywise"); }
}
