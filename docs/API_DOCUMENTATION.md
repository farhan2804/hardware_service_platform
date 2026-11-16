# API Documentation

## 1. Purpose and source of truth

This document describes the REST endpoints declared in the submitted Spring Boot source code. It was prepared by inspecting the backend controller classes, request/response DTOs, enums, and Spring Security configuration in the submitted project archive.

It documents the routes and data structures found in that source snapshot. It is not a claim that every endpoint has been runtime-tested or that every frontend screen currently uses every endpoint.

**Backend base path:** `/api` for application endpoints.

**Local backend address used by the frontend fallback:** `http://localhost:8080`

The deployed API host is environment-specific and is not hard-coded here. The frontend reads its API base URL from `VITE_API_BASE_URL` and appends `/api`.

## 2. General request conventions

- Request and response bodies use JSON where a body is defined.
- Authenticated endpoints use a JWT access token in the HTTP `Authorization` header.
- The expected token format is `Bearer <token>`.
- Path variables are shown in braces, for example `{bookingId}`.
- Query parameters are shown after `?`, for example `?available=true`.
- Enum values must match the exact uppercase names listed in Section 10.
- Date-time fields use Java `LocalDateTime` values. The source code does not define a custom date-time format in these DTOs; use ISO-8601 local date-time strings such as `2026-11-20T10:00:00` when sending JSON.

### Authorization header

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

## 3. Authentication endpoints

Base path: `/api/auth`

These routes are permitted without an existing JWT according to `SecurityConfig`.

### 3.1 Register a customer

`POST /api/auth/register`

Creates a new customer account. The controller explicitly assigns the `CUSTOMER` role to public registrations. A caller cannot select an administrator or engineer role through this request.

Request body:

```json
{
  "fullName": "Aarav Kumar",
  "email": "aarav@example.com",
  "password": "ExamplePass123",
  "phone": "9876543210"
}
```

Fields:
- `fullName`: required by the DTO constraints; maximum 100 characters.
- `email`: required and expected to be a valid email address.
- `password`: required; DTO constraints specify 8 to 100 characters.
- `phone`: required by the DTO constraints.

Response fields:
- `id`
- `fullName`
- `email`
- `phone`
- `role`
- `message`

The controller returns the message `Registration successful` when the service completes.

**Validation note:** Although `RegisterRequest` declares Jakarta validation annotations, this controller method does not annotate the request parameter with `@Valid`. Do not assume those DTO constraints are automatically enforced by this endpoint without runtime verification or a code change.

### 3.2 Log in

`POST /api/auth/login`

Authenticates a user and returns a JWT.

Request body:

```json
{
  "email": "aarav@example.com",
  "password": "ExamplePass123"
}
```

Response fields:
- `token`
- `tokenType` (the controller sets this to `Bearer`)
- `userId`
- `fullName`
- `email`
- `role`

The login request DTO declares non-blank and email-format constraints, but the controller does not use `@Valid` on the request parameter. Authentication is performed through Spring Security's `AuthenticationManager`.

## 4. Service category endpoints

Base path: `/api/categories`

| Method | Endpoint | Access declared in source | Purpose |
|---|---|---|---|
| GET | `/api/categories` | Public | List active service categories |
| POST | `/api/categories` | ADMIN | Create a service category |
| PUT | `/api/categories/{id}` | ADMIN | Update a service category |
| DELETE | `/api/categories/{id}` | ADMIN | Deactivate a service category |

### 4.1 List active categories

`GET /api/categories`

Returns a list of active `ServiceCategory` entities. The entity contains:
- `id`
- `name`
- `description`
- `active`

### 4.2 Create a category

`POST /api/categories`

Requires the `ADMIN` role.

Example request body:

```json
{
  "name": "Laptop Repair",
  "description": "Hardware diagnosis and repair for laptops",
  "active": true
}
```

Returns the created `ServiceCategory` entity. The controller declares HTTP `201 Created`.

### 4.3 Update a category

`PUT /api/categories/{id}`

Requires the `ADMIN` role.

