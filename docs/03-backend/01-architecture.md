# 01. Backend Architecture

## 🏗 Kiến trúc tổng quan

### Tech stack
- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.2+
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **Search** (future): Elasticsearch
- **Message Queue** (future): RabbitMQ hoặc Kafka

### Architecture pattern
**Modular Monolith** cho MVP, có thể tách microservices sau.

```
┌──────────────────────────────────────────────┐
│              API Gateway Layer                │
│         (Spring Cloud Gateway)                │
└─────────────────────┬────────────────────────┘
                      │
      ┌───────────────┼───────────────┐
      │               │                │
      ▼               ▼                ▼
┌───────────┐ ┌──────────────┐ ┌──────────────┐
│   Auth    │ │Recommendation│ │    Group     │
│  Module   │ │   Module     │ │   Module     │
└─────┬─────┘ └──────┬───────┘ └──────┬───────┘
      │              │                 │
      └──────────────┼─────────────────┘
                     │
      ┌──────────────┴──────────────┐
      │                              │
      ▼                              ▼
┌───────────┐                 ┌──────────────┐
│PostgreSQL │                 │    Redis     │
│  (primary)│                 │   (cache)    │
└───────────┘                 └──────────────┘
      │
      │  Async events
      ▼
┌──────────────┐
│  External    │
│   APIs:      │
│ - OpenWeather│
│ - Claude API │
│ - Google Maps│
└──────────────┘
```

### Module breakdown

#### Core modules (MVP)
1. **Auth Module**: Registration, login, JWT, OAuth
2. **User Module**: Profile, preferences, history
3. **Restaurant Module**: CRUD quán ăn, search
4. **Recommendation Module**: Logic gợi ý với AI
5. **Context Module**: Weather, location, time aggregation
6. **Feedback Module**: Feedback loop, analytics

#### Phase 2 modules
7. **Group Module**: Team, lunch poll, voting
8. **Notification Module**: Push notifications
9. **Analytics Module**: Event tracking, funnels

#### Shared modules
- **Common**: DTOs, utilities, exceptions
- **Security**: JWT, encryption
- **Infrastructure**: Config, DB migrations

---

## 📐 Design principles

### 1. Domain-Driven Design (DDD)
Mỗi module có domain logic riêng, không phụ thuộc trực tiếp module khác.

### 2. CQRS lite
Tách read/write models cho các flow phức tạp (vd: Recommendation read từ cache, write từ events).

### 3. Event-driven (where needed)
Sử dụng Spring Events cho các cross-module communication:
- `UserOnboardedEvent` → Recommendation module tạo initial preferences
- `MealFeedbackEvent` → Recommendation module update model
- `GroupDecisionMadeEvent` → Analytics module track

### 4. Fail-fast, graceful degradation
- LLM API fail → Fallback to rule-based
- Weather API fail → Use cached weather
- External API timeout: 3 seconds max

---

## 🔐 Security architecture

### Authentication flow
```
Client                  API Gateway          Auth Service
  │                          │                      │
  ├─ POST /auth/login ──────▶│                      │
  │                          ├─ Forward ───────────▶│
  │                          │                      ├─ Validate credentials
  │                          │                      ├─ Generate JWT
  │                          │                      │  (access + refresh)
  │                          │◀── Tokens ──────────│
  │◀── Tokens + User ───────│                      │
  │                          │                      │
  ├─ Next requests ──────────▶│                     │
  │  with Bearer token        │                     │
  │                          ├─ Validate JWT        │
  │                          ├─ Forward to service  │
```

### JWT structure
- **Access token**: 15 phút
- **Refresh token**: 30 ngày (stored in Redis)
- **Claims**: userId, email, roles, iat, exp

### Security measures
- BCrypt cho password hashing (strength 12)
- HTTPS mandatory
- Rate limiting: 100 requests/minute/user
- CORS configured for frontend domains
- SQL injection: Parameterized queries only (JPA)
- XSS prevention: Input sanitization

---

## 🗄 Caching strategy

### Redis use cases

#### 1. Recommendation cache
```
Key: recommendation:{userId}:{dayOfYear}:{hour}
Value: JSON list of 3 recommendations
TTL: 15 minutes

Why? Multiple requests same user same hour → same result
```

#### 2. Restaurant data cache
```
Key: restaurant:{id}
Value: Full restaurant object
TTL: 1 hour

Why? Restaurant data thay đổi không thường xuyên
```

#### 3. Weather cache
```
Key: weather:{lat_rounded}:{lng_rounded}:{hour}
Value: Weather data
TTL: 1 hour

Why? OpenWeather API rate limits, weather không đổi mỗi phút
```

#### 4. User preferences cache
```
Key: user:preferences:{userId}
Value: User preferences object
TTL: 1 day (invalidate on update)

Why? Read-heavy, change ít
```

#### 5. Session data (refresh tokens)
```
Key: session:{userId}:{sessionId}
Value: refresh token + metadata
TTL: 30 days
```

#### 6. Rate limiting
```
Key: ratelimit:{userId}:{endpoint}
Value: counter
TTL: 1 minute
```

### Cache invalidation rules
- **User preferences updated** → invalidate user:preferences:{id}
- **Restaurant updated** → invalidate restaurant:{id}
- **Feedback submitted** → invalidate recommendation cache (forces fresh recommendation next time)

---

## 🔄 Recommendation Flow (Critical path)

