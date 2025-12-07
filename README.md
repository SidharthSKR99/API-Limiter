# 🛡️ API Rate Limiter & Dashboard

A high-performance distributed rate limiter built with **Spring Boot 3**, **Redis (Lua Scripts)**, and **React**.

## 🚀 Features
* **Token Bucket Algorithm:** Atomic rate limiting using Redis Lua scripts.
* **Real-time Dashboard:** React UI visualizing traffic and token refill.
* **Scalable:** Dockerized architecture ready for cloud deployment.
* **Security:** API Key validation and role-based limits (Free vs Gold).

## 🛠️ Tech Stack
* **Backend:** Java 17, Spring Boot, Spring Data Redis
* **Database:** Redis (Docker)
* **Frontend:** React, Tailwind CSS, Recharts
* **DevOps:** Docker Compose

## 🏃 How to Run
1.  **Clone the repo:** `git clone ...`
2.  **Start Backend:** `docker-compose up --build`
3.  **Start Frontend:** `cd frontend && npm run dev`
4.  **Open Dashboard:** `http://localhost:5173`

## 📸 Screenshots
[Insert screenshot of your dashboard here]
