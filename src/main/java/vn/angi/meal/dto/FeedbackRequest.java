package vn.angi.meal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record FeedbackRequest(
    @NotNull(message = "Recommendation ID is required")
    UUID recommendationId,

    @NotNull(message = "Restaurant ID is required")
    UUID restaurantId,

    UUID dishId,

    @NotBlank(message = "Emoji is required")
    String emoji,

    String regretLevel,

    String[] tags,

    String notes
) {}
