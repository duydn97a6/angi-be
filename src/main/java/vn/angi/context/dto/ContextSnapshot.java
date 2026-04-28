package vn.angi.context.dto;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ContextSnapshot(
    WeatherData weather,
    OffsetDateTime time,
    String mealType,
    LocationDto location
) {
    @Builder
    public record LocationDto(
        double lat,
        double lng,
        String district
    ) {}
}
