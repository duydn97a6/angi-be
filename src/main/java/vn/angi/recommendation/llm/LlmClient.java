package vn.angi.recommendation.llm;

import vn.angi.context.dto.ContextSnapshot;
import vn.angi.restaurant.dto.RestaurantDto;
import vn.angi.user.entity.UserPreferences;

import java.util.List;

public interface LlmClient {
    String getProviderName();
    List<RecommendationResult> recommend(
        UserPreferences preferences,
        List<?> history,
        ContextSnapshot context,
        List<RestaurantDto> candidates
    );

    record RecommendationResult(
        String restaurantId,
        String category,
        String reason
    ) {}
}
