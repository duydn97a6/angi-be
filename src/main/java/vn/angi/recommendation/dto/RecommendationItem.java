package vn.angi.recommendation.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record RecommendationItem(
    String category,
    RestaurantDto restaurant,
    DishDto dish,
    String explanation,
    Integer estimatedDeliveryMinutes,
    Boolean isTopPick
) {
    @Builder
    public record RestaurantDto(
        UUID id,
        String name,
        String cuisine,
        Integer avgPrice,
        Double distance,
        Double rating,
        String imageUrl,
        DeliveryLinksDto deliveryLinks
    ) {}

    @Builder
    public record DishDto(
        UUID id,
        String name,
        Integer price,
        String imageUrl
    ) {}

    @Builder
    public record DeliveryLinksDto(
        String grabfood,
        String shopeefood
    ) {}
}
