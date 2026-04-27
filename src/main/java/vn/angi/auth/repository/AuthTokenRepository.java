package vn.angi.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.angi.auth.entity.AuthToken;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

    Optional<AuthToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE AuthToken t SET t.revokedAt = :ts WHERE t.userId = :userId AND t.revokedAt IS NULL")
    int revokeAllByUserId(@Param("userId") UUID userId, @Param("ts") OffsetDateTime ts);

    @Modifying
    @Query("UPDATE AuthToken t SET t.revokedAt = :ts WHERE t.tokenHash = :hash")
    int revokeByHash(@Param("hash") String hash, @Param("ts") OffsetDateTime ts);
}
