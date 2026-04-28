package vn.angi.meal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_meal_history", indexes = {
    @Index(name = "idx_meal_history_user", columnList = "user_id, created_at DESC"),
    @Index(name = "idx_meal_history_user_restaurant", columnList = "user_id, restaurant_id"),
    @Index(name = "idx_meal_history_feedback", columnList = "user_id, feedback_emoji")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMealHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "dish_id")
    private UUID dishId;

    @Column(name = "recommendation_id")
    private UUID recommendationId;

    @Column(name = "recommendation_category", length = 20)
    private String recommendationCategory;

    @Column(name = "meal_type", length = 20)
    private String mealType;

    @Column(name = "meal_at")
    private OffsetDateTime mealAt;

    @Column(name = "price_paid_vnd")
    private Integer pricePaidVnd;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String weatherData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String locationData;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "hour_of_day")
    private Integer hourOfDay;

    @Column(name = "feedback_emoji", length = 10)
    private String feedbackEmoji;

    @Column(name = "regret_level", length = 10)
    private String regretLevel;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] feedbackTags;

    @Column(columnDefinition = "TEXT")
    private String feedbackNotes;

    @Column(name = "feedback_at")
    private OffsetDateTime feedbackAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
