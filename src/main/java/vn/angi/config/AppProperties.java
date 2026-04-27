package vn.angi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Llm llm = new Llm();
    private Weather weather = new Weather();
    private Google google = new Google();

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenTtl = 900;
        private long refreshTokenTtl = 2592000;
        private String issuer = "angi.vn";
    }

    @Data
    public static class Llm {
        private String provider = "claude";
        private String claudeApiKey;
        private String openaiApiKey;
        private long timeoutMs = 3000;
        private int maxRetries = 2;
    }

    @Data
    public static class Weather {
        private String provider = "openweathermap";
        private String apiKey;
        private long cacheTtlSeconds = 3600;
    }

    @Data
    public static class Google {
        private OAuth oauth = new OAuth();

        @Data
        public static class OAuth {
            private String clientId;
        }
    }
}
