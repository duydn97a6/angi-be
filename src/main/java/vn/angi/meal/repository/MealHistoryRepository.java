package vn.angi.meal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.angi.meal.entity.UserMealHistory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealHistoryRepository extends JpaRepository<UserMealHistory, UUID> {

    List<UserMealHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT m FROM UserMealHistory m WHERE m.userId = :userId AND m.createdAt > :since ORDER BY m.createdAt DESC")
    List<UserMealHistory> findRecentByUserId(@Param("userId") UUID userId, @Param("since") OffsetDateTime since);

    @Query("SELECT m FROM UserMealHistory m WHERE m.userId = :userId AND m.feedbackEmoji IS NULL ORDER BY m.createdAt DESC")
    List<UserMealHistory> findPendingFeedback(@Param("userId") UUID userId);

    Optional<UserMealHistory> findByIdAndUserId(UUID id, UUID userId);
}
