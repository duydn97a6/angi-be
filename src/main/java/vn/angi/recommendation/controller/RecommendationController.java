package vn.angi.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.angi.auth.security.UserPrincipal;
import vn.angi.common.dto.ApiResponse;
import vn.angi.recommendation.dto.RecommendationRequest;
import vn.angi.recommendation.dto.RecommendationResponse;
import vn.angi.recommendation.service.RecommendationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "AI-powered meal recommendations")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "Get 3 meal recommendations")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @ModelAttribute RecommendationRequest request) {
        RecommendationResponse response = recommendationService.getRecommendations(
            userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/click")
    @Operation(summary = "Track recommendation click")
    public ResponseEntity<ApiResponse<Void>> trackClick(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
