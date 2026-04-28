package vn.angi.meal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.angi.common.constant.ErrorCodes;
import vn.angi.common.exception.NotFoundException;
import vn.angi.meal.dto.MealHistoryDto;
import vn.angi.meal.entity.UserMealHistory;
import vn.angi.meal.repository.MealHistoryRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealHistoryService {

    private final MealHistoryRepository mealHistoryRepository;

    public List<MealHistoryDto> getRecentHistory(UUID userId, int days) {
        OffsetDateTime since = OffsetDateTime.now().minusDays(days);
        List<UserMealHistory> history = mealHistoryRepository.findRecentByUserId(userId, since);
        return history.stream().map(this::toDto).toList();
    }

    public List<MealHistoryDto> getHistory(UUID userId) {
        List<UserMealHistory> history = mealHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return history.stream().map(this::toDto).toList();
    }

    public List<UserMealHistory> getPendingFeedback(UUID userId) {
        return mealHistoryRepository.findPendingFeedback(userId);
    }

    public UserMealHistory getByIdAndUserId(UUID id, UUID userId) {
        return mealHistoryRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new NotFoundException(ErrorCodes.MEAL_NOT_FOUND, "Meal history không tồn tại"));
    }

    public UserMealHistory create(UserMealHistory meal) {
        return mealHistoryRepository.save(meal);
    }

    public UserMealHistory update(UserMealHistory meal) {
        return mealHistoryRepository.save(meal);
    }

    private MealHistoryDto toDto(UserMealHistory meal) {
        return MealHistoryDto.builder()
            .id(meal.getId())
            .restaurant(MealHistoryDto.RestaurantDto.builder()
                .id(meal.getRestaurantId())
                .name(null)
                .cuisine(null)
                .build())
            .dish(meal.getDishId() != null ? MealHistoryDto.DishDto.builder()
                .id(meal.getDishId())
                .name(null)
                .price(null)
                .build() : null)
            .mealAt(meal.getMealAt())
            .pricePaid(meal.getPricePaidVnd())
            .feedback(meal.getFeedbackEmoji() != null ? MealHistoryDto.FeedbackDto.builder()
                .emoji(meal.getFeedbackEmoji())
                .regretLevel(meal.getRegretLevel())
                .tags(meal.getFeedbackTags())
                .notes(meal.getFeedbackNotes())
                .build() : null)
            .build();
    }
}
