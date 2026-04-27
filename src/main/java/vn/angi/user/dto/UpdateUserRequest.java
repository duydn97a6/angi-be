package vn.angi.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 100) String name,
        String avatarUrl,
        @Size(max = 20) String phone
) {}
