package vn.angi.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.angi.common.constant.ErrorCodes;
import vn.angi.common.exception.UnauthorizedException;
import vn.angi.config.AppProperties;

import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

    private final AppProperties appProperties;
    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    void init() {
        String clientId = appProperties.getGoogle().getOauth().getClientId();
        if (clientId == null || clientId.isBlank()) {
            log.warn("Google OAuth client-id is not configured; /auth/google sẽ không hoạt động.");
            return;
        }
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public GoogleProfile verify(String idToken) {
        if (verifier == null) {
            throw new UnauthorizedException(ErrorCodes.UNAUTHORIZED, "Google OAuth chưa được cấu hình");
        }
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new UnauthorizedException(ErrorCodes.INVALID_TOKEN, "Google ID token không hợp lệ");
            }
            GoogleIdToken.Payload payload = token.getPayload();
            return new GoogleProfile(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name"),
                    (String) payload.get("picture")
            );
        } catch (GeneralSecurityException | java.io.IOException ex) {
            log.error("Google verify error", ex);
            throw new UnauthorizedException(ErrorCodes.INVALID_TOKEN, "Không xác thực được Google token");
        }
    }

    public record GoogleProfile(String googleId, String email, String name, String avatarUrl) {}
}