Uses a `ServiceCategory` JSON request body with the entity fields `name`, `description`, and `active`. Returns the updated entity.

### 4.4 Deactivate a category

`DELETE /api/categories/{id}`

Requires the `ADMIN` role. The service method is named `deactivateCategory`; this endpoint is intended to deactivate the category rather than document a hard-delete operation. The controller declares HTTP `204 No Content`.

## 5. Booking endpoints

Base path: `/api/bookings`

The booking controller's customer-facing operations explicitly resolve the authenticated user and verify that the user has the `CUSTOMER` role. The individual booking read/history operations also check booking ownership.

| Method | Endpoint | Access declared in source | Purpose |
|---|---|---|---|
| POST | `/api/bookings` | Authenticated CUSTOMER | Create a booking and trigger automatic engineer allocation |
| GET | `/api/bookings/my` | Authenticated CUSTOMER | List the current customer's bookings |
| GET | `/api/bookings/{id}` | Authenticated CUSTOMER, owner only | Get one booking |
| GET | `/api/bookings/{id}/history` | Authenticated CUSTOMER, owner only | Get booking status history |

### 5.1 Create a booking

`POST /api/bookings`

Request body:

```json
{
  "serviceCategoryId": 1,
  "serviceType": "REPAIR",
  "issueDescription": "Laptop does not power on",
  "preferredDateTime": "2026-11-20T10:00:00",
  "preferredEndDateTime": "2026-11-20T12:00:00",
  "serviceAddress": "12 Example Street, Jamshedpur",
  "latitude": 22.8046,
  "longitude": 86.2029
}
```

Request fields:
- `serviceCategoryId`: required.
- `serviceType`: required; must be a `ServiceType` enum value.
- `issueDescription`: required; maximum 1000 characters.
- `preferredDateTime`: required start date-time.
- `preferredEndDateTime`: required end date-time.
- `serviceAddress`: required; maximum 500 characters.
- `latitude`: required; range -90 to 90.
- `longitude`: required; range -180 to 180.

The controller uses `@Valid` for `BookingRequest`, so the declared DTO constraints are applied. After saving the booking, the controller calls automatic engineer allocation and then fetches the updated booking.

The controller returns HTTP `201 Created` with a booking response.

Booking response fields:
- `bookingId`
- `serviceCategory`
- `serviceType`
- `issueDescription`
- `preferredDateTime`
- `preferredEndDateTime`
- `serviceAddress`
- `status`
- `assignedEngineerName` (may be `null` if no engineer is assigned)
- `createdAt`

### 5.2 List the current customer's bookings

`GET /api/bookings/my`

Requires an authenticated customer. Returns a JSON array of booking response objects.

### 5.3 Get one booking

`GET /api/bookings/{id}`

Requires an authenticated customer who owns the requested booking. The controller returns `404 Not Found` when the booking cannot be found and `403 Forbidden` when the booking belongs to another customer.

### 5.4 Get booking status history

`GET /api/bookings/{id}/history`

Requires an authenticated customer who owns the requested booking.

Each history item contains:
- `previousStatus`
- `newStatus`
- `changedByName`
- `note`
- `changedAt`

## 6. Engineer endpoints

Base path: `/api/engineers`

| Method | Endpoint | Access declared in source | Purpose |
|---|---|---|---|
| POST | `/api/engineers` | ADMIN | Create an engineer account and profile |
| GET | `/api/engineers` | ADMIN | List engineers |
| POST | `/api/engineers/{engineerId}/skills` | ADMIN | Assign a service skill to an engineer |
| GET | `/api/engineers/{engineerId}/skills` | ADMIN | List an engineer's skills |
| GET | `/api/engineers/me` | ENGINEER | Get the signed-in engineer's profile |
| PUT | `/api/engineers/me/availability?available={boolean}` | ENGINEER | Update own availability |
| GET | `/api/engineers/me/jobs` | ENGINEER | List jobs assigned to the signed-in engineer |
| POST | `/api/engineers/me/jobs/{bookingId}/accept` | ENGINEER | Accept an assigned booking |
| PUT | `/api/engineers/me/jobs/{bookingId}/status?status={status}` | ENGINEER | Update booking progress |

