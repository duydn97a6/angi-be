package vn.angi.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.angi.analytics.entity.Event;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("SELECT e FROM Event e WHERE e.userId = :userId AND e.eventName = :eventName ORDER BY e.occurredAt DESC")
    List<Event> findByUserIdAndEventName(@Param("userId") UUID userId, @Param("eventName") String eventName);

    @Query("SELECT e FROM Event e WHERE e.occurredAt > :since ORDER BY e.occurredAt DESC")
    List<Event> findRecent(@Param("since") OffsetDateTime since);
}
