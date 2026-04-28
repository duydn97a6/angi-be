package vn.angi.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import vn.angi.common.exception.LlmException;
import vn.angi.context.dto.ContextSnapshot;
import vn.angi.recommendation.dto.RecommendationItem;
import vn.angi.recommendation.dto.RecommendationRequest;
import vn.angi.recommendation.dto.RecommendationResponse;
import vn.angi.recommendation.entity.Recommendation;
import vn.angi.recommendation.llm.LlmClient;
import vn.angi.recommendation.repository.RecommendationRepository;
import vn.angi.restaurant.dto.RestaurantDto;
import vn.angi.restaurant.service.DishService;
import vn.angi.restaurant.service.RestaurantService;
import vn.angi.user.entity.UserPreferences;
import vn.angi.user.service.UserPreferencesService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final UserPreferencesService preferencesService;
    private final ContextService contextService;
    private final RestaurantService restaurantService;
    private final DishService dishService;
    private final RecommendationCacheService cacheService;
    private final RecommendationRepository recommendationRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    private Optional<LlmClient> llmClient;
    private final RuleBasedRecommender ruleBasedRecommender;

    public RecommendationResponse getRecommendations(UUID userId, RecommendationRequest request) {
        String cacheKey = cacheService.buildCacheKey(userId, request.lat(), request.lng());
        
        if (!request.forceRefresh()) {
            Optional<RecommendationResponse> cached = cacheService.get(cacheKey);
            if (cached.isPresent()) {
                log.info("recommendation.cache_hit userId={}", userId);
                return cached.get();
            }
        }

        long startTime = System.currentTimeMillis();

        CompletableFuture<UserPreferences> prefsF = CompletableFuture.supplyAsync(() ->
            preferencesService.getByUserId(userId)
        );

        CompletableFuture<ContextSnapshot> contextF = CompletableFuture.supplyAsync(() ->
            contextService.buildContext(request.lat(), request.lng())
        );

        CompletableFuture<List<RestaurantDto>> restaurantsF = prefsF.thenCompose(prefs ->
            CompletableFuture.supplyAsync(() ->
                restaurantService.findNearbyForRecommendation(
                    request.lat(), request.lng(), prefs.getSearchRadiusMeters(), prefs
                )
            )
        );

        CompletableFuture.allOf(prefsF, contextF, restaurantsF).join();

        UserPreferences preferences = prefsF.join();
        ContextSnapshot context = contextF.join();
        List<RestaurantDto> candidates = restaurantsF.join();

        List<RecommendationItem> recommendations;
        String method;

        try {
            if (llmClient.isPresent()) {
                var llmResults = llmClient.get().recommend(preferences, List.of(), context, candidates);
                recommendations = buildRecommendationItems(llmResults, candidates);
                method = "llm_" + llmClient.get().getProviderName();
            } else {
                throw new LlmException("LLM client not configured");
            }
        } catch (Exception e) {
            log.warn("LLM failed, falling back to rule-based", e);
            var ruleResults = ruleBasedRecommender.recommend(preferences, List.of(), context, candidates);
            recommendations = buildRecommendationItemsFromRule(ruleResults, candidates);
            method = "rule_based";
        }

        long elapsed = System.currentTimeMillis() - startTime;

        Recommendation record = Recommendation.builder()
            .userId(userId)
            .contextData(serializeContext(context))
            .recommendations(serializeRecommendations(recommendations))
            .generationMethod(method)
            .generationTimeMs((int) elapsed)
            .build();
        recommendationRepo.save(record);

        RecommendationResponse response = RecommendationResponse.builder()
            .recommendationId(record.getId())
            .context(toResponseContext(context))
            .recommendations(recommendations)
            .generationMethod(method)
            .generationTimeMs((int) elapsed)
            .build();

        cacheService.put(cacheKey, response);

        log.info("recommendation.generated userId={} method={} latency_ms={}", userId, method, elapsed);

        return response;
    }

    private List<RecommendationItem> buildRecommendationItems(
        List<LlmClient.RecommendationResult> results,
        List<RestaurantDto> candidates
    ) {
        return results.stream().map(result -> {
            RestaurantDto restaurant = candidates.stream()
                .filter(r -> r.id().toString().equals(result.restaurantId()))
                .findFirst()
                .orElse(null);

            if (restaurant == null) return null;

            return RecommendationItem.builder()
                .category(result.category())
                .restaurant(toItemRestaurant(restaurant))
                .explanation(result.reason())
                .build();
        }).filter(java.util.Objects::nonNull).toList();
    }

    private List<RecommendationItem> buildRecommendationItemsFromRule(
        List<RuleBasedRecommender.RecommendationResult> results,
        List<RestaurantDto> candidates
    ) {
        return results.stream().map(result -> {
            RestaurantDto restaurant = candidates.stream()
                .filter(r -> r.id().toString().equals(result.restaurantId()))
                .findFirst()
                .orElse(null);

            if (restaurant == null) return null;

            return RecommendationItem.builder()
                .category(result.category())
                .restaurant(toItemRestaurant(restaurant))
                .explanation(result.reason())
                .build();
        }).filter(java.util.Objects::nonNull).toList();
    }

    private RecommendationItem.RestaurantDto toItemRestaurant(RestaurantDto r) {
        return RecommendationItem.RestaurantDto.builder()
            .id(r.id())
            .name(r.name())
            .cuisine(r.primaryCuisine())
            .avgPrice(r.avgPriceVnd())
            .distance(0.0)
            .rating(r.avgRating() != null ? r.avgRating().doubleValue() : 0.0)
            .imageUrl(r.coverImageUrl())
            .deliveryLinks(new RecommendationItem.DeliveryLinksDto(
                r.deliveryLinks() != null ? r.deliveryLinks().grabfood() : null,
                r.deliveryLinks() != null ? r.deliveryLinks().shopeefood() : null
            ))
            .build();
    }

    private String serializeContext(ContextSnapshot context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String serializeRecommendations(List<RecommendationItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private RecommendationResponse.ContextSnapshot toResponseContext(ContextSnapshot context) {
        return RecommendationResponse.ContextSnapshot.builder()
            .weather(new RecommendationResponse.WeatherData(
                context.weather().temp(),
                context.weather().condition(),
                context.weather().description()
            ))
            .time(context.time().toString())
            .mealType(context.mealType())
            .location(new RecommendationResponse.LocationDto(
                context.location().lat(),
                context.location().lng()
            ))
            .build();
    }
}