### 6.1 Create an engineer

`POST /api/engineers`

Requires the `ADMIN` role.

Request body:

```json
{
  "fullName": "Rohan Singh",
  "email": "rohan@example.com",
  "password": "ExamplePass123",
  "phone": "9876543211",
  "serviceArea": "Jamshedpur",
  "latitude": 22.8046,
  "longitude": 86.2029,
  "serviceRadiusKm": 15.0
}
```

The request DTO declares:
- `fullName`: required, maximum 100 characters.
- `email`: required, valid email.
- `password`: required, 8 to 100 characters.
- `phone`: required.
- `serviceArea`: required.
- `latitude` and `longitude`: optional in this DTO.
- `serviceRadiusKm`: defaults to 15.0 in the request DTO if omitted.

The controller uses `@Valid` and declares HTTP `201 Created`.

### 6.2 List engineers

`GET /api/engineers`

Requires the `ADMIN` role. Returns a list of engineer profile response objects.

Engineer response fields:
- `engineerId`
- `userId`
- `fullName`
- `email`
- `phone`
- `serviceArea`
- `latitude`
- `longitude`
- `serviceRadiusKm`
- `available`
- `workingHoursStart`
- `workingHoursEnd`

### 6.3 Assign an engineer skill

`POST /api/engineers/{engineerId}/skills`

Requires the `ADMIN` role.

Request body:

```json
{
  "serviceCategoryId": 1,
  "serviceType": "REPAIR"
}
```

Both fields are required by the request DTO. The controller declares HTTP `201 Created`.

Response fields:
- `skillId`
- `serviceCategory`
- `serviceType`

### 6.4 List engineer skills

`GET /api/engineers/{engineerId}/skills`

Requires the `ADMIN` role. Returns a JSON array of skill response objects.

### 6.5 Get own engineer profile

`GET /api/engineers/me`

Requires the `ENGINEER` role. Returns the engineer response fields listed above.

### 6.6 Update own availability

`PUT /api/engineers/me/availability?available=true`

Requires the `ENGINEER` role. The `available` value is a required boolean query parameter. The endpoint returns the updated engineer profile.

Example:

```http
PUT /api/engineers/me/availability?available=false
```

### 6.7 List assigned jobs

`GET /api/engineers/me/jobs`

Requires the `ENGINEER` role. Returns a JSON array of booking response objects for the signed-in engineer.

### 6.8 Accept an assigned booking

`POST /api/engineers/me/jobs/{bookingId}/accept`

Requires the `ENGINEER` role. No request body is declared. Returns the updated booking response.

### 6.9 Update booking progress

`PUT /api/engineers/me/jobs/{bookingId}/status?status=ON_THE_WAY`

Requires the `ENGINEER` role. The `status` query parameter must be a valid `BookingStatus` enum value. Returns the updated booking response.

## 7. Admin endpoints

Base path: `/api/admin`

| Method | Endpoint | Access declared in source | Purpose |
|---|---|---|---|
| GET | `/api/admin/bookings` | ADMIN | List all bookings for the admin dashboard |
| PUT | `/api/admin/bookings/{bookingId}/assign/{engineerId}?reason={reason}` | ADMIN | Manually assign or reassign an engineer |

### 7.1 List all bookings

`GET /api/admin/bookings`

Requires the `ADMIN` role.

Returns a JSON array. Each item contains:
- `bookingId`
- `customerName`
- `customerEmail`
- `customerPhone`
- `serviceCategory`
- `serviceType`
- `issueDescription`
- `appointmentStart`
- `appointmentEnd`
- `serviceAddress`
- `assignedEngineerName` (may be `null`)
- `status`
- `createdAt`

### 7.2 Manually assign or reassign an engineer

`PUT /api/admin/bookings/{bookingId}/assign/{engineerId}`

Requires the `ADMIN` role.

Optional query parameter:
- `reason`: if omitted or blank, the controller uses `Manually assigned by admin`.

