package vn.angi.recommendation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "recommendations", indexes = {
    @Index(name = "idx_recommendations_user", columnList = "user_id"),
    @Index(name = "idx_recommendations_method", columnList = "generation_method")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String contextData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String recommendations;

    @Column(name = "generation_method", length = 30)
    private String generationMethod;

    @Column(name = "generation_time_ms")
    private Integer generationTimeMs;

    @Column(name = "llm_tokens_used")
    private Integer llmTokensUsed;

    @Column(name = "clicked_recommendation_index")
    private Integer clickedRecommendationIndex;

    @Column(name = "clicked_at")
    private OffsetDateTime clickedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