```
1. GET /api/v1/recommendations
   │
   ├─ Check Redis cache
   │   │
   │   ├─ HIT → Return cached (< 50ms)
   │   │
   │   └─ MISS → Continue
   │
   ├─ Gather context (parallel):
   │   ├─ Load user preferences (from DB/cache)
   │   ├─ Load user meal history (last 30 days)
   │   ├─ Get weather (from cache/OpenWeather API)
   │   ├─ Get time context (day, hour)
   │   └─ Get location (from request)
   │
   ├─ Filter restaurants:
   │   ├─ Within radius (geospatial query)
   │   ├─ Not in user's excluded foods
   │   ├─ Within budget range
   │   └─ Matching dietary preferences
   │
   ├─ Call LLM:
   │   ├─ Build prompt with context
   │   ├─ Send to Claude API
   │   ├─ Timeout: 3 seconds
   │   └─ Fail → Rule-based fallback
   │
   ├─ Process LLM response:
   │   ├─ Parse 3 recommendations
   │   ├─ Enrich with restaurant metadata
   │   └─ Add explanations
   │
   ├─ Cache result in Redis (15 min)
   │
   └─ Return to client (< 3 seconds total)
```

### Performance targets
- P50: < 500ms (cache hit)
- P95: < 2000ms (cache miss)
- P99: < 3000ms
- Error rate: < 0.1%

---

## 📊 Observability

### Logging
- **Format**: JSON structured
- **Library**: Logback + SLF4J
- **Levels**:
  - ERROR: Critical issues, need immediate attention
  - WARN: Unexpected but recoverable
  - INFO: Important business events (signup, recommendation_generated)
  - DEBUG: Dev-only details

```java
// Example log
log.info("recommendation.generated",
    kv("userId", userId),
    kv("restaurantCount", count),
    kv("latency_ms", elapsed),
    kv("cache_hit", false)
);
```

### Metrics (Micrometer + Prometheus)
- `http_requests_total{endpoint, status}`
- `recommendation_latency{cache_hit, llm_provider}`
- `llm_api_calls_total{provider, status}`
- `active_users_gauge`
- `database_connection_pool_utilization`

### Tracing (future)
- OpenTelemetry
- Jaeger for visualization

---

## 🚀 Deployment architecture

### MVP (Single server)
```
┌─────────────────────────────────┐
│       Docker Compose             │
│                                  │
│  ┌─────────┐  ┌──────────────┐ │
│  │ Backend │  │  PostgreSQL  │ │
│  │  :8080  │  │    :5432     │ │
│  └─────────┘  └──────────────┘ │
│                                  │
│  ┌─────────┐  ┌──────────────┐ │
│  │  Redis  │  │    Nginx     │ │
│  │  :6379  │  │   :80,443    │ │
│  └─────────┘  └──────────────┘ │
│                                  │
└─────────────────────────────────┘

Host: 1 VPS (4 CPU, 8GB RAM)
Provider: DigitalOcean / AWS Lightsail / GCP e2
```

### Scale-up (10k+ DAU)
```
- 2+ Backend instances (load balanced)
- Managed PostgreSQL (read replicas)
- Redis Cluster
- CDN for static assets
- Separate worker nodes for async jobs
```

### Production considerations
- Database backups: Daily automated
- Monitoring: UptimeRobot + custom Prometheus
- Alerts: Slack webhook for critical errors
- SSL: Let's Encrypt auto-renewal

---

## 🧪 Testing strategy

### Unit tests (target 70% coverage)
- Service layer logic
- Domain logic
- Utility functions
- Tool: JUnit 5 + Mockito

### Integration tests
- Repository layer (with Testcontainers PostgreSQL)
- External API clients (WireMock)
- Cache operations

### API tests
- Controllers with MockMvc
- Full flow tests with @SpringBootTest

### Contract tests
- Pact between backend and frontend (future)

### Load tests
- K6 scripts for critical endpoints
- Target: Handle 100 req/s on MVP

---

## 📦 Dependency management

### Core dependencies
```xml
<!-- Spring Boot BOM -->
<spring-boot.version>3.2.0</spring-boot.version>

<!-- Key dependencies -->
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-data-redis
- spring-boot-starter-security
- spring-boot-starter-validation
- postgresql (driver)
- flyway-core (migrations)
- jjwt-api / jjwt-impl / jjwt-jackson (JWT)
- lombok
- mapstruct (DTO mapping)
- springdoc-openapi (Swagger)
- micrometer-registry-prometheus
```

### Utility libraries
- Jackson (JSON)
- Apache Commons Lang/IO
- Google Guava
- OkHttp (external API clients)
- Resilience4j (circuit breaker, retry)

---

## 🔧 Configuration management

### Profiles
- `dev`: Local development
- `test`: Integration tests
- `staging`: Staging environment
- `prod`: Production

### Secrets management (MVP)
- Environment variables
- `.env` file (not committed)
- Future: AWS Secrets Manager / HashiCorp Vault

### Example application.yml
```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASS}

  redis:
    host: ${REDIS_HOST}
    port: 6379

  jpa:
    hibernate:
      ddl-auto: validate

app:
  jwt:
    secret: ${JWT_SECRET}
    access-token-ttl: 900  # 15 min
    refresh-token-ttl: 2592000  # 30 days

  llm:
    provider: claude  # or openai
    api-key: ${LLM_API_KEY}
    timeout-ms: 3000

  weather:
    api-key: ${OPENWEATHER_API_KEY}
    cache-ttl-seconds: 3600
```
