package vn.angi.restaurant.mapper;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import vn.angi.restaurant.dto.DishDto;
import vn.angi.restaurant.dto.RestaurantDto;
import vn.angi.restaurant.entity.Dish;
import vn.angi.restaurant.entity.Restaurant;

import java.util.List;

@Component
public class RestaurantMapper {

    public RestaurantDto toDto(Restaurant restaurant) {
        if (restaurant == null) return null;

        return RestaurantDto.builder()
            .id(restaurant.getId())
            .name(restaurant.getName())
            .slug(restaurant.getSlug())
            .description(restaurant.getDescription())
            .address(restaurant.getAddress())
            .district(restaurant.getDistrict())
            .city(restaurant.getCity())
            .location(toLocationDto(restaurant.getLocation()))
            .primaryCuisine(restaurant.getPrimaryCuisine())
            .priceRange(restaurant.getPriceRange())
            .avgPriceVnd(restaurant.getAvgPriceVnd())
            .openingHours(restaurant.getOpeningHours())
            .avgRating(restaurant.getAvgRating())
            .totalReviews(restaurant.getTotalReviews())
            .coverImageUrl(restaurant.getCoverImageUrl())
            .images(restaurant.getImages())
            .deliveryLinks(toDeliveryLinksDto(restaurant))
            .isVerified(restaurant.getIsVerified())
            .build();
    }

    public RestaurantDto.LocationDto toLocationDto(Point point) {
        if (point == null) return null;
        return RestaurantDto.LocationDto.builder()
            .lat(point.getY())
            .lng(point.getX())
            .build();
    }

    public RestaurantDto.DeliveryLinksDto toDeliveryLinksDto(Restaurant restaurant) {
        return RestaurantDto.DeliveryLinksDto.builder()
            .grabfood(restaurant.getGrabfoodUrl())
            .shopeefood(restaurant.getShopeefoodUrl())
            .baemin(restaurant.getBaeminUrl())
            .build();
    }

    public DishDto toDishDto(Dish dish) {
        if (dish == null) return null;

        return DishDto.builder()
            .id(dish.getId())
            .name(dish.getName())
            .description(dish.getDescription())
            .priceVnd(dish.getPriceVnd())
            .category(dish.getCategory())
            .cuisineTags(dish.getCuisineTags())
            .isVegetarian(dish.getIsVegetarian())
            .isVegan(dish.getIsVegan())
            .isSpicy(dish.getIsSpicy())
            .allergens(dish.getAllergens())
            .avgRating(dish.getAvgRating())
            .orderCount(dish.getOrderCount())
            .imageUrl(dish.getImageUrl())
            .isAvailable(dish.getIsAvailable())
            .build();
    }

    public List<DishDto> toDishDtoList(List<Dish> dishes) {
        return dishes.stream().map(this::toDishDto).toList();
    }
}
