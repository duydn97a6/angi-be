# 04. Backend Project Structure

## 📁 Cấu trúc thư mục

```
angi-backend/
├── pom.xml                              (hoặc build.gradle)
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── .gitignore
├── README.md
│
├── src/
│   ├── main/
│   │   ├── java/vn/angi/
│   │   │   ├── AnGiApplication.java    (Main class)
│   │   │   │
│   │   │   ├── config/                  (Configuration)
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── WebClientConfig.java
│   │   │   │   └── AsyncConfig.java
│   │   │   │
│   │   │   ├── common/                  (Shared)
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ApiResponse.java
│   │   │   │   │   ├── PageResponse.java
│   │   │   │   │   └── ErrorResponse.java
│   │   │   │   ├── exception/
│   │   │   │   │   ├── AppException.java
│   │   │   │   │   ├── NotFoundException.java
│   │   │   │   │   ├── ValidationException.java
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   ├── util/
│   │   │   │   │   ├── DateUtils.java
│   │   │   │   │   ├── GeoUtils.java
│   │   │   │   │   └── IdGenerator.java
│   │   │   │   └── constant/
│   │   │   │       ├── ErrorCodes.java
│   │   │   │       └── Constants.java
│   │   │   │
│   │   │   ├── auth/                    (Auth module)
│   │   │   │   ├── controller/
│   │   │   │   │   └── AuthController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── JwtService.java
│   │   │   │   │   └── GoogleOAuthService.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   └── TokenResponse.java
│   │   │   │   ├── entity/
│   │   │   │   │   └── AuthToken.java
│   │   │   │   ├── repository/
│   │   │   │   │   └── AuthTokenRepository.java
│   │   │   │   └── security/
│   │   │   │       ├── JwtAuthFilter.java
│   │   │   │       ├── JwtProvider.java
│   │   │   │       └── UserPrincipal.java
│   │   │   │
│   │   │   ├── user/                    (User module)
│   │   │   │   ├── controller/
│   │   │   │   │   └── UserController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   └── UserPreferencesService.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── UserResponse.java
│   │   │   │   │   ├── UpdateUserRequest.java
│   │   │   │   │   └── UserPreferencesDto.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── User.java
│   │   │   │   │   └── UserPreferences.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   └── UserPreferencesRepository.java
│   │   │   │   └── mapper/
│   │   │   │       └── UserMapper.java
│   │   │   │
│   │   │   ├── restaurant/              (Restaurant module)
│   │   │   │   ├── controller/
│   │   │   │   │   └── RestaurantController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── RestaurantService.java
│   │   │   │   │   └── DishService.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── RestaurantDto.java
│   │   │   │   │   ├── RestaurantSearchRequest.java
│   │   │   │   │   └── DishDto.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Restaurant.java
│   │   │   │   │   └── Dish.java
│   │   │   │   └── repository/
│   │   │   │       ├── RestaurantRepository.java
│   │   │   │       └── DishRepository.java
│   │   │   │
│   │   │   ├── recommendation/          (Recommendation module - CORE)
│   │   │   │   ├── controller/
│   │   │   │   │   └── RecommendationController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── RecommendationService.java
│   │   │   │   │   ├── RecommendationCacheService.java
│   │   │   │   │   ├── RuleBasedRecommender.java
│   │   │   │   │   └── RecommendationEnricher.java
│   │   │   │   ├── llm/
│   │   │   │   │   ├── LlmClient.java          (interface)
│   │   │   │   │   ├── ClaudeClient.java
│   │   │   │   │   ├── OpenAiClient.java
│   │   │   │   │   ├── PromptBuilder.java
│   │   │   │   │   └── LlmResponseParser.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── RecommendationRequest.java
│   │   │   │   │   ├── RecommendationResponse.java
│   │   │   │   │   └── RecommendationItem.java
│   │   │   │   ├── entity/
│   │   │   │   │   └── Recommendation.java
│   │   │   │   └── repository/
│   │   │   │       └── RecommendationRepository.java
│   │   │   │
│   │   │   ├── context/                 (Context aggregation)
│   │   │   │   ├── service/
│   │   │   │   │   ├── ContextService.java
│   │   │   │   │   ├── WeatherService.java
│   │   │   │   │   └── TimeContextService.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ContextSnapshot.java
│   │   │   │   │   └── WeatherData.java
│   │   │   │   └── client/
│   │   │   │       └── OpenWeatherClient.java
│   │   │   │
│   │   │   ├── meal/                    (Meal history & feedback)
│   │   │   │   ├── controller/
│   │   │   │   │   ├── MealHistoryController.java
│   │   │   │   │   └── FeedbackController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── MealHistoryService.java
│   │   │   │   │   ├── FeedbackService.java
│   │   │   │   │   └── MealStatsService.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── MealHistoryDto.java
│   │   │   │   │   ├── FeedbackRequest.java
│   │   │   │   │   └── MealStatsDto.java
│   │   │   │   ├── entity/
│   │   │   │   │   └── UserMealHistory.java
│   │   │   │   └── repository/
│   │   │   │       └── MealHistoryRepository.java
│   │   │   │
│   │   │   ├── group/                   (Phase 2 - Groups)
│   │   │   │   ├── controller/
│   │   │   │   │   ├── GroupController.java
│   │   │   │   │   └── PollController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── GroupService.java
│   │   │   │   │   ├── PollService.java
│   │   │   │   │   └── FairnessService.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Group.java
│   │   │   │   │   ├── GroupMember.java
│   │   │   │   │   ├── LunchPoll.java
│   │   │   │   │   └── PollVote.java
│   │   │   │   └── repository/
│   │   │   │       ├── GroupRepository.java
│   │   │   │       ├── GroupMemberRepository.java
│   │   │   │       ├── LunchPollRepository.java
│   │   │   │       └── PollVoteRepository.java
│   │   │   │
│   │   │   ├── notification/            (Phase 2 - Notifications)
│   │   │   │   ├── service/
│   │   │   │   │   ├── NotificationService.java
│   │   │   │   │   └── PushNotificationClient.java
│   │   │   │   └── scheduler/
│   │   │   │       └── FeedbackReminderJob.java
│   │   │   │
│   │   │   ├── analytics/               (Event tracking)
│   │   │   │   ├── controller/
│   │   │   │   │   └── AnalyticsController.java
│   │   │   │   ├── service/
│   │   │   │   │   └── EventService.java
│   │   │   │   ├── entity/
│   │   │   │   │   └── Event.java
│   │   │   │   └── repository/
│   │   │   │       └── EventRepository.java
│   │   │   │
│   │   │   └── event/                   (Spring Events)
│   │   │       ├── UserOnboardedEvent.java
│   │   │       ├── MealFeedbackEvent.java
│   │   │       └── RecommendationClickedEvent.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── logback-spring.xml
│   │       └── db/migration/
│   │           ├── V1__create_users.sql
│   │           ├── V2__create_user_preferences.sql
│   │           ├── V3__create_restaurants.sql
│   │           ├── V4__create_dishes.sql
│   │           ├── V5__create_meal_history.sql
│   │           ├── V6__create_recommendations.sql
│   │           ├── V7__create_auth_tokens.sql
│   │           ├── V8__create_events.sql
│   │           └── V9__seed_restaurants.sql
│   │
│   └── test/
│       └── java/vn/angi/
│           ├── auth/
│           ├── user/
│           ├── restaurant/
│           ├── recommendation/
│           └── integration/
│
└── docs/
    └── postman-collection.json
```

