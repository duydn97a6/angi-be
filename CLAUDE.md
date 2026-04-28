# AnGi Backend - Implementation Progress

> Theo dõi tiến độ triển khai backend AnGi (Spring Boot 3.2 + Java 21).
> Cập nhật mỗi khi hoàn tất một phase.

## Tech Stack
- **Java**: 21 (LTS)
- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL 16 + PostGIS (geospatial)
- **Cache**: Redis 7
- **Build**: Maven
- **Migrations**: Flyway

## Project Layout
- `src/main/java/vn/angi/` — code base package
- `src/main/resources/db/migration/` — Flyway SQL migrations
- `docs/` — design docs (vision, schema, API spec, structure, implementation guide)

## Implementation Phases

### Phase 1 — Project setup & build files ✅
- `pom.xml` với toàn bộ dependencies (Spring Boot starters, PostGIS, Flyway, JJWT, MapStruct, Lombok, springdoc, resilience4j, hypersistence-utils)
- `application.yml` + `application-dev.yml` + `application-prod.yml`
- `logback-spring.xml`
- `Dockerfile` (multi-stage build) + `docker-compose.yml` (postgres + redis + backend)
- `.env.example`, `.gitignore`
- `AnGiApplication.java` — main class với `@EnableCaching`, `@EnableAsync`, `@EnableScheduling`

### Phase 2 — Database migrations (Flyway) ✅
- `V1__create_extensions_and_users.sql` — extensions (uuid-ossp, postgis, pg_trgm) + `users` table
- `V2__create_user_preferences.sql`
- `V3__create_restaurants.sql` — với GIST index trên `location` (PostGIS) + GIN trgm trên `name`
- `V4__create_dishes.sql`
- `V5__create_meal_history.sql`
- `V6__create_recommendations.sql`
- `V7__create_auth_tokens.sql`
- `V8__create_events.sql`
- `V9__create_groups_phase2.sql` — `groups`, `group_members`, `lunch_polls`, `poll_votes` (Phase 2 sẵn schema)
### Phase 3 — Common shared infrastructure ✅
- DTOs: `ApiResponse<T>`, `ErrorResponse`, `PageResponse<T>` (đúng response format trong API spec)
- Exceptions: `AppException` (base) + `NotFoundException`, `ValidationException`, `ConflictException`, `UnauthorizedException`, `LlmException`
- `GlobalExceptionHandler` — handle AppException, validation, security exceptions, fallback
- Constants: `ErrorCodes`, `Constants` (recommendation/feedback/meal type)
- Utils: `GeoUtils` (PostGIS Point + haversine), `DateUtils` (meal type detection), `SlugUtils`
### Phase 4 — Configuration classes ✅
- `RedisConfig` — RedisTemplate + RedisCacheManager (cache `weather`, `user-preferences`, `restaurant`, `recommendation` với TTL khác nhau)
- `OpenApiConfig` — Swagger UI + JWT bearer security scheme
- `WebClientConfig` — WebClient với connect/read timeout cho external API
- `AsyncConfig` — ThreadPoolTaskExecutor (4-16 threads) cho `@Async`
- `CorsProperties`, `AppProperties` — typed config từ `application.yml`
- `SecurityConfig` được tạo trong Phase 5 (kèm JwtAuthFilter)
### Phase 5 — Auth module ✅
- Entities: `User` (trong `user/entity` — dùng chung Auth + User), `AuthToken` (refresh token tracker)
- Repos: `UserRepository`, `AuthTokenRepository` (revoke, findByHash)
- Security: `JwtService` (sign/verify HS256), `JwtAuthFilter` (OncePerRequestFilter), `UserPrincipal`, `SecurityConfig` (stateless, permitAll cho `/auth/**`, `/swagger`, GET `/restaurants/**`)
- DTOs: `RegisterRequest`, `LoginRequest`, `GoogleLoginRequest`, `RefreshTokenRequest`, `TokenResponse`, `AuthResponse`
- Services: `AuthService` (register/login/google/refresh/logout, hash refresh token bằng SHA-256), `GoogleOAuthService` (verify Google id_token)
- Controller: `AuthController` mount `/api/v1/auth/{register,login,google,refresh,logout}`
- Event: `UserRegisteredEvent` (publish khi tạo user mới)
### Phase 6 — User module ✅
- Controller: `UserController` với endpoints `/api/v1/users/me` (GET, PATCH), `/api/v1/users/me/preferences` (PUT), `/api/v1/users/me/onboarding/complete` (POST), `/api/v1/users/me` (DELETE)
- Services: `UserService`, `UserPreferencesService` (đã có từ trước)
- DTOs: `UserResponse`, `UpdateUserRequest`, `UpdatePreferencesRequest`, `UserPreferencesDto`
- Mapper: `UserMapper`

