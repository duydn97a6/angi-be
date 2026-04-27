# 05. Implementation Guide - Hướng dẫn code chi tiết

> Hướng dẫn từng bước implement các module quan trọng. Đây là tài liệu reference khi vibe code.

## 🚀 Week 1-2: Project Setup + Auth Module

### Step 1: Initialize Spring Boot project
```bash
# Dùng Spring Initializr (https://start.spring.io/) hoặc:
mvn archetype:generate -DgroupId=vn.angi -DartifactId=angi-backend
cd angi-backend
```

### Step 2: Setup application.yml

```yaml
# application.yml
spring:
  application:
    name: angi-backend

  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:angi}
    username: ${DB_USER:postgres}
    password: ${DB_PASS:postgres}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false

  flyway:
    enabled: true
    baseline-on-migrate: true

  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    timeout: 2000

app:
  jwt:
    secret: ${JWT_SECRET:change-me-to-random-string-at-least-32-chars-long}
    access-token-ttl: 900
    refresh-token-ttl: 2592000
    issuer: angi.vn

  cors:
    allowed-origins:
      - http://localhost:3000
      - https://angi.vn

  llm:
    provider: ${LLM_PROVIDER:claude}
    claude-api-key: ${CLAUDE_API_KEY:}
    openai-api-key: ${OPENAI_API_KEY:}
    timeout-ms: 3000
    max-retries: 2

  weather:
    provider: openweathermap
    api-key: ${OPENWEATHER_API_KEY:}
    cache-ttl-seconds: 3600

  google:
    oauth:
      client-id: ${GOOGLE_CLIENT_ID:}

server:
  port: ${PORT:8080}
  compression:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: when_authorized
```

### Step 3: Implement JWT Service

```java
// vn/angi/auth/service/JwtService.java
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-ttl}")
    private long accessTokenTtl;

    @Value("${app.jwt.refresh-token-ttl}")
    private long refreshTokenTtl;

    public String generateAccessToken(UUID userId, String email) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenTtl * 1000))
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenTtl * 1000))
                .signWith(getSecretKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
```

