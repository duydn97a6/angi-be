package vn.angi.context.dto;

import lombok.Builder;

@Builder
public record WeatherData(
    double temp,
    double feelsLike,
    int humidity,
    String condition,
    String description,
    String recommendation
) {
    public static WeatherData defaultFor(double lat, double lng) {
        return WeatherData.builder()
            .temp(30.0)
            .feelsLike(32.0)
            .humidity(70)
            .condition("Clear")
            .description("Trời đẹp")
            .recommendation("Thời tiết tốt, ăn gì cũng được")
            .build();
    }
}