### Phase 7 — Restaurant module ✅
- Entities: `Restaurant` (với PostGIS Point), `Dish`
- Repositories: `RestaurantRepository` (với geospatial query `findNearby`), `DishRepository`
- Services: `RestaurantService`, `DishService`
- DTOs: `RestaurantDto`, `DishDto`, `RestaurantSearchRequest`
- Mapper: `RestaurantMapper`
- Controller: `RestaurantController` với endpoints `/api/v1/restaurants` (GET), `/api/v1/restaurants/{id}` (GET), `/api/v1/restaurants/{id}/dishes` (GET)

### Phase 8 — Context module ✅
- Services: `WeatherService`, `TimeContextService`, `ContextService`
- Client: `OpenWeatherClient` (gọi OpenWeatherMap API với caching)
- DTOs: `WeatherData`, `ContextSnapshot`
- Controller: `ContextController` với endpoint `/api/v1/context/weather` (GET)

### Phase 9 — Recommendation module (CORE) ✅
- Entity: `Recommendation`
- Repository: `RecommendationRepository`
- Services: `RecommendationService`, `RecommendationCacheService`, `RuleBasedRecommender`
- LLM: `LlmClient` interface, `ClaudeClient` implementation (gọi Claude API với timeout + fallback)
- DTOs: `RecommendationRequest`, `RecommendationResponse`, `RecommendationItem`
- Controller: `RecommendationController` với endpoints `/api/v1/recommendations` (GET), `/api/v1/recommendations/{id}/click` (POST)

### Phase 10 — Meal / Feedback module ✅
- Entity: `UserMealHistory`
- Repository: `MealHistoryRepository`
- Services: `MealHistoryService`, `FeedbackService`, `MealStatsService`
- DTOs: `MealHistoryDto`, `FeedbackRequest`, `MealStatsDto`
- Controllers: `MealHistoryController` (`/api/v1/meals/history`, `/api/v1/meals/stats`), `FeedbackController` (`/api/v1/feedback`)
- Feedback loop: update `UserPreferences.lovedDishIds` / `dislikedDishIds` dựa trên feedback

### Phase 11 — Analytics + Spring Events ✅
- Entity: `Event`
- Repository: `EventRepository`
- Service: `EventService` (async event tracking)
- Controller: `AnalyticsController` với endpoint `/api/v1/analytics/events` (POST)
- Spring Events: `UserRegisteredEvent`, `UserOnboardedEvent`, `MealFeedbackEvent`, `RecommendationClickedEvent`

## Run locally

```bash
# Start postgres + redis
docker compose up -d postgres redis

# Run app
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

API docs: http://localhost:8080/swagger-ui.html

## Notes
- Mã nguồn theo modular monolith (Auth / User / Restaurant / Recommendation / Context / Meal / Group / Notification / Analytics).
- Mỗi module có `controller`, `service`, `repository`, `entity`, `dto`.
- Dùng constructor injection (`@RequiredArgsConstructor`).
- DTO không trộn với Entity — mapping qua MapStruct.
