# backend — Book Worm API (Spring Boot 3.3, Java 21)

Spec-driven backend. `openapi-generator-maven-plugin` reads `../docs/openapi.yaml` at `generate-sources` and produces controller interfaces + DTOs under `target/generated-sources/openapi/`. Hand-written services implement those interfaces.

## Prerequisites

- **JDK 21** (Adoptium Temurin recommended). Compilation with JDK 25 breaks Lombok's annotation processor — install 21 alongside:
  ```powershell
  winget install EclipseAdoptium.Temurin.21.JDK
  ```
- **PostgreSQL 16** with a database + role provisioned (see root README).

## Run

```bash
./mvnw spring-boot:run
```

Boots on `http://localhost:8080/api`. Swagger UI at `/swagger-ui.html`. Flyway auto-migrates `V1__init.sql` + `V3__seed.sql`.

## Test

```bash
./mvnw test         # 11 tests: 7 unit + 4 integration (H2 in PostgreSQL mode)
```

## Layout

```
src/main/java/com/bookworm/
├── auth/          JWT service + filter + /auth/* + /me lives in member/
├── member/        User + Address + /me/*
├── catalog/       Category/Brand/Author/Book/Review + search + related + recommender
├── cart/          /cart/*
├── wishlist/      /wishlist/*
├── order/         checkout + list + cancel + buy-again + shipping quote + PricingCalculator
├── payment/       MockPaymentGateway
├── config/        SecurityConfig (JWT filter chain) + CorsConfig + OpenApiConfig
└── common/        ApiException + GlobalExceptionHandler + PageResponses helper
```

## Regenerating API stubs

Any edit to `../docs/openapi.yaml` regenerates on the next Maven build. If you want an explicit refresh:

```bash
./mvnw generate-sources
```

Generated code lands under `target/generated-sources/openapi/` and is on the compile classpath but not committed.
