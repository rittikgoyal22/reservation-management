# Reservation Management Service

Microservice responsible for managing travel reservations in the **Employee Travel Desk (ETD)** system. A Travel Desk Executive (TravelDeskExe) books flights, trains, buses, cabs, and hotels against approved travel requests. Employees can then track and download their booking confirmations.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Service Overview](#service-overview)
3. [Getting Started](#getting-started)
4. [Configuration](#configuration)
5. [Architecture](#architecture)
6. [Database Schema](#database-schema)
7. [Startup Data Seeding](#startup-data-seeding)
8. [API Reference](#api-reference)
9. [Business Rules & Requirements](#business-rules--requirements)
10. [Authentication & Authorization](#authentication--authorization)
11. [File Upload & Download](#file-upload--download)
12. [Error Handling](#error-handling)
13. [Inter-service Communication](#inter-service-communication)
14. [Known Constraints & Notes](#known-constraints--notes)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| Build tool | Gradle (no wrapper JAR committed — use system `gradle`) |
| Persistence | Spring Data JPA + Hibernate |
| Database (dev) | H2 file-mode |
| Database (prod) | MySQL (commented out — see Configuration) |
| Security | Spring Security 6 + stateless JWT (JJWT 0.12.6, HMAC-SHA256) |
| HTTP clients | Spring Cloud OpenFeign |
| API docs | Springdoc OpenAPI (Swagger UI) |
| Utilities | Lombok |

---

## Service Overview

| Property | Value |
|---|---|
| Port | **8083** |
| Base path | `/api/reservations` |
| Spring app name | `reservation-management` |
| Package | `com.etd.reservation_management` |

### Role in ETD System

| Service | Port | Responsibility |
|---|---|---|
| auth-service | 8080 | Login, token issuance, logout, blacklist |
| account-management | 8081 | Employee & grade CRUD |
| travel-planner | 8082 | Travel request lifecycle, budget calculation |
| **reservation-management** | **8083** | **Reservation booking & document management** |
| reimbursement-management | 8084 | Reimbursement request lifecycle |

---

## Getting Started

### Prerequisites

- Java 21
- Gradle (system install)
- account-management running (provides H2 TCP server on port 9092 — needed by auth-service)
- auth-service running on port 8080
- travel-planner running on port 8082

### Startup Order (important)

```
1. account-management  → starts H2 TCP server (port 9092)
2. auth-service        → connects to account-management's H2
3. travel-planner      → independent H2 file DB
4. reservation-management  → independent H2 file DB (this service)
```

### Run

```bash
gradle bootRun
```

### Build

```bash
gradle build
```

### H2 Console (dev)

URL: `http://localhost:8083/h2-console`
- JDBC URL: `jdbc:h2:file:~/data/reservation_types`
- Username: `sa`
- Password: _(blank)_

### Swagger UI

`http://localhost:8083/swagger-ui.html`

---

## Configuration

All settings in `src/main/resources/application.properties`:

```properties
# Server
server.port=8083
spring.application.name=reservation-management

# H2 Database (dev)
spring.datasource.url=jdbc:h2:file:~/data/reservation_types;AUTO_SERVER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true

# MySQL Database (prod — uncomment and add mysql-connector-j to build.gradle)
# spring.datasource.url=jdbc:mysql://localhost:3306/reservation_management
# spring.datasource.username=root
# spring.datasource.password=<your-password>
# spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT (must match all other ETD services)
jwt.secret=etdTravelDeskJwtSecretKey1234567890ABCDEF

# Dependent service URLs
auth.service.base_url=http://localhost:8080/
travel.planner.service.base_url=http://localhost:8082/
travel.planner.service.url=api/

# File upload directory (relative to working dir, auto-created on first upload)
app.upload.dir=./uploads
```

---

## Architecture

### Layer Flow

```
HTTP Request
     ↓
JwtAuthFilter  (validates JWT, checks blacklist via auth-service Feign)
     ↓
SecurityConfig (role-based access enforcement)
     ↓
Controller     (@RestController, @CrossOrigin)
     ↓
Service Interface → Service Impl
     ↓                     ↓
Mapper               Feign Clients (travel-planner, auth-service)
     ↓
JPA Repository → H2 / MySQL
```

### Package Layout

```
com.etd.reservation_management
├── client/
│   ├── AuthServiceClient.java       Feign → GET /auth/blacklist/check
│   └── TravelPlannerClient.java     Feign → GET /api/travelrequests/{trid}
├── config/
│   ├── DataInitializer.java         Seeds reservation types on startup
│   ├── FeignAuthInterceptor.java    Forwards Bearer token to all outgoing Feign calls
│   ├── JwtAuthFilter.java           Per-request JWT validation filter
│   └── SecurityConfig.java          Role-based security rules
├── constant/
│   └── AppConstant.java             All string constants and message keys
├── controller/
│   ├── ReservationController.java       POST /add, GET /track/{id}, GET /{id}
│   ├── ReservationDocsController.java   GET /{id}/download
│   └── ReservationTypeController.java   GET /types
├── dao/
│   ├── ReservationRepo.java         findByTravelRequestId(Long)
│   ├── ReservationDocsRepo.java     findByReservationId(Long)
│   └── ReservationTypeRepo.java     Standard JpaRepository
├── dto/
│   ├── ReservationRequestDTO.java
│   ├── ReservationResponseDTO.java
│   ├── ReservationTypeResponseDTO.java
│   └── ErrorDTO.java
├── entity/
│   ├── Reservation.java             Table: reservations
│   ├── ReservationDocs.java         Table: reservation_docs
│   └── ReservationType.java         Table: reservation_types
├── exception/
│   ├── BadRequestException.java
│   ├── DocumentSizeLimitExceededException.java
│   ├── IllegalArgumentException.java
│   ├── IllegalStateException.java
│   ├── NotFoundException.java
│   └── GlobalExceptionHandler.java  @RestControllerAdvice
├── mapper/
│   ├── ReservationMapper.java
│   ├── ReservationDocsMapper.java
│   └── ReservationTypeMapper.java
├── service/
│   ├── interfaces/
│   │   ├── ReservationService.java
│   │   ├── ReservationDocsService.java
│   │   └── ReservationTypeService.java
│   └── classes/
│       ├── ReservationServiceImpl.java
│       ├── ReservationDocsServiceImpl.java
│       └── ReservationTypeServiceImpl.java
└── util/
    └── JWTUtil.java                 extractUsername, extractRole, validateToken
```

---

## Database Schema

### `reservation_types`

| Column | Type | Notes |
|---|---|---|
| `type_id` | BIGINT (PK, identity) | Auto-generated |
| `type_name` | VARCHAR | "Flight", "Train", "Bus", "Cab", "Hotel" |

### `reservations`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT (PK, identity) | Auto-generated |
| `reservation_done_by_employee_id` | BIGINT | TravelDeskExe employee ID (no FK — cross-service) |
| `travel_request_id` | BIGINT | Travel request ID from travel-planner (no FK — cross-service) |
| `reservation_type_id` | BIGINT (FK) | References `reservation_types.type_id` |
| `created_on` | DATE | Set to current date on save |
| `reservation_done_with_entity` | VARCHAR | Airline/hotel/company name |
| `reservation_date` | DATE | Date of travel/check-in |
| `amount` | BIGINT | Amount in INR |
| `confirmation_id` | VARCHAR | PNR/booking reference |
| `remarks` | VARCHAR | Optional notes |

### `reservation_docs`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT (PK, identity) | Auto-generated |
| `reservation_id` | BIGINT (FK, unique) | @OneToOne → reservations.id |
| `document_url` | VARCHAR | PDF filename (not full path) |

---

## Startup Data Seeding

`DataInitializer` (implements `ApplicationRunner`) runs on every startup. It is **idempotent** — seeds only when `reservation_types` table is empty.

Seeded reservation types in order:

| Auto-assigned ID | Name | Used For |
|---|---|---|
| 1 | Flight | Air travel |
| 2 | Train | Train travel |
| 3 | Bus | Bus travel |
| 4 | Cab | Local cab/taxi |
| 5 | Hotel | Accommodation |

> **Warning:** These IDs are referenced by hard-coded values in business logic (e.g., `TRAVEL_MODES_IDS = Set.of(1L, 2L, 3L)`, Cab = 4L, Hotel = 5L). If the DB is wiped and re-seeded, IDs reset to 1–5 correctly. But do not manually insert or reorder types.

---

## API Reference

### Authentication

All endpoints except Swagger/H2 console require a valid JWT token in the `Authorization` header:
```
Authorization: Bearer <token>
```

Tokens are issued by **auth-service** (`POST http://localhost:8080/login`).

---

### GET `/api/reservations/types`

**Description:** Retrieve all available reservation types (Flight, Train, Bus, Cab, Hotel).

**Auth:** Any authenticated user (HR, Employee, TravelDeskExe)

**Response `200 OK`:**
```json
[
  { "typeId": 1, "typeName": "Flight" },
  { "typeId": 2, "typeName": "Train" },
  { "typeId": 3, "typeName": "Bus" },
  { "typeId": 4, "typeName": "Cab" },
  { "typeId": 5, "typeName": "Hotel" }
]
```

---

### POST `/api/reservations/add`

**Description:** Create a new reservation for an approved travel request. Uploads the PDF booking document.

**Auth:** TravelDeskExe only

**Content-Type:** `multipart/form-data`

| Part | Type | Required | Description |
|---|---|---|---|
| `reservationRequestDTO` | JSON (application/json) | Yes | Reservation details |
| `pdfFile` | File | Yes | PDF booking confirmation (max 1 MB, must be PDF) |

**`reservationRequestDTO` fields:**

| Field | Type | Required | Description |
|---|---|---|---|
| `reservationDoneByEmployeeId` | Long | Yes | Employee ID of the TravelDeskExe |
| `travelRequestId` | Long | Yes | ID of the approved travel request |
| `reservationTypeId` | Long | Yes | ID from `/api/reservations/types` |
| `reservationDoneWithEntity` | String | Yes | Airline/hotel/company name |
| `reservationDate` | Date | Yes | Date of travel or check-in (see date rules) |
| `amount` | Long | Yes | Cost in INR (must be > 0) |
| `confirmationId` | String | Yes | PNR / booking reference number |
| `remarks` | String | No | Optional notes |

**Sample request body (JSON part):**
```json
{
  "reservationDoneByEmployeeId": 100002,
  "travelRequestId": 1,
  "reservationTypeId": 1,
  "reservationDoneWithEntity": "IndiGo Airlines",
  "reservationDate": "2025-12-01",
  "amount": 5000,
  "confirmationId": "PNR123456",
  "remarks": "Window seat requested"
}
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "reservationDoneByEmployeeId": 100002,
  "travelRequestId": 1,
  "createdOn": "2025-11-20",
  "reservationDoneWithEntity": "IndiGo Airlines",
  "reservationDate": "2025-12-01",
  "amount": 5000,
  "confirmationId": "PNR123456",
  "remarks": "Window seat requested",
  "reservationTypeName": "Flight"
}
```

**Possible error responses:**
| Status | Reason |
|---|---|
| 400 | Amount is null or ≤ 0 |
| 400 | Invalid reservation type ID |
| 400 | Travel request not found or unreachable |
| 400 | Travel request not in APPROVED status |
| 400 | Reservation date violates date rules |
| 400 | Duplicate reservation for same type |
| 400 | Budget not yet calculated on travel request |
| 400 | Amount exceeds budget cap for the type |
| 400 | PDF content-type is not application/pdf |
| 400 | Failed to save PDF file |
| 403 | Not authenticated / wrong role |
| 413 | PDF file exceeds 1 MB |

---

### GET `/api/reservations/track/{travelRequestId}`

**Description:** Get all reservations made for a specific travel request.

**Auth:** Employee only

**Path variable:** `travelRequestId` (Long) — ID of the travel request

**Response `200 OK`:** Array of `ReservationResponseDTO` (same structure as POST response)

**Response `404`:** No reservations found for the given travel request ID

---

### GET `/api/reservations/{reservationId}`

**Description:** Get a single reservation by its ID.

**Auth:** Employee only

**Path variable:** `reservationId` (Long)

**Response `200 OK`:** Single `ReservationResponseDTO`

**Response `404`:** Reservation not found

---

### GET `/api/reservations/{reservationId}/download`

**Description:** Download the PDF booking confirmation for a reservation.

**Auth:** Employee only

**Path variable:** `reservationId` (Long)

**Response `200 OK`:**
- Content-Type: `application/pdf`
- Body: Raw PDF bytes

**Response `404`:** No document found for the given reservation ID

---

## Business Rules & Requirements

All BRs are derived from the ETD project specification document (`CDE-EmployeeTravelDesk-V1.0.1.docx`).

---

### BR-1: Only TravelDeskExe can create reservations

Only users with role `TravelDeskExe` can call `POST /api/reservations/add`. Employees and HR cannot create reservations.

---

### BR-2: Travel request must be APPROVED

A reservation can only be created for a travel request with status `APPROVED`. Attempts on `PENDING`, `REJECTED`, or any other status result in a 400 error.

---

### BR-3: Budget must be calculated before reservation

The `approvedBudget` field on the travel request must exist and be non-null. If HR has not yet called the budget calculation endpoint on travel-planner, reservation creation is blocked with a descriptive error.

---

### BR-4: Reservations budget = 70% of approved budget

Not all of the approved budget is for reservations. The effective reservations budget is:
```
reservationsBudget = approvedBudget × 70 / 100
```

---

### BR-5: Amount caps by reservation type

Within the reservations budget:

| Type | Max % of reservations budget |
|---|---|
| Flight, Train, or Bus | 35% |
| Cab | 15% |
| Hotel | 50% |

Example: If `approvedBudget = ₹10,000`, then `reservationsBudget = ₹7,000`.
- Max flight amount = ₹7,000 × 35% = ₹2,450
- Max cab amount = ₹7,000 × 15% = ₹1,050
- Max hotel amount = ₹7,000 × 50% = ₹3,500

---

### BR-6: Only one travel mode reservation per travel request

A travel request can have at most one reservation in the **travel mode** category (Flight, Train, or Bus). Attempting to add a second travel mode reservation returns a 400 error, regardless of which type is being duplicated.

---

### BR-7: Only one Cab reservation per travel request

Only one cab reservation is allowed per travel request.

---

### BR-8: Only one Hotel reservation per travel request

Only one hotel reservation is allowed per travel request.

---

### BR-9: Train/Bus reservation date must be 1 day before travel date

If reserving a train or bus ticket, the `reservationDate` must be exactly **1 day before** the travel request's `fromDate`. This accounts for advance booking.

---

### BR-10: Hotel reservation date must equal travel date

If reserving a hotel, the `reservationDate` must be the **same day** as the travel request's `fromDate` (check-in on day of travel).

---

### BR-11: PDF confirmation document is mandatory

Every reservation must include a PDF booking confirmation document uploaded as a multipart file. The document is stored and associated with the reservation for later download.

---

### BR-12: PDF size limit — 1 MB

The uploaded PDF must not exceed **1,048,576 bytes (1 MB)**. Larger files are rejected with HTTP 413.

---

### BR-13: Only PDF files accepted

The uploaded file must have Content-Type `application/pdf`. Non-PDF files (even if renamed with `.pdf`) are rejected with HTTP 400.

---

### BR-14: Amount must be positive

The `amount` field must be a non-null positive number (> 0). Zero or negative amounts are rejected.

---

### BR-15: Employee can track all reservations for a travel request

An employee can retrieve all reservations associated with their travel request using the `GET /api/reservations/track/{travelRequestId}` endpoint.

---

### BR-16: Employee can download booking PDF

An employee can download the stored PDF confirmation for any reservation using `GET /api/reservations/{reservationId}/download`.

---

## Authentication & Authorization

### JWT Structure

Tokens are issued by **auth-service** and carry:
- `sub` — user's email address
- `role` — one of `"HR"`, `"Employee"`, `"TravelDeskExe"`
- `iat` — issued-at timestamp
- `exp` — expiry (1 hour from issuance)

Signed with HMAC-SHA256 using:
```
jwt.secret=etdTravelDeskJwtSecretKey1234567890ABCDEF
```

### Token Validation Flow

Every request goes through `JwtAuthFilter`:

```
1. Extract Bearer token from Authorization header
2. Decode username (subject claim) — reject silently if invalid/expired
3. Call auth-service → GET /auth/blacklist/check?token=...
   → if blacklisted: do not set SecurityContext (Spring Security returns 403)
   → if auth-service unreachable: skip blacklist check, continue (fail-open)
4. Validate token signature + expiry via JWTUtil.validateToken(token)
5. Extract role claim → set SimpleGrantedAuthority in SecurityContextHolder
```

### Access Matrix

| Endpoint | HR | Employee | TravelDeskExe |
|---|---|---|---|
| GET `/api/reservations/types` | ✅ | ✅ | ✅ |
| POST `/api/reservations/add` | ❌ | ❌ | ✅ |
| GET `/api/reservations/track/{travelRequestId}` | ❌ | ✅ | ❌ |
| GET `/api/reservations/{reservationId}` | ❌ | ✅ | ❌ |
| GET `/api/reservations/{reservationId}/download` | ❌ | ✅ | ❌ |

> Unauthenticated requests (missing or invalid token) return **403 Forbidden** on all protected routes.

---

## File Upload & Download

### Upload

- Endpoint: `POST /api/reservations/add` (multipart part named `pdfFile`)
- Max size: 1 MB
- Accepted MIME type: `application/pdf`
- Storage location: `app.upload.dir` property (default: `./uploads` relative to working directory)
- Directory is auto-created on first upload — no manual setup required
- Stored filename format: `{epoch_millis}_{original_filename}` (e.g., `1749300000000_booking.pdf`)
- Only the **filename** is stored in the database (not the full path)

### Download

- Endpoint: `GET /api/reservations/{reservationId}/download`
- File is read from: `Paths.get(uploadDir, documentUrl)`
- Returns raw PDF bytes with Content-Type `application/pdf`
- Returns empty byte array with 200 if file is missing from disk (DB record exists but file deleted)

### Important

> Do not change `app.upload.dir` between uploads and downloads — the filenames stored in the DB will no longer resolve.

---

## Error Handling

All errors are returned as `ErrorDTO`:

```json
{
  "message": "Human-readable error message",
  "fieldName": "The field that caused the error (may be null)",
  "status": "BAD_REQUEST"
}
```

### HTTP Status Codes

| Code | Meaning | When |
|---|---|---|
| 200 | OK | Success |
| 400 | Bad Request | Validation failure, business rule violation |
| 403 | Forbidden | Missing token, expired token, blacklisted token, wrong role |
| 404 | Not Found | Reservation or document does not exist |
| 413 | Payload Too Large | PDF exceeds 1 MB |

### Exception Types

| Class | Status | Typical Cause |
|---|---|---|
| `BadRequestException` | 400 | Invalid amount, bad PDF format, budget not calculated, request not approved, file save failure |
| `IllegalArgumentException` | 400 | Invalid reservation type ID, travel request not found |
| `IllegalStateException` | 400 | Duplicate reservation for same type/travel request |
| `NotFoundException` | 404 | Reservation or document not found by ID |
| `DocumentSizeLimitExceededException` | 413 | PDF > 1 MB |

---

## Inter-service Communication

### Feign Client: auth-service

```
GET http://localhost:8080/auth/blacklist/check?token={token}
Returns: Boolean
```

Called by `JwtAuthFilter` on every authenticated request. If auth-service is unreachable, the check is skipped (fail-open behaviour) — only local signature/expiry validation applies.

### Feign Client: travel-planner

```
GET http://localhost:8082/api/travelrequests/{travelRequestId}
Returns: ObjectNode (JSON)
```

Called during `addReservation`. Fields used from the response:

| Field | Type | Purpose |
|---|---|---|
| `requestId` | Long | Travel request identifier |
| `requestStatus` | String | Must be `"APPROVED"` |
| `fromDate` | String (ISO datetime) | Travel start date for date validation |
| `approvedBudget` | Long | Budget cap base for amount validation |

### Token Forwarding (`FeignAuthInterceptor`)

All outgoing Feign calls automatically carry the incoming `Authorization: Bearer <token>` header. This is required because travel-planner also validates JWT on its endpoints.

---

## Known Constraints & Notes

### Cross-service ID references (no foreign keys)

`reservations.travel_request_id` and `reservations.reservation_done_by_employee_id` reference entities in other microservices. There are **no database-level foreign keys** for these — only application-level validation via Feign.

### No UserDetailsService

Unlike account-management and travel-planner, this service does **not** query the employee database for authentication. The JWT `role` claim is trusted directly. This keeps the service stateless and independent.

### H2 AUTO_SERVER

`AUTO_SERVER=TRUE` in the H2 JDBC URL allows multiple JVM connections to the same H2 file database. This is necessary during development when both Hibernate and the H2 console access the file simultaneously.

### Type ID ordering is critical

Reservation type IDs (Flight=1, Train=2, Bus=3, Cab=4, Hotel=5) are used in business logic by literal `Long` values. If the `reservation_types` table is ever dropped and re-created, these IDs will be re-assigned by the identity sequence starting from 1 — which is correct **as long as** `DataInitializer` seeds them in the same order (Flight, Train, Bus, Cab, Hotel). Never manually insert or reorder them.

### Java 21 — unnamed variables are not standard

`catch (Exception _)` is a preview feature in Java 21 and must not be used. Always use a named variable in catch blocks (`catch (Exception e)`). This is standard only from Java 22 onwards.
