package vn.angi.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.angi.common.constant.ErrorCodes;
import vn.angi.common.exception.NotFoundException;
import vn.angi.user.dto.UpdatePreferencesRequest;
import vn.angi.user.dto.UpdateUserRequest;
import vn.angi.user.dto.UserResponse;
import vn.angi.user.entity.User;
import vn.angi.user.entity.UserPreferences;
import vn.angi.user.mapper.UserMapper;
import vn.angi.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferencesService preferencesService;
    private final UserMapper userMapper;

    public UserResponse getCurrent(UUID userId) {
        User user = loadActive(userId);
        UserPreferences prefs = preferencesService.findOrInit(userId);
        return userMapper.toResponse(user, prefs);
    }

    @Transactional
    public UserResponse update(UUID userId, UpdateUserRequest req) {
        User user = loadActive(userId);
        if (req.name() != null) user.setName(req.name().trim());
        if (req.avatarUrl() != null) user.setAvatarUrl(req.avatarUrl());
        if (req.phone() != null) user.setPhone(req.phone());
        userRepository.save(user);
        return userMapper.toResponse(user, preferencesService.findOrInit(userId));
    }

    @Transactional
    public UserResponse updatePreferences(UUID userId, UpdatePreferencesRequest req) {
        User user = loadActive(userId);
        UserPreferences prefs = preferencesService.upsert(userId, req);
        return userMapper.toResponse(user, prefs);
    }

    @Transactional
    public UserResponse completeOnboarding(UUID userId, UpdatePreferencesRequest req) {
        User user = loadActive(userId);
        UserPreferences prefs = preferencesService.upsert(userId, req);
        user.setIsOnboarded(true);
        userRepository.save(user);
        return userMapper.toResponse(user, prefs);
    }

    @Transactional
    public void softDelete(UUID userId) {
        User user = loadActive(userId);
        user.setDeletedAt(OffsetDateTime.now());
        user.setIsActive(false);
        userRepository.save(user);
    }

    public User loadActive(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.USER_NOT_FOUND, "User không tồn tại"));
        if (user.getDeletedAt() != null) {
            throw new NotFoundException(ErrorCodes.USER_NOT_FOUND, "User đã bị xoá");
        }
        return user;
    }
}
