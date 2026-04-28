package vn.angi.meal.controller;

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
import vn.angi.meal.dto.FeedbackRequest;
import vn.angi.meal.dto.MealHistoryDto;
import vn.angi.meal.service.FeedbackService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "Meal feedback submission")
@SecurityRequirement(name = "bearerAuth")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "Submit meal feedback")
    public ResponseEntity<ApiResponse<MealHistoryDto>> submitFeedback(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody FeedbackRequest request) {
        MealHistoryDto response = feedbackService.submitFeedback(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending feedback items")
    public ResponseEntity<ApiResponse<Void>> getPending(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
