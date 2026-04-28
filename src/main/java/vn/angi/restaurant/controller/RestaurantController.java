package vn.angi.restaurant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.angi.common.dto.ApiResponse;
import vn.angi.common.dto.PageResponse;
import vn.angi.restaurant.dto.DishDto;
import vn.angi.restaurant.dto.RestaurantDto;
import vn.angi.restaurant.dto.RestaurantSearchRequest;
import vn.angi.restaurant.service.DishService;
import vn.angi.restaurant.service.RestaurantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant", description = "Restaurant and dish management")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final DishService dishService;

    @GetMapping
    @Operation(summary = "Search restaurants nearby")
    public ResponseEntity<ApiResponse<List<RestaurantDto>>> searchRestaurants(
            @Valid @ModelAttribute RestaurantSearchRequest request) {
        List<RestaurantDto> restaurants = restaurantService.searchNearby(request);
        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant details")
    public ResponseEntity<ApiResponse<RestaurantDto>> getRestaurant(@PathVariable UUID id) {
        RestaurantDto restaurant = restaurantService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(restaurant));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get restaurant by slug")
    public ResponseEntity<ApiResponse<RestaurantDto>> getRestaurantBySlug(@PathVariable String slug) {
        RestaurantDto restaurant = restaurantService.getBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(restaurant));
    }

    @GetMapping("/{id}/dishes")
    @Operation(summary = "Get dishes of a restaurant")
    public ResponseEntity<ApiResponse<List<DishDto>>> getRestaurantDishes(@PathVariable UUID id) {
        List<DishDto> dishes = dishService.getByRestaurantId(id);
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @GetMapping("/dishes/{id}")
    @Operation(summary = "Get dish details")
    public ResponseEntity<ApiResponse<DishDto>> getDish(@PathVariable UUID id) {
        DishDto dish = dishService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(dish));
    }
}
