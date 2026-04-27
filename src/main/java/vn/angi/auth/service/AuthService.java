package vn.angi.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.angi.auth.dto.AuthResponse;
import vn.angi.auth.dto.LoginRequest;
import vn.angi.auth.dto.RegisterRequest;
import vn.angi.auth.dto.TokenResponse;
import vn.angi.auth.entity.AuthToken;
import vn.angi.auth.repository.AuthTokenRepository;
import vn.angi.common.constant.ErrorCodes;
import vn.angi.common.exception.ConflictException;
import vn.angi.common.exception.NotFoundException;
import vn.angi.common.exception.UnauthorizedException;
import vn.angi.event.UserRegisteredEvent;
import vn.angi.user.entity.User;
import vn.angi.user.repository.UserRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleOAuthService googleOAuthService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException(ErrorCodes.EMAIL_ALREADY_EXISTS, "Email đã được sử dụng");
        }
        User user = User.builder()
                .email(req.email().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(req.password()))
                .name(req.name().trim())
                .isActive(true)
                .isEmailVerified(false)
                .isOnboarded(false)
                .build();
        userRepository.save(user);

        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail()));

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException(ErrorCodes.INVALID_CREDENTIALS, "Email hoặc mật khẩu không đúng"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException(ErrorCodes.INVALID_CREDENTIALS, "Email hoặc mật khẩu không đúng");
        }
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException(ErrorCodes.UNAUTHORIZED, "Tài khoản đã bị khoá");
        }

        userRepository.touchLastLogin(user.getId(), OffsetDateTime.now());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {
        GoogleOAuthService.GoogleProfile profile = googleOAuthService.verify(idToken);
        User user = userRepository.findByGoogleId(profile.googleId())
                .or(() -> userRepository.findByEmail(profile.email()))
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(profile.email().toLowerCase().trim())
                            .name(profile.name() != null ? profile.name() : profile.email())
                            .avatarUrl(profile.avatarUrl())
                            .googleId(profile.googleId())
                            .isEmailVerified(true)
                            .isActive(true)
                            .isOnboarded(false)
                            .build();
                    User saved = userRepository.save(newUser);
                    eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId(), saved.getEmail()));
                    return saved;
                });

        if (user.getGoogleId() == null) {
            user.setGoogleId(profile.googleId());
            userRepository.save(user);
        }
        userRepository.touchLastLogin(user.getId(), OffsetDateTime.now());
        return buildAuthResponse(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parseToken(refreshToken);
        } catch (JwtException ex) {
            throw new UnauthorizedException(ErrorCodes.INVALID_TOKEN, "Refresh token không hợp lệ");
        }
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new UnauthorizedException(ErrorCodes.INVALID_TOKEN, "Token không phải refresh");
        }

        String hash = sha256(refreshToken);
        AuthToken stored = authTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException(ErrorCodes.INVALID_TOKEN, "Refresh token không tồn tại"));
        if (stored.getRevokedAt() != null || stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new UnauthorizedException(ErrorCodes.TOKEN_EXPIRED, "Refresh token đã hết hạn");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.USER_NOT_FOUND, "User không tồn tại"));

        stored.setLastUsedAt(OffsetDateTime.now());
        authTokenRepository.save(stored);

        String newAccess = jwtService.generateAccessToken(user.getId(), user.getEmail());
        return new TokenResponse(newAccess, null, jwtService.getAccessTokenTtlSeconds());
    }

    @Transactional
    public void logout(UUID userId) {
        authTokenRepository.revokeAllByUserId(userId, OffsetDateTime.now());
    }

    private AuthResponse buildAuthResponse(User user) {
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refresh = jwtService.generateRefreshToken(user.getId());

        AuthToken token = AuthToken.builder()
                .userId(user.getId())
                .tokenHash(sha256(refresh))
                .expiresAt(OffsetDateTime.now().plusSeconds(jwtService.getRefreshTokenTtlSeconds()))
                .build();
        authTokenRepository.save(token);

        return new AuthResponse(
                new AuthResponse.UserSummary(
                        user.getId(), user.getEmail(), user.getName(),
                        user.getAvatarUrl(), Boolean.TRUE.equals(user.getIsOnboarded())),
                new TokenResponse(access, refresh, jwtService.getAccessTokenTtlSeconds())
        );
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
