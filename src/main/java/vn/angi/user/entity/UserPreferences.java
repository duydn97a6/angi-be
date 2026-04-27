package vn.angi.user.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    private String region;

    @Column(name = "office_lat", precision = 10, scale = 7)
    private BigDecimal officeLat;

    @Column(name = "office_lng", precision = 10, scale = 7)
    private BigDecimal officeLng;

    @Column(name = "office_address")
    private String officeAddress;

    @Column(name = "search_radius_meters")
    @Builder.Default
    private Integer searchRadiusMeters = 1000;

    @Column(name = "diet_type")
    @Builder.Default
    private String dietType = "normal";

    @Type(ListArrayType.class)
    @Column(name = "excluded_foods", columnDefinition = "text[]")
    private List<String> excludedFoods;

    @Type(ListArrayType.class)
    @Column(name = "favorite_cuisines", columnDefinition = "text[]")
    private List<String> favoriteCuisines;

    @Column(name = "budget_min")
    @Builder.Default
    private Integer budgetMin = 30000;

    @Column(name = "budget_max")
    @Builder.Default
    private Integer budgetMax = 80000;

    @Column(name = "prefers_delivery")
    @Builder.Default
    private Boolean prefersDelivery = false;

    @Column(name = "max_delivery_time_min")
    @Builder.Default
    private Integer maxDeliveryTimeMin = 30;

    @Type(UUIDArrayType.class)
    @Column(name = "loved_dish_ids", columnDefinition = "uuid[]")
    private UUID[] lovedDishIds;

    @Type(UUIDArrayType.class)
    @Column(name = "disliked_dish_ids", columnDefinition = "uuid[]")
    private UUID[] dislikedDishIds;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