### Step 4: Implement Auth Controller

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal user) {
        authService.logout(user.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

### Step 5: Security Config

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/restaurants/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://angi.vn"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

---

## 🏪 Week 3: Restaurant Module

### Restaurant Entity với PostGIS

```java
@Entity
@Table(name = "restaurants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Restaurant {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    private String description;
    private String address;
    private String district;
    private String city;

    @Column(columnDefinition = "geography(Point, 4326)")
    private Point location;  // org.locationtech.jts.geom.Point

    @Column(name = "primary_cuisine")
    private String primaryCuisine;

    @Column(name = "avg_price_vnd")
    private Integer avgPriceVnd;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> openingHours;

    @Column(name = "avg_rating")
    private BigDecimal avgRating;

    @Column(name = "grabfood_url")
    private String grabfoodUrl;

    @Column(name = "shopeefood_url")
    private String shopeefoodUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
```

### Restaurant Repository với geospatial query

```java
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    @Query(value = """
        SELECT r.*,
               ST_Distance(r.location, ST_MakePoint(:lng, :lat)::geography) as distance
        FROM restaurants r
        WHERE ST_DWithin(
            r.location,
            ST_MakePoint(:lng, :lat)::geography,
            :radiusMeters
        )
        AND r.is_active = true
        AND (:cuisine IS NULL OR r.primary_cuisine = :cuisine)
        AND (:maxPrice IS NULL OR r.avg_price_vnd <= :maxPrice)
        ORDER BY distance
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findNearby(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radiusMeters") int radiusMeters,
        @Param("cuisine") String cuisine,
        @Param("maxPrice") Integer maxPrice,
        @Param("limit") int limit
    );
}
```

---

## 🧠 Week 4: Recommendation Module (CORE)

Đây là module quan trọng nhất. Hãy implement cẩn thận.

### RecommendationService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final UserService userService;
    private final UserPreferencesService preferencesService;
    private final ContextService contextService;
    private final RestaurantService restaurantService;
    private final MealHistoryService mealHistoryService;
    private final LlmClient llmClient;
    private final RuleBasedRecommender ruleBasedRecommender;
    private final RecommendationCacheService cacheService;
    private final RecommendationRepository recommendationRepo;
    private final ApplicationEventPublisher eventPublisher;

    public RecommendationResponse getRecommendations(UUID userId, RecommendationRequest request) {
        // Step 1: Try cache first
        String cacheKey = buildCacheKey(userId, request);
        Optional<RecommendationResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent() && !request.isForceRefresh()) {
            log.info("recommendation.cache_hit userId={}", userId);
            return cached.get();
        }

        // Step 2: Gather context in parallel
        long startTime = System.currentTimeMillis();
        CompletableFuture<UserPreferences> prefsF = CompletableFuture.supplyAsync(() ->
            preferencesService.getByUserId(userId)
        );
        CompletableFuture<List<UserMealHistory>> historyF = CompletableFuture.supplyAsync(() ->
            mealHistoryService.getRecentHistory(userId, 30)
        );
        CompletableFuture<WeatherData> weatherF = CompletableFuture.supplyAsync(() ->
            contextService.getWeather(request.getLat(), request.getLng())
        );
        CompletableFuture<List<Restaurant>> restaurantsF = CompletableFuture.supplyAsync(() -> {
            UserPreferences prefs = prefsF.join();
            return restaurantService.findNearby(
                request.getLat(), request.getLng(),
                prefs.getSearchRadiusMeters(), prefs
            );
        });

        CompletableFuture.allOf(prefsF, historyF, weatherF, restaurantsF).join();

        UserPreferences preferences = prefsF.join();
        List<UserMealHistory> history = historyF.join();
        WeatherData weather = weatherF.join();
        List<Restaurant> candidates = restaurantsF.join();

        // Step 3: Build context snapshot
        ContextSnapshot context = ContextSnapshot.builder()
            .weather(weather)
            .time(OffsetDateTime.now())
            .mealType(determineMealType())
            .location(new LatLng(request.getLat(), request.getLng()))
            .build();

        // Step 4: Try LLM first, fallback to rule-based
        List<RecommendationItem> recommendations;
        String method;
        try {
            recommendations = llmClient.recommend(preferences, history, context, candidates);
            method = "llm_" + llmClient.getProviderName();
        } catch (Exception e) {
            log.warn("LLM failed, falling back to rule-based", e);
            recommendations = ruleBasedRecommender.recommend(preferences, history, context, candidates);
            method = "rule_based";
        }

        // Step 5: Enrich with restaurant details
        recommendations = enrichRecommendations(recommendations);

        // Step 6: Save to DB for tracking
        long elapsed = System.currentTimeMillis() - startTime;
        Recommendation record = Recommendation.builder()
            .userId(userId)
            .contextData(context)
            .recommendations(recommendations)
            .generationMethod(method)
            .generationTimeMs((int) elapsed)
            .build();
        recommendationRepo.save(record);

        // Step 7: Build response & cache
        RecommendationResponse response = RecommendationResponse.builder()
            .recommendationId(record.getId())
            .context(context)
            .recommendations(recommendations)
            .generationMethod(method)
            .generationTimeMs((int) elapsed)
            .build();

        cacheService.put(cacheKey, response, Duration.ofMinutes(15));

        log.info("recommendation.generated userId={} method={} latency_ms={}",
            userId, method, elapsed);

        return response;
    }

    private String buildCacheKey(UUID userId, RecommendationRequest request) {
        int hourBucket = LocalDateTime.now().getHour();
        return String.format("rec:%s:%d:%.3f:%.3f",
            userId, hourBucket, request.getLat(), request.getLng());
    }

    private String determineMealType() {
        int hour = LocalTime.now().getHour();
        if (hour >= 6 && hour < 10) return "breakfast";
        if (hour >= 10 && hour < 15) return "lunch";
        if (hour >= 15 && hour < 18) return "snack";
        return "dinner";
    }

    private List<RecommendationItem> enrichRecommendations(List<RecommendationItem> items) {
        // Load full restaurant data, add deep links, etc.
        return items;
    }
}
```

### Rule-Based Recommender (Fallback)

```java
@Service
public class RuleBasedRecommender {

    public List<RecommendationItem> recommend(
        UserPreferences prefs,
        List<UserMealHistory> history,
        ContextSnapshot context,
        List<Restaurant> candidates
    ) {
        // Filter out excluded foods
        List<Restaurant> filtered = candidates.stream()
            .filter(r -> !isExcluded(r, prefs.getExcludedFoods()))
            .toList();

        // Weather-based ranking
        if (context.getWeather().getTemp() > 32) {
            filtered = rankByTag(filtered, List.of("refreshing", "light", "noodle-soup"));
        } else if (context.getWeather().getCondition().equals("rain")) {
            filtered = rankByTag(filtered, List.of("hot", "soup", "delivery-fast"));
        }

        // Pick 3 with 3 different strategies
        Restaurant safe = pickSafe(filtered, history);
        Restaurant familiar = pickFamiliar(filtered, history, safe);
        Restaurant discovery = pickDiscovery(filtered, history, safe, familiar);

        return List.of(
            buildItem(safe, "safe", "Món bạn đã ăn và thích"),
            buildItem(familiar, "familiar", "Gần giống món quen thuộc của bạn"),
            buildItem(discovery, "discovery", "Thử món mới xem sao?")
        );
    }

