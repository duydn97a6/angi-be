package vn.angi.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.angi.analytics.entity.Event;
import vn.angi.analytics.repository.EventRepository;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    @Async
    public void track(String eventName, Map<String, Object> properties) {
        track(eventName, properties, null, null);
    }

    @Async
    public void track(String eventName, Map<String, Object> properties, UUID userId) {
        track(eventName, properties, userId, null);
    }

    @Async
    public void track(String eventName, Map<String, Object> properties, UUID userId, UUID sessionId) {
        try {
            Event event = Event.builder()
                .userId(userId)
                .sessionId(sessionId)
                .eventName(eventName)
                .eventProperties(properties)
                .platform("web")
                .occurredAt(OffsetDateTime.now())
                .build();
            eventRepository.save(event);
            log.debug("Event tracked: {} for user: {}", eventName, userId);
        } catch (Exception e) {
            log.warn("Failed to track event: {}", eventName, e);
        }
    }
}
