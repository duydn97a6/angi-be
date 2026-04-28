package vn.angi.meal.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MealStatsDto(
    int totalMeals,
    long totalSpent,
    double avgRating,
    List<CuisineStatDto> topCuisines,
    List<DishStatDto> topDishes,
    HealthPatternDto healthPattern
) {
    @Builder
    public record CuisineStatDto(
        String cuisine,
        int count
    ) {}

    @Builder
    public record DishStatDto(
        String name,
        int count
    ) {}

    @Builder
    public record HealthPatternDto(
        int oilyFoodPercentage,
        String warning
    ) {}
}
