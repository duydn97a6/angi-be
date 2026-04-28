package vn.angi.context.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.angi.context.client.OpenWeatherClient;
import vn.angi.context.dto.WeatherData;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final OpenWeatherClient openWeatherClient;
    private final RedisTemplate<String, WeatherData> redisTemplate;

    @Value("${app.weather.cache-ttl-seconds:3600}")
    private long cacheTtlSeconds;

    @Cacheable(value = "weather", key = "#lat + ':' + #lng")
    public WeatherData getWeather(double lat, double lng) {
        String cacheKey = String.format("weather:%.2f:%.2f", lat, lng);
        WeatherData cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Weather cache hit for lat={}, lng={}", lat, lng);
            return cached;
        }

        log.debug("Weather cache miss for lat={}, lng={}", lat, lng);
        WeatherData data = openWeatherClient.getWeather(lat, lng);
        redisTemplate.opsForValue().set(cacheKey, data, Duration.ofSeconds(cacheTtlSeconds));
        return data;
    }
}
