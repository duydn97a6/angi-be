package vn.angi.restaurant.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record DishDto(
    UUID id,
    String name,
    String description,
    Integer priceVnd,
    String category,
    String[] cuisineTags,
    Boolean isVegetarian,
    Boolean isVegan,
    Boolean isSpicy,
    String[] allergens,
    BigDecimal avgRating,
    Integer orderCount,
    String imageUrl,
    Boolean isAvailable
) {}
