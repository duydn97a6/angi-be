package vn.angi.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        UUID id,
        String email,
        String name,
        String avatarUrl,
        String phone,
        boolean isOnboarded,
        UserPreferencesDto preferences
) {}
