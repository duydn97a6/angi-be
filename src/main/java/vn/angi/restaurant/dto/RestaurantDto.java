package vn.angi.restaurant.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Builder
public record RestaurantDto(
    UUID id,
    String name,
    String slug,
    String description,
    String address,
    String district,
    String city,
    LocationDto location,
    String primaryCuisine,
    String priceRange,
    Integer avgPriceVnd,
    Map<String, String> openingHours,
    BigDecimal avgRating,
    Integer totalReviews,
    String coverImageUrl,
    String[] images,
    DeliveryLinksDto deliveryLinks,
    Boolean isVerified
) {
    @Builder
    public record LocationDto(
        double lat,
        double lng
    ) {}

    @Builder
    public record DeliveryLinksDto(
        String grabfood,
        String shopeefood,
        String baemin
    ) {}
}
