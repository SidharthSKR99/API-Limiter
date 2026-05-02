# 🛡️ API Rate Limiter & Dashboard

A high-performance distributed rate limiter built with **Spring Boot 3**, **Redis (Lua Scripts)**, and **React**.

## 🚀 Features
* **Token Bucket Algorithm:** Atomic rate limiting using Redis Lua scripts.
* **Real-time Dashboard:** React UI visualizing traffic and token refill.
* **Scalable:** Dockerized architecture ready for cloud deployment.
* **Security:** API Key validation, JWT Dashboard Authentication, and Redis-based caching.
* **Role-Based Limits:** Free (10 req/s) vs Gold (50 req/s).

## 🛠️ Tech Stack
* **Backend:** Java 17, Spring Boot 3, Spring Data Redis, Spring Security
* **Database:** Redis (Docker), H2 (In-memory)
* **Frontend:** React, Tailwind CSS, Recharts, Vite
* **DevOps:** Docker, Docker Compose

## 🏗️ System Design

This project uses a layered architecture to ensure maximum performance for rate-limited endpoints:
1. **Interceptor Layer:** `RateLimitInterceptor` extracts the `X-API-KEY`.
2. **Cache Layer:** The API Key's associated plan is fetched from a Redis cache to prevent DB bottlenecking.
3. **Lua Script Execution:** The `RateLimiterService` executes an atomic Lua script in Redis implementing the Token Bucket algorithm, avoiding race conditions.
4. **Header Injection:** Remaining tokens are returned and injected into standard `X-RateLimit-*` headers.

## 🏃 How to Run (Docker)

The entire stack is containerized.

1. **Clone the repo:** `git clone https://github.com/SidharthSKR99/API-Limiter.git`
2. **Start Everything:** 
   ```bash
   cd API-limiter
   docker-compose up --build
   ```
3. **Open Dashboard:** `http://localhost:3000`

*(Note: The backend runs on `8080`, Redis on `6379`, and Frontend on `3000`)*

## 🧪 How to Test

1. Register an account and get your API Key from the dashboard.
2. Run this curl command to test the rate limiter (replace `YOUR_API_KEY`):

```bash
curl -i -H "X-API-KEY: YOUR_API_KEY" http://localhost:8080/api/weather/current
```
Watch the `X-RateLimit-Remaining` header decrease. Send more than your limit to see the `429 Too Many Requests` JSON error.

## 📸 Screenshots
*(Take a screenshot of your dashboard showing the real-time graph, and place it in the project directory, then update this link)*
`![Dashboard Screenshot](./screenshot.png)`
