package vn.angi.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.angi.auth.security.UserPrincipal;
import vn.angi.common.dto.ApiResponse;
import vn.angi.user.dto.UpdatePreferencesRequest;
import vn.angi.user.dto.UpdateUserRequest;
import vn.angi.user.dto.UserResponse;
import vn.angi.user.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile and preferences management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse response = userService.getCurrent(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.update(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me/preferences")
    @Operation(summary = "Update user preferences")
    public ResponseEntity<ApiResponse<UserResponse>> updatePreferences(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        UserResponse response = userService.updatePreferences(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/me/onboarding/complete")
    @Operation(summary = "Complete onboarding")
    public ResponseEntity<ApiResponse<UserResponse>> completeOnboarding(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        UserResponse response = userService.completeOnboarding(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete current user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        userService.softDelete(userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
