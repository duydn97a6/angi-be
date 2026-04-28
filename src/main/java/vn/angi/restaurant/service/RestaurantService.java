package vn.angi.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.angi.common.constant.ErrorCodes;
import vn.angi.common.exception.NotFoundException;
import vn.angi.restaurant.dto.RestaurantDto;
import vn.angi.restaurant.dto.RestaurantSearchRequest;
import vn.angi.restaurant.entity.Restaurant;
import vn.angi.restaurant.entity.UserPreferences;
import vn.angi.restaurant.mapper.RestaurantMapper;
import vn.angi.restaurant.repository.RestaurantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Cacheable(value = "restaurant", key = "#id")
    public RestaurantDto getById(UUID id) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCodes.RESTAURANT_NOT_FOUND, "Restaurant không tồn tại"));
        return restaurantMapper.toDto(restaurant);
    }

    public RestaurantDto getBySlug(String slug) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
            .orElseThrow(() -> new NotFoundException(ErrorCodes.RESTAURANT_NOT_FOUND, "Restaurant không tồn tại"));
        return restaurantMapper.toDto(restaurant);
    }

    public List<RestaurantDto> searchNearby(RestaurantSearchRequest request) {
        List<Object[]> results = restaurantRepository.findNearby(
            request.lat(),
            request.lng(),
            request.radius(),
            request.cuisine(),
            request.minPrice(),
            request.maxPrice(),
            request.size()
        );

        List<RestaurantDto> dtos = new ArrayList<>();
        for (Object[] row : results) {
            Restaurant restaurant = (Restaurant) row[0];
            Double distance = (Double) row[1];
            RestaurantDto dto = restaurantMapper.toDto(restaurant);
            dtos.add(dto);
        }

        return dtos;
    }

    public List<RestaurantDto> findNearbyForRecommendation(
        double lat, double lng, int radiusMeters, UserPreferences prefs
    ) {
        List<Object[]> results = restaurantRepository.findNearby(
            lat, lng, radiusMeters,
            null,
            prefs.getBudgetMin(),
            prefs.getBudgetMax(),
            100
        );

        List<RestaurantDto> dtos = new ArrayList<>();
        for (Object[] row : results) {
            Restaurant restaurant = (Restaurant) row[0];
            dtos.add(restaurantMapper.toDto(restaurant));
        }

        return dtos;
    }

    public Restaurant create(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public Restaurant update(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public void delete(UUID id) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCodes.RESTAURANT_NOT_FOUND, "Restaurant không tồn tại"));
        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
    }
}
