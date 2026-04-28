package vn.angi.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.angi.context.dto.ContextSnapshot;
import vn.angi.restaurant.dto.RestaurantDto;
import vn.angi.user.entity.UserPreferences;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleBasedRecommender {

    public List<RecommendationResult> recommend(
        UserPreferences prefs,
        List<?> history,
        ContextSnapshot context,
        List<RestaurantDto> candidates
    ) {
        if (candidates.isEmpty()) {
            return List.of();
        }

        List<RestaurantDto> filtered = filterCandidates(candidates, prefs);

        Restaurant safe = pickSafe(filtered, history);
        Restaurant familiar = pickFamiliar(filtered, history, safe);
        Restaurant discovery = pickDiscovery(filtered, history, safe, familiar);

        return List.of(
            new RecommendationResult(safe.id(), "safe", "Món bạn đã ăn và thích"),
            new RecommendationResult(familiar.id(), "familiar", "Gần giống món quen thuộc của bạn"),
            new RecommendationResult(discovery.id(), "discovery", "Thử món mới xem sao?")
        );
    }

    private List<RestaurantDto> filterCandidates(List<RestaurantDto> candidates, UserPreferences prefs) {
        return candidates.stream()
            .filter(r -> r.avgPrice() >= prefs.getBudgetMin() && r.avgPrice() <= prefs.getBudgetMax())
            .filter(r -> !isExcluded(r, prefs.getExcludedFoods()))
            .collect(Collectors.toList());
    }

    private boolean isExcluded(RestaurantDto restaurant, String[] excludedFoods) {
        if (excludedFoods == null || excludedFoods.length == 0) return false;
        String cuisine = restaurant.cuisine().toLowerCase();
        for (String excluded : excludedFoods) {
            if (cuisine.contains(excluded.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private RestaurantDto pickSafe(List<RestaurantDto> candidates, List<?> history) {
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No candidates available");
        }
        return candidates.get(0);
    }

    private RestaurantDto pickFamiliar(List<RestaurantDto> candidates, List<?> history, RestaurantDto safe) {
        if (candidates.size() < 2) {
            return safe;
        }
        int index = candidates.indexOf(safe);
        return candidates.get((index + 1) % candidates.size());
    }

    private RestaurantDto pickDiscovery(List<RestaurantDto> candidates, List<?> history, RestaurantDto safe, RestaurantDto familiar) {
        if (candidates.size() < 3) {
            return candidates.get(candidates.size() - 1);
        }
        int safeIndex = candidates.indexOf(safe);
        int familiarIndex = candidates.indexOf(familiar);
        for (int i = 0; i < candidates.size(); i++) {
            if (i != safeIndex && i != familiarIndex) {
                return candidates.get(i);
            }
        }
        return candidates.get(0);
    }

    public record RecommendationResult(
        String restaurantId,
        String category,
        String reason
    ) {}
}
