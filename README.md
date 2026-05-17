# Stock API

[![CI](https://github.com/OdgerelJ/stock-api/actions/workflows/ci.yml/badge.svg)](https://github.com/OdgerelJ/stock-api/actions/workflows/ci.yml)

A Spring Boot REST API for managing a personal stock watchlist and portfolio positions. Built to run as a standalone microservice that can be connected to the [ai-semis](../ai-semis) frontend.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JWT (JJWT 0.12) |
| Database | H2 (in-memory, dev) / PostgreSQL (prod) |
| ORM | Spring Data JPA + Hibernate |
| Validation | Jakarta Bean Validation |
| Build | Maven (wrapper included — no install needed) |
| Java | 17 |

## Getting Started

### Prerequisites
- Java 17+
- No Maven install required — use the included `./mvnw` wrapper

### Run locally

```bash
./mvnw spring-boot:run
```

Server starts at `http://localhost:8080`.

H2 console (dev only) available at `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:stockdb`
- Username: `sa` / Password: *(empty)*

### Run tests

```bash
./mvnw test
```

### Build JAR

```bash
./mvnw package -DskipTests
java -jar target/stock-api-0.0.1-SNAPSHOT.jar
```

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JWT_SECRET` | Yes (prod) | `dev-secret-change-in-production-min32` | HS256 signing key — must be ≥32 chars |
| `PORT` | No | `8080` | Server port |

Set them in a `.env` file or export before running:

```bash
export JWT_SECRET=your-very-long-random-secret-here
```

## API Reference

All protected endpoints require a `Bearer` token in the `Authorization` header.

### Auth

#### Register
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "odgerel",
  "password": "mypassword"
}
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "odgerel",
  "password": "mypassword"
}
```

Both return:
```json
{
  "token": "<jwt>",
  "username": "odgerel"
}
```

---

### Watchlist — `Authorization: Bearer <token>`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/watchlist` | List all tickers |
| `POST` | `/api/watchlist` | Add a ticker |
| `DELETE` | `/api/watchlist/{ticker}` | Remove a ticker |

**Add ticker:**
```
POST /api/watchlist
{ "ticker": "NVDA" }
```

---

### Positions — `Authorization: Bearer <token>`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/positions` | List all positions |
| `PUT` | `/api/positions` | Create or update a position |
| `DELETE` | `/api/positions/{ticker}` | Delete a position |

**Save position:**
```
PUT /api/positions
{
  "ticker": "NVDA",
  "quantity": 10,
  "avgCost": 850.00
}
```

---

### Health
```
GET /api/health
→ { "ok": true, "ts": 1716000000000 }
```

## Project Structure

```
src/main/java/com/stockapi/
├── config/
│   ├── GlobalExceptionHandler.java   # Centralized error responses
│   └── SecurityConfig.java           # Spring Security + JWT filter chain
├── controller/
│   ├── AuthController.java
│   ├── HealthController.java
│   ├── PositionController.java
│   └── WatchlistController.java
├── dto/                              # Request/response shapes
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── PositionRequest.java
│   └── RegisterRequest.java
├── entity/                           # JPA-managed database tables
│   ├── Position.java
│   ├── User.java
│   └── WatchlistItem.java
├── repository/                       # Spring Data JPA interfaces
│   ├── PositionRepository.java
│   ├── UserRepository.java
│   └── WatchlistRepository.java
├── security/
│   ├── JwtFilter.java                # Extracts + validates JWT on each request
│   ├── JwtUtil.java                  # Token generation and parsing
│   └── UserDetailsServiceImpl.java   # Loads user from DB for Spring Security
└── service/
    ├── AuthService.java
    ├── PositionService.java
    └── WatchlistService.java
```

## Connecting to ai-semis (Microservice)

Once this API is running, the Node.js backend in `ai-semis` can call it over HTTP:

```
ai-semis backend  →  http://localhost:8080/api/...
```

Set the Java API URL in ai-semis via an env var (e.g. `JAVA_API_URL=http://localhost:8080`) and use `fetch` or `axios` to proxy requests.

## Switching to PostgreSQL (Production)

1. Replace the H2 dependency in `pom.xml` with:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/stockdb
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

## Roadmap

- [ ] Quote fetching from Yahoo Finance
- [ ] Price alert endpoints
- [ ] P&L calculation endpoint with live prices
- [ ] Pagination on watchlist/positions
- [ ] PostgreSQL + Fly.io deployment
