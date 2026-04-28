package vn.angi.recommendation.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record RecommendationResponse(
    UUID recommendationId,
    ContextSnapshot context,
    List<RecommendationItem> recommendations,
    String generationMethod,
    Integer generationTimeMs
) {
    @Builder
    public record ContextSnapshot(
        WeatherData weather,
        String time,
        String mealType,
        LocationDto location
    ) {}

    @Builder
    public record WeatherData(
        double temp,
        String condition,
        String description
    ) {}

    @Builder
    public record LocationDto(
        double lat,
        double lng
    ) {}
}
