package vn.angi.meal.dto;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record MealHistoryDto(
    UUID id,
    RestaurantDto restaurant,
    DishDto dish,
    OffsetDateTime mealAt,
    Integer pricePaid,
    FeedbackDto feedback
) {
    @Builder
    public record RestaurantDto(
        UUID id,
        String name,
        String cuisine
    ) {}

    @Builder
    public record DishDto(
        UUID id,
        String name,
        Integer price
    ) {}

    @Builder
    public record FeedbackDto(
        String emoji,
        String regretLevel,
        String[] tags,
        String notes
    ) {}
}
