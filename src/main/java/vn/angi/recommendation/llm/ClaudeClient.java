package vn.angi.recommendation.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import vn.angi.common.exception.LlmException;
import vn.angi.context.dto.ContextSnapshot;
import vn.angi.restaurant.dto.RestaurantDto;
import vn.angi.user.entity.UserPreferences;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "claude")
@RequiredArgsConstructor
@Slf4j
public class ClaudeClient implements LlmClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${app.llm.claude-api-key}")
    private String apiKey;

    @Value("${app.llm.timeout-ms:3000}")
    private long timeoutMs;

    @Override
    public String getProviderName() {
        return "claude";
    }

    @Override
    public List<RecommendationResult> recommend(
        UserPreferences prefs,
        List<?> history,
        ContextSnapshot context,
        List<RestaurantDto> candidates
    ) {
        String prompt = buildPrompt(prefs, history, context, candidates);

        try {
            Map<String, Object> request = Map.of(
                "model", "claude-sonnet-4-20250514",
                "max_tokens", 1024,
                "messages", List.of(
                    Map.of("role", "user", "content", prompt)
                )
            );

            String response = webClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

            return parseResponse(response, candidates);
        } catch (Exception e) {
            log.error("Claude API call failed", e);
            throw new LlmException("Claude API failed", e);
        }
    }

    private String buildPrompt(
        UserPreferences prefs,
        List<?> history,
        ContextSnapshot context,
        List<RestaurantDto> candidates
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là AI gợi ý món ăn cho người dùng Việt Nam. ");
        sb.append("Hãy chọn 3 quán ăn từ danh sách dưới đây theo 3 loại:\n");
        sb.append("1. safe: món user đã ăn và thích\n");
        sb.append("2. familiar: biến thể món quen thuộc\n");
        sb.append("3. discovery: món mới hoàn toàn\n\n");

        sb.append("Thông tin user:\n");
        sb.append("- Ngân sách: ").append(prefs.getBudgetMin()).append("-").append(prefs.getBudgetMax()).append(" VND\n");
        sb.append("- Loại ăn: ").append(prefs.getDietType()).append("\n");
        sb.append("- Tránh: ").append(prefs.getExcludedFoods() != null ? String.join(", ", prefs.getExcludedFoods()) : "không có").append("\n\n");

        sb.append("Ngữ cảnh hiện tại:\n");
        sb.append("- Thời tiết: ").append(context.weather().description()).append(", ").append(context.weather().temp()).append("°C\n");
        sb.append("- Bữa: ").append(context.mealType()).append("\n\n");

        sb.append("Danh sách quán (id, name, cuisine, price):\n");
        for (RestaurantDto r : candidates) {
            sb.append("- ").append(r.id()).append(": ").append(r.name())
              .append(" (").append(r.cuisine()).append(", ").append(r.avgPrice()).append(" VND)\n");
        }

        sb.append("\nTrả về JSON format:\n");
        sb.append("[{\"restaurantId\": \"uuid\", \"category\": \"safe|familiar|discovery\", \"reason\": \"lý do ngắn gọn\"}, ...]");

        return sb.toString();
    }

    private List<RecommendationResult> parseResponse(String json, List<RestaurantDto> candidates) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode content = root.path("content").get(0);
            String text = content.path("text").asText();

            JsonNode results = objectMapper.readTree(text);
            List<RecommendationResult> list = new ArrayList<>();

            for (JsonNode item : results) {
                list.add(new RecommendationResult(
                    item.get("restaurantId").asText(),
                    item.get("category").asText(),
                    item.get("reason").asText()
                ));
            }

            return list;
        } catch (Exception e) {
            log.error("Failed to parse Claude response", e);
            throw new LlmException("Failed to parse LLM response", e);
        }
    }
}
