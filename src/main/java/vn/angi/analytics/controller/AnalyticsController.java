package vn.angi.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.angi.common.dto.ApiResponse;
import vn.angi.analytics.service.EventService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Event tracking (internal)")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final EventService eventService;

    @PostMapping("/events")
    @Operation(summary = "Track custom event")
    public ResponseEntity<ApiResponse<Void>> trackEvent(
            @RequestBody Map<String, Object> request) {
        String eventName = (String) request.get("eventName");
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) request.get("properties");
        eventService.track(eventName, properties);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
