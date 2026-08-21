# frontend — Book Worm (React 18 + Vite 5 + TypeScript)

Dark-themed SPA matching the capstone wireframes. Redux Toolkit + RTK Query drive the data layer; `redux-persist` keeps the JWT and user across refreshes.

## Prerequisites

- **Node 20+** (`winget install OpenJS.NodeJS.LTS`)
- The backend running at `http://localhost:8080` — the Vite dev server proxies `/api → :8080`.

## Run

```bash
npm install
npm run dev             # http://localhost:5173
```

## Build / test / lint

```bash
npm run build           # tsc + vite build → dist/
npm run test            # vitest run
npm run generate:api    # regenerate src/api/generated/schema.ts from ../docs/openapi.yaml
```

## Layout

```
src/
├── api/
│   ├── generated/schema.ts    ← 1746 lines, auto-generated; committed for zero-friction install
│   └── types.ts               ← convenience re-exports (Book, Cart, Order …)
├── store/
│   ├── store.ts               ← configureStore + persist gate
│   ├── api.ts                 ← RTK Query root (attaches Bearer JWT from state)
│   ├── api/                   ← one file per feature (auth, catalog, cart, wishlist, reviews, orders, address)
│   └── slices/                ← authSlice (persisted) + catalogSlice (filters)
├── components/
│   ├── Layout/                ← AppLayout, Header, Footer, CategorySidebar
│   ├── BookCard.tsx           ← cover-hash gradient fallback + title/author/price/rating
│   ├── BookRail.tsx           ← horizontal scroll row with skeleton loader
│   ├── PaymentModal.tsx       ← 4-tab (Credit/Debit/UPI/Wallet) modal with 1.5s deterministic spinner
│   └── ReviewsSection.tsx     ← list + inline write-a-review form
├── pages/                     ← 9 pages, one per wireframe
├── lib/format.ts              ← ₹ + Indian digit grouping
└── router.tsx                 ← 10 routes under AppLayout + separate /login
```

## Regenerating API types after a spec change

`npm run generate:api` writes `src/api/generated/schema.ts`. Commit it so `npm install && npm run dev` works without an extra codegen step in CI.
