# `docs/`

Contract and reference material. These files drive both codegen paths (backend Spring interfaces + frontend TypeScript types).

| File | Purpose |
|---|---|
| [`openapi.yaml`](openapi.yaml) | OpenAPI 3.0.3 contract. Single source of truth. Any API change starts here. |
| [`data-model.md`](data-model.md) | ERD (Mermaid) + one-paragraph description of every entity. Companion to `V1__init.sql`. |
| [`bookworm.postman_collection.json`](bookworm.postman_collection.json) | 26-request Postman/Insomnia collection covering every endpoint. Login script captures the JWT into a collection variable. |
| `wireframes/` (empty) | Reference PNGs from the capstone brief PDF. Extraction was deferred — see the root README. |

## Editing the OpenAPI spec

After changing `openapi.yaml`:

```bash
cd backend && ./mvnw compile      # regenerates Java interfaces + DTOs
cd frontend && npm run generate:api  # regenerates TypeScript types
```

Both codegen steps also run automatically on next `mvn package` and `npm run build`.
