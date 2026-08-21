# Book Worm — E-Bookstore Capstone

A full-stack demo e-commerce site for the **AI Specialist — Cloud FullStack Capstone**. Curated books, warm covers, ₹499 free-shipping threshold, 48-hour cancel window, mock payment gateway.

**Stack.** Spring Boot 3.3 (Java 21) · PostgreSQL 16 · React 18 + Vite + TypeScript · Redux Toolkit · Tailwind CSS

**Architecture.** OpenAPI-first / spec-driven. [`docs/openapi.yaml`](docs/openapi.yaml) is the single source of truth: the backend generates Spring controller interfaces + DTOs from it via `openapi-generator-maven-plugin`, and the frontend generates TypeScript types from it via `openapi-typescript`. Changes to the spec regenerate both sides on the next build.

---

## Quick start

### 1 · Prerequisites

Install locally (Windows one-liners with `winget`; equivalents exist for other OSes):

```powershell
winget install EclipseAdoptium.Temurin.21.JDK       # Java 21
winget install OpenJS.NodeJS.LTS                     # Node 20+
winget install PostgreSQL.PostgreSQL.16              # Postgres 16
```

You also need **Maven** (or use the bundled `mvnw` wrapper) and **npm** (bundled with Node).

### 2 · Database

Open `psql` as the superuser and run:

```sql
CREATE DATABASE bookworm;
CREATE USER bookworm WITH PASSWORD 'bookworm';
GRANT ALL PRIVILEGES ON DATABASE bookworm TO bookworm;
```

### 3 · Backend

```bash
cd backend
./mvnw spring-boot:run
```

- Boots on <http://localhost:8080/api>
- Flyway runs `V1__init.sql` + `V3__seed.sql` on first start (19 categories, 6 brands, 13 authors, 29 books, one demo user with 3 seeded orders)
- Swagger UI: <http://localhost:8080/api/swagger-ui.html>

### 4 · Frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open <http://localhost:5173>. The Vite dev server proxies `/api → :8080` so calls travel through it — no CORS ceremony needed.

---

## Demo credentials

- **Email:** `demo@bookworm.io`
- **Password:** `Demo@123`

The demo user has 250 gift points and three seeded orders in different states (DELIVERED, SHIPPED with cancel window open, CANCELLED with window closed) so the My Orders page has something to render.

### Testing payment paths

The mock gateway is deterministic:

| Input | Result |
|---|---|
| Card number ending in anything **except** `0000` | **Success** |
| Card number ending in `0000` (e.g. `4111 1111 1111 0000`) | **Declined** |
| UPI ID starting with `fail@` | **Declined** |
| Anything else | **Success** |

Declines return HTTP `402` with `code=payment_declined`. The order stays `PENDING` so the user can retry with a different method.

---

## Repository layout

```
E-Commerce/
├── docs/
│   ├── openapi.yaml                 ← contract; drives backend + frontend codegen
│   ├── data-model.md                ← ERD (Mermaid) + entity notes
│   └── bookworm.postman_collection.json
├── backend/                          ← Spring Boot 3.3, Java 21
│   ├── pom.xml                      ← openapi-generator-maven-plugin wired here
│   ├── src/main/java/com/bookworm/
│   │   ├── auth/                    ← /auth/* + JwtService + filter
│   │   ├── member/                  ← /me/* + addresses
│   │   ├── catalog/                 ← categories/brands/books/reviews/recommender
│   │   ├── cart/                    ← /cart/*
│   │   ├── wishlist/                ← /wishlist/*
│   │   ├── order/                   ← /orders/* + PricingCalculator + ShippingService
│   │   ├── payment/                 ← MockPaymentGateway
│   │   ├── config/                  ← SecurityConfig, CorsConfig, OpenApiConfig
│   │   └── common/                  ← ApiException + GlobalExceptionHandler
│   └── src/main/resources/db/migration/
│       ├── V1__init.sql             ← 14 tables
│       └── V3__seed.sql             ← wireframe-accurate demo data
└── frontend/                         ← React 18 + Vite 5 + TS 5
    ├── package.json                 ← npm run generate:api regenerates types
    ├── src/
    │   ├── api/                     ← generated schema.ts + type re-exports
    │   ├── components/              ← BookCard, BookRail, PaymentModal, ReviewsSection, Layout/*
    │   ├── pages/                   ← 9 pages, one per wireframe
    │   ├── store/                   ← Redux Toolkit + RTK Query + redux-persist
    │   └── lib/format.ts            ← ₹ + Indian digit grouping
    └── tailwind.config.ts           ← dark theme matching wireframes
```

---

## Verification checklist

The end-to-end demo can be reproduced in ~2 minutes:

1. Register a new user → JWT lands in `localStorage` (via `redux-persist`)
2. Home shows **Recommended for You** (once you have a completed order), **Bestsellers this Month**, **New Launches**
3. Filter `/catalog?category=self-help` → only self-help books
4. Open **Joy of Minimalism** → **Related Reads** sidebar shows other Self-help / Penguin Random House books
5. Add to cart → header badge increments → open cart → change quantity → totals update
6. `/checkout` → address pre-selected → move gift-points slider → totals update → Pay Now
7. Payment modal → non-0000 card → success → confirmation screen
8. `/orders` → new order visible with **Cancel Order** button
9. Cancel the seeded ≥ 48h-old order → 409 `cancel_window_closed`
10. Swagger UI at `/api/swagger-ui.html` shows every endpoint with try-it-out
11. `cd backend && ./mvnw test` — 11 tests pass
12. `cd frontend && npm run build` — clean, ~137 KB gzipped

---

## Tech stack

| Layer | Choice | Notes |
|---|---|---|
| Language (BE) | **Java 21** (LTS) | Compiled with `--release 21`; JDK 21 or higher required at build time |
| Framework (BE) | **Spring Boot 3.3.5** | `spring-boot-starter-{web,data-jpa,security,validation,actuator}` |
| Auth | **JJWT 0.12.6** | HS256, 15m access / 7d refresh, typed claims |
| Persistence | **PostgreSQL 16** + Flyway | H2 (MODE=PostgreSQL) for tests |
| Mapping | **MapStruct 1.6** + **Lombok 1.18** | DTO ↔ entity conversion |
| API docs | **springdoc-openapi 2.6** | Swagger UI at `/swagger-ui.html` |
| Codegen (BE) | **openapi-generator-maven-plugin 7.9** | Spring generator, `interfaceOnly=true` |
| Testing (BE) | JUnit 5 + REST-Assured + H2 | 11 tests: unit + integration |
| Language (FE) | **TypeScript 5.6** | Strict mode |
| Framework (FE) | **React 18.3** + **Vite 5.4** | Dev proxy `/api → :8080` |
| State | **Redux Toolkit 2.3** + **RTK Query** + **redux-persist** | JWT + user persisted across refreshes |
| Styling | **Tailwind CSS 3.4** | Dark theme matching wireframes; IBM Plex Sans |
| Forms | **React Hook Form 7** + **Zod 3** | Zod schemas double as TS types |
| Codegen (FE) | **openapi-typescript 7** | `npm run generate:api` |
| Testing (FE) | Vitest + React Testing Library + jsdom | 3 unit tests |

Explicitly **not** used: Docker (local install only per capstone Step 6), Testcontainers (Docker dep), real payment gateway (all mocked deterministically), cloud deployment.

---

## Development

### Regenerate API types after editing the spec

```bash
# Backend: happens automatically on next mvn build (openapi-generator-maven-plugin)
cd frontend && npm run generate:api
```

### Run tests

```bash
cd backend && ./mvnw test          # 11 backend tests
cd frontend && npm run test         # 3 frontend unit tests
```

### Import the Postman collection

Load `docs/bookworm.postman_collection.json`. Run **Auth › Login demo user** first — subsequent requests auto-authenticate via `{{accessToken}}`.

---

## Known limitations (deferred)

- **Book cover images**: seed data uses `/covers/*.jpg` URLs that don't exist on disk. `BookCard` falls back to a colour-hash gradient with the title so the UI still looks polished. Adding real cover art is a v2 concern.
- **Wireframe PNGs** in `docs/wireframes/`: not extracted from the capstone PDF automatically (no `pdftoppm` on the target machine). The PDF itself lives at the capstone's Downloads path.
- **Email receipts**: the confirmation screen says an email will be sent; no SMTP is wired.
- **Coupon codes**: `CheckoutRequest.couponCode` is accepted by the API but there's no coupon table yet.
- **Admin views**: no admin UI. Orders transition from PAID → SHIPPED → DELIVERED via direct DB update in v1.

---

## Contributing / capstone review path

The 14 feature branches were merged into `main` in dependency order. Reviewing them chronologically walks a reader through the whole build:

1. `feature/data-model-and-openapi` — the contract
2. `feature/backend-scaffold` — Maven + openapi-generator wiring
3. `feature/backend-auth`, `feature/backend-catalog`, `feature/backend-cart-wishlist`, `feature/backend-orders`, `feature/backend-payment-shipping-mock`, `feature/backend-recommendations` — one API slice at a time
4. `feature/seed-and-tests` — closes the backend
5. `feature/frontend-scaffold`, `feature/frontend-auth-and-home`, `feature/frontend-product-and-cart`, `feature/frontend-checkout-and-orders` — frontend, wireframe page by wireframe page
6. `feature/docs-and-readme` — this branch

Every PR title starts with the conventional-commit prefix (`feat` / `chore` / `docs` / `test`); the merge commit messages on `main` are the shortest path through the story.