    private Restaurant pickSafe(List<Restaurant> candidates, List<UserMealHistory> history) {
        // Pick restaurant user had positive feedback before
        Set<UUID> lovedRestaurantIds = history.stream()
            .filter(h -> "happy".equals(h.getFeedbackEmoji()))
            .map(UserMealHistory::getRestaurantId)
            .collect(Collectors.toSet());

        return candidates.stream()
            .filter(r -> lovedRestaurantIds.contains(r.getId()))
            .findFirst()
            .orElse(candidates.get(0));  // Fallback: first
    }

    // ... similar pickFamiliar, pickDiscovery
}
```

### LLM Client Interface

```java
public interface LlmClient {
    String getProviderName();
    List<RecommendationItem> recommend(
        UserPreferences preferences,
        List<UserMealHistory> history,
        ContextSnapshot context,
        List<Restaurant> candidates
    );
}
```

### Claude Client implementation

```java
@Service
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "claude")
@RequiredArgsConstructor
@Slf4j
public class ClaudeClient implements LlmClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final PromptBuilder promptBuilder;

    @Value("${app.llm.claude-api-key}")
    private String apiKey;

    @Override
    public String getProviderName() { return "claude"; }

    @Override
    public List<RecommendationItem> recommend(
        UserPreferences prefs, List<UserMealHistory> history,
        ContextSnapshot context, List<Restaurant> candidates
    ) {
        String prompt = promptBuilder.buildRecommendationPrompt(prefs, history, context, candidates);

        Map<String, Object> request = Map.of(
            "model", "claude-sonnet-4-6",
            "max_tokens", 1024,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            )
        );

        try {
            String response = webClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(3))
                .block();

            return parseResponse(response, candidates);
        } catch (Exception e) {
            log.error("Claude API call failed", e);
            throw new LlmException("Claude API failed", e);
        }
    }

    private List<RecommendationItem> parseResponse(String json, List<Restaurant> candidates) {
        // Parse Claude's JSON response
        // Expected format: [{ "restaurantId": "...", "category": "safe", "reason": "..." }, ...]
        // Full impl in LlmResponseParser
    }
}
```

---

## 💬 Week 5: Feedback Module

### Feedback Service

```java
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final MealHistoryRepository mealHistoryRepo;
    private final UserPreferencesService preferencesService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MealHistoryDto submitFeedback(UUID userId, FeedbackRequest request) {
        UserMealHistory meal = mealHistoryRepo.findByIdAndUserId(request.mealId(), userId)
            .orElseThrow(() -> new NotFoundException("Meal not found"));

        meal.setFeedbackEmoji(request.emoji());
        meal.setRegretLevel(request.regretLevel());
        meal.setFeedbackTags(request.tags());
        meal.setFeedbackNotes(request.notes());
        meal.setFeedbackAt(OffsetDateTime.now());

        mealHistoryRepo.save(meal);

        // Update user preferences based on feedback
        updatePreferencesFromFeedback(userId, meal);

        // Publish event for other modules to react
        eventPublisher.publishEvent(new MealFeedbackEvent(userId, meal.getId(), request.emoji()));

        return toDto(meal);
    }

    private void updatePreferencesFromFeedback(UUID userId, UserMealHistory meal) {
        UserPreferences prefs = preferencesService.getByUserId(userId);

        if ("happy".equals(meal.getFeedbackEmoji()) && "none".equals(meal.getRegretLevel())) {
            // Add to loved dishes
            List<UUID> loved = new ArrayList<>(prefs.getLovedDishIds() != null ?
                Arrays.asList(prefs.getLovedDishIds()) : List.of());
            if (!loved.contains(meal.getDishId())) {
                loved.add(meal.getDishId());
                prefs.setLovedDishIds(loved.toArray(UUID[]::new));
            }
        } else if ("sad".equals(meal.getFeedbackEmoji())) {
            // Add to disliked
            List<UUID> disliked = new ArrayList<>(prefs.getDislikedDishIds() != null ?
                Arrays.asList(prefs.getDislikedDishIds()) : List.of());
            if (!disliked.contains(meal.getDishId())) {
                disliked.add(meal.getDishId());
                prefs.setDislikedDishIds(disliked.toArray(UUID[]::new));
            }
        }

        preferencesService.save(prefs);
    }
}
```

---

## 🌤 Context Service Implementation

### Weather Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WebClient webClient;
    private final RedisTemplate<String, WeatherData> redisTemplate;

    @Value("${app.weather.api-key}")
    private String apiKey;

    @Value("${app.weather.cache-ttl-seconds}")
    private long cacheTtl;

    @Cacheable(value = "weather", key = "#lat + ':' + #lng")
    public WeatherData getWeather(double lat, double lng) {
        String cacheKey = String.format("weather:%.2f:%.2f", lat, lng);
        WeatherData cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;

        try {
            String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%.4f&lon=%.4f&appid=%s&units=metric&lang=vi",
                lat, lng, apiKey
            );

            Map response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(3))
                .block();

            WeatherData data = parseWeatherResponse(response);
            redisTemplate.opsForValue().set(cacheKey, data, Duration.ofSeconds(cacheTtl));
            return data;
        } catch (Exception e) {
            log.warn("Weather API failed, using default", e);
            return WeatherData.defaultFor(lat, lng);
        }
    }

    private WeatherData parseWeatherResponse(Map<?,?> response) {
        Map main = (Map) response.get("main");
        List weatherList = (List) response.get("weather");
        Map weatherObj = (Map) weatherList.get(0);

        return WeatherData.builder()
            .temp(((Number) main.get("temp")).doubleValue())
            .feelsLike(((Number) main.get("feels_like")).doubleValue())
            .humidity(((Number) main.get("humidity")).intValue())
            .condition((String) weatherObj.get("main"))
            .description((String) weatherObj.get("description"))
            .build();
    }
}
```

