package vn.angi.context.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.angi.context.dto.ContextSnapshot;
import vn.angi.context.dto.WeatherData;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ContextService {

    private final WeatherService weatherService;
    private final TimeContextService timeContextService;

    public WeatherData getWeather(double lat, double lng) {
        return weatherService.getWeather(lat, lng);
    }

    public ContextSnapshot buildContext(double lat, double lng) {
        WeatherData weather = getWeather(lat, lng);
        String mealType = timeContextService.determineMealType();

        return ContextSnapshot.builder()
            .weather(weather)
            .time(OffsetDateTime.now())
            .mealType(mealType)
            .location(ContextSnapshot.LocationDto.builder()
                .lat(lat)
                .lng(lng)
                .district(null)
                .build())
            .build();
    }
}
