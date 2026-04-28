package vn.angi.meal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.angi.auth.security.UserPrincipal;
import vn.angi.common.dto.ApiResponse;
import vn.angi.meal.dto.MealHistoryDto;
import vn.angi.meal.dto.MealStatsDto;
import vn.angi.meal.service.MealHistoryService;
import vn.angi.meal.service.MealStatsService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meals")
@RequiredArgsConstructor
@Tag(name = "Meal History", description = "Meal history and statistics")
@SecurityRequirement(name = "bearerAuth")
public class MealHistoryController {

    private final MealHistoryService mealHistoryService;
    private final MealStatsService mealStatsService;

    @GetMapping("/history")
    @Operation(summary = "Get meal history")
    public ResponseEntity<ApiResponse<List<MealHistoryDto>>> getHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "30") int days) {
        List<MealHistoryDto> history = mealHistoryService.getRecentHistory(userPrincipal.getId(), days);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get meal statistics")
    public ResponseEntity<ApiResponse<MealStatsDto>> getStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "month") String period) {
        MealStatsDto stats = mealStatsService.getStats(userPrincipal.getId(), period);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
