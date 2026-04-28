package vn.angi.context.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import vn.angi.context.dto.WeatherData;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenWeatherClient {

    private final WebClient webClient;

    @Value("${app.weather.api-key}")
    private String apiKey;

    @Value("${app.weather.timeout-ms:3000}")
    private long timeoutMs;

    public WeatherData getWeather(double lat, double lng) {
        try {
            String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%.4f&lon=%.4f&appid=%s&units=metric&lang=vi",
                lat, lng, apiKey
            );

            Map<?, ?> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

            return parseWeatherResponse(response);
        } catch (Exception e) {
            log.warn("OpenWeather API failed for lat={}, lng={}", lat, lng, e);
            return WeatherData.defaultFor(lat, lng);
        }
    }

    private WeatherData parseWeatherResponse(Map<?, ?> response) {
        Map<?, ?> main = (Map<?, ?>) response.get("main");
        java.util.List<?> weatherList = (java.util.List<?>) response.get("weather");
        Map<?, ?> weatherObj = (Map<?, ?>) weatherList.get(0);

        double temp = ((Number) main.get("temp")).doubleValue();
        double feelsLike = ((Number) main.get("feels_like")).doubleValue();
        int humidity = ((Number) main.get("humidity")).intValue();
        String condition = (String) weatherObj.get("main");
        String description = (String) weatherObj.get("description");

        String recommendation = generateRecommendation(temp, condition);

        return WeatherData.builder()
            .temp(temp)
            .feelsLike(feelsLike)
            .humidity(humidity)
            .condition(condition)
            .description(description)
            .recommendation(recommendation)
            .build();
    }

    private String generateRecommendation(double temp, String condition) {
        if (temp > 32) {
            return "Trời nóng, nên chọn món mát, nhiều nước";
        } else if (temp < 20) {
            return "Trời lạnh, nên chọn món nóng, ấm bụng";
        } else if ("Rain".equalsIgnoreCase(condition) || "Thunderstorm".equalsIgnoreCase(condition)) {
            return "Trời mưa, nên chọn món nóng hoặc đặt giao hàng";
        } else {
            return "Thời tiết tốt, ăn gì cũng được";
        }
    }
}