---

## 📊 Events & Analytics

### Event listener pattern

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MealFeedbackEventListener {

    private final EventService eventService;

    @EventListener
    @Async
    public void handleMealFeedback(MealFeedbackEvent event) {
        log.info("Processing meal feedback event: {}", event);

        // Track analytics
        eventService.track("meal_feedback_submitted", Map.of(
            "userId", event.userId().toString(),
            "emoji", event.emoji(),
            "mealId", event.mealId().toString()
        ));

        // Could also: invalidate cache, update ML model, send notification, etc.
    }
}
```

---

## ✅ Testing strategy

### Unit test example

```java
@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock private UserPreferencesService preferencesService;
    @Mock private ContextService contextService;
    @Mock private LlmClient llmClient;
    @Mock private RuleBasedRecommender ruleBasedRecommender;
    @Mock private RecommendationCacheService cacheService;

    @InjectMocks private RecommendationService recommendationService;

    @Test
    void whenLlmFails_shouldFallbackToRuleBased() {
        // Given
        UUID userId = UUID.randomUUID();
        when(cacheService.get(anyString())).thenReturn(Optional.empty());
        when(llmClient.recommend(any(), any(), any(), any()))
            .thenThrow(new LlmException("API down"));
        when(ruleBasedRecommender.recommend(any(), any(), any(), any()))
            .thenReturn(mockRecommendations());

        // When
        RecommendationResponse response = recommendationService.getRecommendations(userId, mockRequest());

        // Then
        assertThat(response.getGenerationMethod()).isEqualTo("rule_based");
        assertThat(response.getRecommendations()).hasSize(3);
    }
}
```

### Integration test với Testcontainers

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RestaurantRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgis/postgis:16-3.4")
        .withDatabaseName("angi_test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private RestaurantRepository restaurantRepo;

    @Test
    void findNearby_shouldReturnRestaurantsWithinRadius() {
        // Given: Restaurant at Q1 HCM
        // When: Search within 1km from office
        // Then: Should find it
    }
}
```

---

## 🎯 Implementation order (recommended)

1. ✅ Project setup + DB migrations
2. ✅ User + Auth module (register, login, JWT)
3. ✅ User preferences (onboarding)
4. ✅ Restaurant module (seed data + search)
5. ✅ Context service (weather, time)
6. ✅ Recommendation module (rule-based first)
7. ✅ LLM integration (Claude/OpenAI)
8. ✅ Feedback module
9. ✅ Meal history & stats
10. ⏭ Group features (Phase 2)

## 🔧 Useful Maven commands

```bash
# Run locally
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Test
mvn test

# Build JAR
mvn clean package -DskipTests

# Run with Docker Compose
docker-compose up

# DB migration status
mvn flyway:info

# Migrate DB
mvn flyway:migrate
```
