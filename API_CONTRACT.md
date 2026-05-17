# API Contract — stock-api ↔ ai-semis

This file is the source of truth for the interface between the Java microservice (`stock-api`) and the Node.js backend (`ai-semis`). Update this file whenever an endpoint, field name, or auth mechanism changes.

**Last updated:** 2026-05-17  
**stock-api base URL (local):** `http://localhost:8080`  
**stock-api base URL (prod):** set via `JAVA_API_URL` env var in ai-semis

---

## Authentication

All protected endpoints require:
```
Authorization: Bearer <jwt>
```

Tokens are obtained via `/api/auth/login` or `/api/auth/register`.  
ai-semis manages this automatically via `backend/src/lib/javaApi.ts` using a service account.

**Service account credentials (local dev):**
- Username: `aisemis-service` (env: `JAVA_API_USERNAME`)
- Password: `aisemis-service-password` (env: `JAVA_API_PASSWORD`)

---

## Endpoints

### POST /api/auth/register
**Auth:** None  
**Request:**
```json
{ "username": "string (3-30 chars)", "password": "string (6-100 chars)" }
```
**Response:**
```json
{ "token": "string", "username": "string" }
```

---

### POST /api/auth/login
**Auth:** None  
**Rate limit:** 10 requests / 15 minutes per IP  
**Request:**
```json
{ "username": "string", "password": "string" }
```
**Response:**
```json
{ "token": "string", "username": "string" }
```

---

### GET /api/auth/me
**Auth:** Required  
**Response:**
```json
{ "ok": true }
```
Used by ai-semis on page load to validate a cached JWT.

---

### GET /api/watchlist
**Auth:** Required  
**Response:**
```json
["NVDA", "TSLA", "AMD"]
```

---

### POST /api/watchlist
**Auth:** Required  
**Request:**
```json
{ "ticker": "NVDA" }
```
**Response:** `200 OK` (no body)  
**Notes:** Ticker is uppercased automatically. Duplicate adds are silently ignored.

---

### DELETE /api/watchlist/{ticker}
**Auth:** Required  
**Response:** `204 No Content`

---

### GET /api/positions
**Auth:** Required  
**Response:**
```json
[
  {
    "id": 1,
    "ticker": "NVDA",
    "quantity": 10.0,
    "avgCost": 850.0,
    "targetPrice": 1000.0,
    "stopPrice": 750.0
  }
]
```
> ⚠️ **Field name mapping** — ai-semis uses different names. See mapping table below.

---

### PUT /api/positions
**Auth:** Required  
**Request:**
```json
{
  "ticker": "NVDA",
  "quantity": 10.0,
  "avgCost": 850.0,
  "targetPrice": 1000.0,
  "stopPrice": 750.0
}
```
**Response:** The saved position object (same shape as GET).  
**Notes:** Upserts — creates if not exists, updates if exists.

---

### DELETE /api/positions/{ticker}
**Auth:** Required  
**Response:** `204 No Content`

---

### GET /api/health
**Auth:** None  
**Response:**
```json
{ "ok": true, "ts": 1716000000000 }
```

---

## Field Name Mapping

ai-semis frontend and backend use snake_case for positions. Java uses camelCase.  
The mapping is handled in `ai-semis/backend/src/routes/positions.ts`.

| ai-semis (Node)  | stock-api (Java) |
|------------------|-----------------|
| `shares`         | `quantity`      |
| `cost_basis`     | `avgCost`       |
| `target_price`   | `targetPrice`   |
| `stop_price`     | `stopPrice`     |

---

## Error Responses

All errors follow this shape:
```json
{ "error": "Human readable message" }
```

| Status | Meaning |
|--------|---------|
| `400`  | Validation error or bad request |
| `401`  | Missing or invalid JWT |
| `429`  | Rate limit exceeded (login endpoint) |
| `500`  | Internal server error |

---

## Planned Endpoints (not yet built)

| Endpoint | Description |
|----------|-------------|
| `GET /api/quotes/:ticker` | Fetch live price data (moves quote fetching to Java) |
| `GET /api/positions/pnl`  | Calculate P&L using live prices |
