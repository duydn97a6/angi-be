package vn.angi.auth.dto;

import java.util.UUID;

public record AuthResponse(UserSummary user, TokenResponse tokens) {
    public record UserSummary(UUID id, String email, String name, String avatarUrl, boolean onboarded) {}
}
