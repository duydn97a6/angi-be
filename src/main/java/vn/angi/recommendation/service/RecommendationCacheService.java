package vn.angi.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.angi.recommendation.dto.RecommendationResponse;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationCacheService {

    private final RedisTemplate<String, RecommendationResponse> redisTemplate;

    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    public Optional<RecommendationResponse> get(String key) {
        try {
            RecommendationResponse cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("Recommendation cache hit: {}", key);
                return Optional.of(cached);
            }
            log.debug("Recommendation cache miss: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Cache get failed for key: {}", key, e);
            return Optional.empty();
        }
    }

    public void put(String key, RecommendationResponse value) {
        try {
            redisTemplate.opsForValue().set(key, value, CACHE_TTL);
            log.debug("Recommendation cached: {}", key);
        } catch (Exception e) {
            log.warn("Cache put failed for key: {}", key, e);
        }
    }

    public void evict(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Recommendation cache evicted: {}", key);
        } catch (Exception e) {
            log.warn("Cache evict failed for key: {}", key, e);
        }
    }

    public String buildCacheKey(UUID userId, double lat, double lng) {
        int hourBucket = java.time.LocalDateTime.now().getHour();
        return String.format("rec:%s:%d:%.3f:%.3f", userId, hourBucket, lat, lng);
    }
}
