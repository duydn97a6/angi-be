package vn.angi.event;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId, String email) {}
