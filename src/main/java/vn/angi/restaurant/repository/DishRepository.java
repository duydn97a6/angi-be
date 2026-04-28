package vn.angi.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.angi.restaurant.entity.Dish;

import java.util.List;
import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {

    List<Dish> findByRestaurantId(UUID restaurantId);

    @Query("SELECT d FROM Dish d WHERE d.restaurantId = :restaurantId AND d.isAvailable = true")
    List<Dish> findAvailableByRestaurantId(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT d FROM Dish d WHERE d.category = :category AND d.isAvailable = true")
    List<Dish> findByCategory(@Param("category") String category);

    @Query("SELECT d FROM Dish d WHERE d.isSpicy = true AND d.isAvailable = true")
    List<Dish> findSpicyDishes();
}
