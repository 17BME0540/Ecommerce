# Ecommerce — Java 17 · Spring Boot · Spring AI Microservices

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M4-purple)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

A reference-architecture e-commerce backend built as independent Spring Boot
microservices, featuring service discovery, an API gateway, JWT auth,
inter-service communication with resilience patterns, and an **AI-powered
shopping assistant built with Spring AI**.

## Architecture

```
                        ┌──────────────────┐
                        │   API Gateway     │  :8080
                        │ (Spring Cloud     │
                        │     Gateway)      │
                        └─────────┬─────────┘
                                  │
        ┌───────────┬────────────┼────────────┬───────────────┐
        ▼           ▼            ▼             ▼               ▼
  user-service  product-service cart-service order-service notification-service
     :8081          :8082         :8083        :8084             :8085
        │              │             │             │
        └──────────────┴─────────────┴─────────────┘
                        │
                registers with
                        ▼
               ┌──────────────────┐
               │  Eureka Server    │  :8761
               └──────────────────┘
```

## Services

| Service | Port | Responsibility |
|---|---|---|
| `eureka-server` | 8761 | Service discovery (Netflix Eureka) |
| `api-gateway` | 8080 | Single entry point; routes requests to services (Spring Cloud Gateway) |
| `user-service` | 8081 | Registration/login, JWT issuance and validation |
| `product-service` | 8082 | Product catalog CRUD **+ Spring AI**: description generation & shopping-assistant chat |
| `cart-service` | 8083 | Per-user shopping cart |
| `order-service` | 8084 | Places orders; calls product/cart services via Feign; circuit breaker via Resilience4j |
| `notification-service` | 8085 | Order/email notifications (mock — logs only) |

## Highlights

- **Service discovery + gateway routing** — the standard pattern for enterprise Java microservices, using Netflix Eureka and Spring Cloud Gateway.
- **Stateless JWT authentication** in `user-service`, so downstream services never need a shared session store.
- **Resilient inter-service calls** — `order-service` talks to `product-service` and `cart-service` over OpenFeign, wrapped in a Resilience4j circuit breaker so a flaky dependency degrades gracefully instead of cascading failures.
- **Spring AI integration** in `product-service`:
  - `POST /api/ai/generate-description` — auto-writes a marketing-quality product description from a name and key features.
  - `POST /api/ai/assistant` — a chatbot that recommends products, grounded in the live product catalog.

## Prerequisites

- Java 17
- Maven 3.9+
- Docker (for Postgres)
- An OpenAI API key (only required for the `/api/ai/**` endpoints) — export as `OPENAI_API_KEY`

## Running locally

```bash
# 1. Start the databases
docker compose up -d

# 2. (optional) enable the AI endpoints
export OPENAI_API_KEY=sk-...

# 3. Build everything
mvn clean install

# 4. Start services, in this order, in separate terminals
cd eureka-server && mvn spring-boot:run          # wait for it to be up at :8761
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd cart-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

Confirm all services registered: http://localhost:8761

## Example flow (via the gateway on :8080)

```bash
# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"password123","fullName":"Jane Doe"}'

# Create a product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Wireless Mouse","description":"Ergonomic wireless mouse","price":19.99,"category":"Electronics","stockQuantity":50}'

# Let AI write a better description
curl -X POST http://localhost:8080/api/ai/generate-description \
  -H "Content-Type: application/json" \
  -d '{"productName":"Wireless Mouse","keyFeatures":"silent clicks, 18-month battery, USB-C","tone":"playful"}'

# Ask the shopping assistant for a recommendation
curl -X POST http://localhost:8080/api/ai/assistant \
  -H "Content-Type: application/json" \
  -d '{"message":"I need something for my home office setup, budget under $30"}'

# Add to cart
curl -X POST http://localhost:8080/api/cart \
  -H "Content-Type: application/json" \
  -d '{"userEmail":"jane@example.com","productId":1,"productName":"Wireless Mouse","price":19.99,"quantity":2}'

# Place an order (decrements stock, clears cart)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userEmail":"jane@example.com"}'
```

## Tech stack

Java 17 · Spring Boot 3.2.5 · Spring Cloud 2023.0.1 · Spring AI 1.0.0-M4 (OpenAI) · Spring Data JPA · PostgreSQL · Spring Security + JJWT · OpenFeign · Resilience4j · Maven (multi-module) · Docker Compose

## Roadmap

- [ ] Replace direct service calls with Kafka/RabbitMQ events (`OrderPlacedEvent`) for full async decoupling
- [ ] Add Testcontainers-based integration tests per service
- [ ] Add a `VectorStore` (pgvector) to `product-service` for real semantic product search
- [ ] Dockerfile per service + a full docker-compose profile (not just the databases)
- [ ] Centralized config (Spring Cloud Config) and distributed tracing (Micrometer + Zipkin)

## License

Distributed under the [MIT License](LICENSE).
