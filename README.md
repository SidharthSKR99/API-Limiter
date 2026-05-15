# 🛡️ API Rate Limiter & Dashboard

A placement-ready, high-performance distributed rate limiter built with **Spring Boot 3**, **Redis (Lua Scripts)**, **PostgreSQL**, and **React**.

## 🚀 Features
* **Token Bucket Algorithm:** Atomic rate limiting using Redis Lua scripts.
* **Real-time Dashboard:** React UI visualizing traffic and token refill, integrated with standardized REST endpoints.
* **Scalable & Durable:** Docker Compose setup with PostgreSQL (via Flyway) and Redis.
* **Security & Reliability:** JWT authentication, robust validation, and structured JSON error responses.
* **Role-Based Limits:** Free (10 req/s) vs Gold (50 req/s).

## 🛠️ Tech Stack
* **Backend:** Java 17, Spring Boot 3, Spring Data Redis, Spring Security, Flyway, Testcontainers
* **Database:** PostgreSQL 16, Redis 7 (via Docker)
* **Frontend:** React, Tailwind CSS, Recharts, Vite, Playwright
* **DevOps:** Docker Compose, Nginx Proxy

## 🏗️ System Architecture

This project uses a layered architecture to ensure maximum performance:
1. **Interceptor Layer:** `RateLimitInterceptor` extracts the `X-API-KEY`.
2. **Cache Layer:** The API Key's associated plan is fetched from a Redis cache to prevent DB bottlenecking on every request.
3. **Lua Script Execution:** The `RateLimiterService` executes an atomic Lua script in Redis implementing the Token Bucket algorithm, avoiding race conditions.
4. **Header Injection:** Remaining tokens are calculated atomically and injected into standard `X-RateLimit-*` HTTP headers.

## 🏃 Dual-Mode Execution

### 1. Full-Stack Docker Compose (Production-Like)
The easiest way to run the entire stack (Postgres, Redis, Java Backend, React Nginx Frontend).
```bash
git clone https://github.com/SidharthSKR99/API-Limiter.git
cd API-Limiter/API-limiter
docker-compose up --build
```
- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080` (Proxied via Nginx)

### 2. Local Development Mode
If you want to edit code and see hot-reloads:
1. Copy `.env.example` to `.env` in both `API-limiter` and `frontend` directories.
2. Start infrastructure: `docker-compose up postgres redis`
3. Start backend: `./mvnw spring-boot:run`
4. Start frontend: `npm run dev` (in the `frontend/` directory).

## 🧪 Verification & Testing

### Automated Scripts
Run the PowerShell verification script to simulate a real user, extract tokens, hit the rate limit, and verify dashboard refills:
```powershell
cd API-limiter
.\verify_stack.ps1
```

### End-to-End Tests
**Backend Integration Tests** (Uses Testcontainers):
```bash
./mvnw verify
```

**Frontend Smoke Tests** (Uses Playwright):
```bash
cd frontend
npx playwright test
```

## 📸 Dashboard
`![Dashboard Screenshot](./screenshot.png)`
