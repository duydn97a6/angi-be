package vn.angi.context.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.angi.common.dto.ApiResponse;
import vn.angi.context.dto.WeatherData;
import vn.angi.context.service.ContextService;

@RestController
@RequestMapping("/api/v1/context")
@RequiredArgsConstructor
@Tag(name = "Context", description = "Context aggregation (internal)")
public class ContextController {

    private final ContextService contextService;

    @GetMapping("/weather")
    @Operation(summary = "Get weather data (cached)")
    public ResponseEntity<ApiResponse<WeatherData>> getWeather(
            @RequestParam double lat,
            @RequestParam double lng) {
        WeatherData weather = contextService.getWeather(lat, lng);
        return ResponseEntity.ok(ApiResponse.success(weather));
    }
}
