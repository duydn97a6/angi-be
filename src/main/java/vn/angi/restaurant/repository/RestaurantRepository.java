package vn.angi.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.angi.restaurant.entity.Restaurant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    Optional<Restaurant> findBySlug(String slug);

    @Query(value = """
        SELECT r.*,
               ST_Distance(r.location, ST_MakePoint(:lng, :lat)::geography) as distance
        FROM restaurants r
        WHERE ST_DWithin(
            r.location,
            ST_MakePoint(:lng, :lat)::geography,
            :radiusMeters
        )
        AND r.is_active = true
        AND (:cuisine IS NULL OR r.primary_cuisine = :cuisine)
        AND (:minPrice IS NULL OR r.avg_price_vnd >= :minPrice)
        AND (:maxPrice IS NULL OR r.avg_price_vnd <= :maxPrice)
        ORDER BY distance
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findNearby(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radiusMeters") int radiusMeters,
        @Param("cuisine") String cuisine,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        @Param("limit") int limit
    );

    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.primaryCuisine = :cuisine")
    List<Restaurant> findByCuisine(@Param("cuisine") String cuisine);

    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.city = :city")
    List<Restaurant> findByCity(@Param("city") String city);
}
