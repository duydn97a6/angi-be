package vn.angi.meal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.angi.meal.dto.MealStatsDto;
import vn.angi.meal.entity.UserMealHistory;
import vn.angi.meal.repository.MealHistoryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealStatsService {

    private final MealHistoryRepository mealHistoryRepository;

    public MealStatsDto getStats(UUID userId, String period) {
        OffsetDateTime since = getSinceDate(period);
        List<UserMealHistory> history = mealHistoryRepository.findRecentByUserId(userId, since);

        int totalMeals = history.size();
        long totalSpent = history.stream()
            .filter(m -> m.getPricePaidVnd() != null)
            .mapToLong(UserMealHistory::getPricePaidVnd)
            .sum();

        double avgRating = history.stream()
            .filter(m -> m.getFeedbackEmoji() != null)
            .mapToDouble(m -> "happy".equals(m.getFeedbackEmoji()) ? 5.0 :
                           "neutral".equals(m.getFeedbackEmoji()) ? 3.0 : 1.0)
            .average()
            .orElse(0.0);

        Map<String, Long> cuisineCounts = history.stream()
            .collect(Collectors.groupingBy(
                m -> m.getRecommendationCategory() != null ? m.getRecommendationCategory() : "unknown",
                Collectors.counting()
            ));

        List<MealStatsDto.CuisineStatDto> topCuisines = cuisineCounts.entrySet().stream()
            .map(e -> new MealStatsDto.CuisineStatDto(e.getKey(), e.getValue().intValue()))
            .sorted((a, b) -> Integer.compare(b.count(), a.count()))
            .limit(5)
            .toList();

        int oilyPercentage = (int) (history.stream()
            .filter(m -> m.getFeedbackTags() != null && Arrays.asList(m.getFeedbackTags()).contains("oily"))
            .count() * 100.0 / Math.max(1, totalMeals));

        String warning = oilyPercentage > 60 ? "Nhiều dầu mỡ" : null;

        return MealStatsDto.builder()
            .totalMeals(totalMeals)
            .totalSpent(totalSpent)
            .avgRating(BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP).doubleValue())
            .topCuisines(topCuisines)
            .topDishes(List.of())
            .healthPattern(new MealStatsDto.HealthPatternDto(oilyPercentage, warning))
            .build();
    }

    private OffsetDateTime getSinceDate(String period) {
        return switch (period) {
            case "week" -> OffsetDateTime.now().minusDays(7);
            case "month" -> OffsetDateTime.now().minusDays(30);
            case "year" -> OffsetDateTime.now().minusDays(365);
            default -> OffsetDateTime.now().minusDays(30);
        };
    }
}
