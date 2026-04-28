package vn.angi.meal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.angi.common.constant.ErrorCodes;
import vn.angi.common.exception.NotFoundException;
import vn.angi.event.MealFeedbackEvent;
import vn.angi.meal.dto.FeedbackRequest;
import vn.angi.meal.dto.MealHistoryDto;
import vn.angi.meal.entity.UserMealHistory;
import vn.angi.meal.repository.MealHistoryRepository;
import vn.angi.user.entity.UserPreferences;
import vn.angi.user.service.UserPreferencesService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final MealHistoryRepository mealHistoryRepository;
    private final UserPreferencesService preferencesService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MealHistoryDto submitFeedback(UUID userId, FeedbackRequest request) {
        UserMealHistory meal = mealHistoryRepository.findByIdAndUserId(request.recommendationId(), userId)
            .orElseThrow(() -> new NotFoundException(ErrorCodes.MEAL_NOT_FOUND, "Meal history không tồn tại"));

        meal.setFeedbackEmoji(request.emoji());
        meal.setRegretLevel(request.regretLevel());
        meal.setFeedbackTags(request.tags());
        meal.setFeedbackNotes(request.notes());
        meal.setFeedbackAt(OffsetDateTime.now());

        mealHistoryRepository.save(meal);

        updatePreferencesFromFeedback(userId, meal);

        eventPublisher.publishEvent(new MealFeedbackEvent(userId, meal.getId(), request.emoji()));

        log.info("Feedback submitted userId={} mealId={} emoji={}", userId, meal.getId(), request.emoji());

        return toDto(meal);
    }

    private void updatePreferencesFromFeedback(UUID userId, UserMealHistory meal) {
        UserPreferences prefs = preferencesService.findOrInit(userId);

        if ("happy".equals(meal.getFeedbackEmoji()) && "none".equals(meal.getRegretLevel())) {
            java.util.List<UUID> loved = new ArrayList<>(prefs.getLovedDishIds() != null ?
                Arrays.asList(prefs.getLovedDishIds()) : java.util.List.of());
            if (meal.getDishId() != null && !loved.contains(meal.getDishId())) {
                loved.add(meal.getDishId());
                prefs.setLovedDishIds(loved.toArray(UUID[]::new));
            }
        } else if ("sad".equals(meal.getFeedbackEmoji())) {
            java.util.List<UUID> disliked = new ArrayList<>(prefs.getDislikedDishIds() != null ?
                Arrays.asList(prefs.getDislikedDishIds()) : java.util.List.of());
            if (meal.getDishId() != null && !disliked.contains(meal.getDishId())) {
                disliked.add(meal.getDishId());
                prefs.setDislikedDishIds(disliked.toArray(UUID[]::new));
            }
        }

        preferencesService.save(prefs);
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
