package vn.angi.event;

import java.util.UUID;

public record MealFeedbackEvent(UUID userId, UUID mealId, String emoji) {}
