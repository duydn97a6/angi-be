package vn.angi.event;

import java.util.UUID;

public record RecommendationClickedEvent(UUID userId, UUID recommendationId, int recommendationIndex) {}