## 📦 Maven dependencies (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>vn.angi</groupId>
    <artifactId>angi-backend</artifactId>
    <version>0.1.0</version>
    <name>AnGi Backend</name>

    <properties>
        <java.version>21</java.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
    </properties>

    <dependencies>
        <!-- Spring Boot starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
            <!-- For WebClient (calling external APIs) -->
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>

        <!-- Hibernate Spatial (for PostGIS) -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-spatial</artifactId>
        </dependency>

        <!-- Flyway migrations -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Google OAuth -->
        <dependency>
            <groupId>com.google.api-client</groupId>
            <artifactId>google-api-client</artifactId>
            <version>2.2.0</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>

        <!-- OpenAPI / Swagger -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>

        <!-- Resilience4j (circuit breaker) -->
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot3</artifactId>
            <version>2.1.0</version>
        </dependency>

        <!-- Micrometer for Prometheus -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.github.tomakehurst</groupId>
            <artifactId>wiremock-standalone</artifactId>
            <version>3.0.1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 🎯 Package naming convention

- Base package: `vn.angi`
- Module packages: `vn.angi.{module}` (e.g., `vn.angi.recommendation`)
- Within module:
  - `.controller` - REST controllers
  - `.service` - Business logic
  - `.repository` - Data access
  - `.entity` - JPA entities
  - `.dto` - Data transfer objects
  - `.mapper` - MapStruct mappers
  - `.client` - External API clients
  - `.exception` - Module-specific exceptions

## 📝 Coding conventions

### 1. Layer boundaries
- Controllers: Only validation + call service
- Services: Business logic, orchestration
- Repositories: Only data access (JPA queries)
- Entities: No business logic in entity (anemic domain model OK for MVP)

### 2. DTO vs Entity
- **NEVER** expose entities to controllers
- Always use DTOs for request/response
- Use MapStruct for mapping

### 3. Exception handling
```java
// Don't do this
throw new RuntimeException("User not found");

// Do this
throw new NotFoundException(ErrorCodes.USER_NOT_FOUND, "User không tồn tại");
```

### 4. Naming
- Classes: `PascalCase`
- Methods: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `lowercase`

### 5. Dependency injection
- Use constructor injection with `@RequiredArgsConstructor` (Lombok)
- Never field injection with `@Autowired`

```java
@Service
@RequiredArgsConstructor  // Lombok generates constructor
public class RecommendationService {
    private final UserRepository userRepository;
    private final RecommendationCacheService cacheService;
    // ...
}
```
