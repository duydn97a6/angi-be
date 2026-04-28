package vn.angi.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.angi.common.constant.ErrorCodes;
import vn.angi.common.exception.NotFoundException;
import vn.angi.restaurant.dto.DishDto;
import vn.angi.restaurant.entity.Dish;
import vn.angi.restaurant.mapper.RestaurantMapper;
import vn.angi.restaurant.repository.DishRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishService {

    private final DishRepository dishRepository;
    private final RestaurantMapper restaurantMapper;

    public List<DishDto> getByRestaurantId(UUID restaurantId) {
        List<Dish> dishes = dishRepository.findByRestaurantId(restaurantId);
        return restaurantMapper.toDishDtoList(dishes);
    }

    public List<DishDto> getAvailableByRestaurantId(UUID restaurantId) {
        List<Dish> dishes = dishRepository.findAvailableByRestaurantId(restaurantId);
        return restaurantMapper.toDishDtoList(dishes);
    }

    public DishDto getById(UUID id) {
        Dish dish = dishRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCodes.DISH_NOT_FOUND, "Dish không tồn tại"));
        return restaurantMapper.toDishDto(dish);
    }

    public List<DishDto> findByCategory(String category) {
        List<Dish> dishes = dishRepository.findByCategory(category);
        return restaurantMapper.toDishDtoList(dishes);
    }

    public Dish create(Dish dish) {
        return dishRepository.save(dish);
    }

    public Dish update(Dish dish) {
        return dishRepository.save(dish);
    }

    public void delete(UUID id) {
        Dish dish = dishRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCodes.DISH_NOT_FOUND, "Dish không tồn tại"));
        dish.setIsAvailable(false);
        dishRepository.save(dish);
    }
}