Example:

```http
PUT /api/admin/bookings/25/assign/4?reason=Engineer%20selected%20by%20admin
```

No JSON request body is declared.

Response fields:
- `bookingId`
- `assignedEngineerId`
- `status`
- `message`

## 8. Quotation endpoints

Base path: `/api/quotations`

| Method | Endpoint | Access declared in source | Purpose |
|---|---|---|---|
| POST | `/api/quotations/bookings/{bookingId}` | ENGINEER | Create a quotation for a booking |
| GET | `/api/quotations/bookings/{bookingId}` | CUSTOMER or ENGINEER, with ownership check | View a booking's quotation |
| PUT | `/api/quotations/{quotationId}/respond` | CUSTOMER | Approve or reject a quotation |

### 8.1 Create a quotation

`POST /api/quotations/bookings/{bookingId}`

Requires the `ENGINEER` role. The service receives the signed-in engineer's profile ID.

Request body:

```json
{
  "labourCost": 1200.00,
  "partsCost": 800.00,
  "additionalCharges": 100.00,
  "remarks": "Replacement part and labour"
}
```

Fields:
- `labourCost`
- `partsCost`
- `additionalCharges`
- `remarks`

The controller's request record does not declare validation annotations. The service calculates and persists the quotation; confirm detailed business validation in the service implementation before relying on particular constraints.

### 8.2 View a quotation

`GET /api/quotations/bookings/{bookingId}`

Requires either `CUSTOMER` or `ENGINEER` role. The controller additionally checks that the authenticated user is the booking customer or the assigned engineer. Returns `403 Forbidden` if neither condition is met and `404 Not Found` if the booking or quotation is missing.

Quotation response fields:
- `quotationId`
- `bookingId`
- `labourCost`
- `partsCost`
- `additionalCharges`
- `totalAmount`
- `status`
- `remarks`
- `createdAt`
- `respondedAt`

### 8.3 Respond to a quotation

`PUT /api/quotations/{quotationId}/respond`

Requires the `CUSTOMER` role.

Request body:

```json
{
  "status": "APPROVED"
}
```

The `status` value is a `QuotationStatus` enum. The service receives the authenticated customer's ID and performs the response operation.

## 9. Payment endpoints

Base path: `/api/payments`

All three endpoints are restricted to the `CUSTOMER` role in the controller.

| Method | Endpoint | Access declared in source | Purpose |
|---|---|---|---|
| POST | `/api/payments/quotations/{quotationId}` | CUSTOMER | Create a payment for a quotation |
| POST | `/api/payments/{paymentId}/simulate` | CUSTOMER | Simulate a successful or failed payment |
| GET | `/api/payments/quotations/{quotationId}` | CUSTOMER | Get payment details for a quotation |

### 9.1 Create a payment

`POST /api/payments/quotations/{quotationId}`

Request body:

```json
{
  "paymentMethod": "UPI"
}
```

The controller passes the authenticated customer's ID, quotation ID, and payment method to the payment service.

### 9.2 Simulate a payment

`POST /api/payments/{paymentId}/simulate`

Request body:

```json
{
  "successful": true
}
```

Set `successful` to `true` or `false` to request the corresponding simulated outcome.

**Important:** This is a simulation endpoint in the submitted source. The controller does not show integration with an external payment gateway. Do not describe this as a live payment-provider integration.

### 9.3 Get payment details

`GET /api/payments/quotations/{quotationId}`

Returns payment details for the authenticated customer, if found.

Payment response fields:
- `paymentId`
- `quotationId`
- `amount`
- `paymentMethod`
- `transactionReference`
- `status`
- `paidAt`
- `createdAt`

## 10. Feedback endpoints

Base path: `/api/feedback`

Both endpoints require the `CUSTOMER` role.

| Method | Endpoint | Access declared in source | Purpose |
|---|---|---|---|
| POST | `/api/feedback/bookings/{bookingId}` | CUSTOMER | Submit feedback for a booking |
| GET | `/api/feedback/my` | CUSTOMER | List the signed-in customer's feedback |

### 10.1 Submit feedback

`POST /api/feedback/bookings/{bookingId}`

Request body:

```json
{
  "rating": 5,
  "comment": "The service was completed successfully."
}
```

Fields:
- `rating`: integer; DTO declares a range of 1 to 5.
- `comment`: optional; maximum 1000 characters according to the DTO.

The controller's request parameter is not annotated with `@Valid`, so the DTO's validation annotations should not be assumed to run automatically on this endpoint without verification.

Response fields:
- `feedbackId`
- `bookingId`
- `rating`
- `comment`
- `createdAt`

### 10.2 List own feedback

`GET /api/feedback/my`

Returns a JSON array of feedback response objects for the authenticated customer.

## 11. Health and test endpoints

| Method | Endpoint | Access | Response |
|---|---|---|---|
| GET | `/` | Public | Plain text: `Hardware Service Platform API is running` |
| GET | `/api/test` | Public | Plain text: `OK` |

These endpoints are defined in `HealthController`.

## 12. Enum reference

Use the exact enum spellings below in JSON bodies and query parameters.

### Role

- `CUSTOMER`
- `ENGINEER`
- `ADMIN`

### ServiceType

- `REPAIR`
- `INSTALLATION`
- `MAINTENANCE`
- `DIAGNOSIS`

### BookingStatus

- `REQUEST_RECEIVED`
- `AWAITING_ASSIGNMENT`
- `ENGINEER_ASSIGNED`
- `ACCEPTED`
- `ON_THE_WAY`
- `ARRIVED`
- `QUOTATION_SHARED`
- `WORK_IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

### QuotationStatus

- `PENDING`
- `APPROVED`
- `REJECTED`

### PaymentStatus

- `PENDING`
- `SUCCESSFUL`
- `REFUNDED`
- `FAILED`

## 13. Security and implementation notes

The inspected `SecurityConfig`:
- Permits `/`, `/error`, `/favicon.ico`, `/api/auth/**`, and `/api/test` without authentication.
- Requires authentication for other requests by default.
- Uses stateless session management.
- Registers the JWT authentication filter before `UsernamePasswordAuthenticationFilter`.
- Enables method-level security with `@EnableMethodSecurity`.
- Configures CORS for `http://localhost:5173` and `https://hardwareserviceplatform.netlify.app`.
- Allows `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, and `OPTIONS`.
- Allows `Authorization` and `Content-Type` headers.

Role annotations such as `@PreAuthorize("hasRole('ADMIN')")` are included in the endpoint tables where present in the controllers. Some customer booking operations enforce the customer role and ownership checks directly in controller code.

### Validation caveat

The presence of validation annotations on a request DTO does not alone guarantee validation is executed. In the inspected source, `@Valid` is used for booking creation, engineer creation, and skill assignment. It is not used on the register, login, quotation, payment, or feedback request parameters. Verify or improve those endpoints before claiming all DTO validation is enforced.

### Error response caveat

The controllers explicitly create some `400`/`401`/`403`/`404`-style exceptions or response statuses, but the project source inspected here does not establish one uniform JSON error response schema for every endpoint. Error payloads may be affected by Spring Boot's default exception handling and any additional global exception handling elsewhere in the project. This document therefore does not invent a standard error JSON example.

### Runtime verification status

This documentation was derived from static source inspection. The endpoints, request examples, and response field names should be exercised against the running backend before final submission. In particular, verify:
1. Authentication and role restrictions with real customer, engineer, and admin accounts.
2. Ownership checks for bookings, quotations, payments, and feedback.
3. Booking creation and automatic allocation.
4. Engineer job acceptance and status updates.
5. Quotation approval/rejection and simulated payment outcomes.
6. Validation and error responses for malformed or missing request fields.

## 14. Source files inspected

The endpoint inventory was derived from these backend source areas:

- `controller/`
- `dto/request/`
- `dto/response/`
- `enums/`
- `config/SecurityConfig.java`

If routes or DTOs change, update this document to match the implementation.
