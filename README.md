# Hardware Service & Repair Booking Platform

A full-stack prototype for booking repair, maintenance, installation and diagnostic services for electronic and hardware devices. It supports customer bookings, engineer allocation, service-status tracking, quotations, simulated payments and feedback.

> **Project status:** This README describes the current source snapshot. Some UI integrations and supporting features remain incomplete; see [Known Limitations](#known-limitations). Confirm any changes made after this ZIP was created before final submission.

## 1. Technology Stack

**Frontend:** React 19, JavaScript, Vite, React Router, Axios and CSS.

**Backend:** Java 21, Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA/Hibernate, Spring Security, JWT (`jjwt` 0.12.6) and Jakarta Bean Validation.

**Database and deployment:** PostgreSQL, Maven and Docker. The repository contains a backend Dockerfile. The frontend is configured for Netlify and its development environment points to a Render-hosted backend.

## 2. Main Features

### Customer
- Sign in using JWT-based authentication.
- Browse active service categories.
- Submit a booking with service details, issue description, preferred time and service location.
- View personal bookings, booking details and status history.
- View assigned engineer, quotation and payment information.
- Respond to a quotation and submit feedback.

### Service engineer
- View engineer profile, skills and assigned jobs.
- Update availability through the backend API.
- Accept an assigned job through the backend API.
- Update booking status as work progresses.
- Participate in inspection and quotation workflow.

### Administrator / dispatcher
- View bookings and engineer profiles.
- Create engineer profiles and assign engineer skills.
- Manually assign or reassign an engineer to a booking.
- Create, update and deactivate service categories through the backend API.
- Work with quotation and payment records through the relevant APIs.

Backend API coverage and frontend UI completeness are not identical. See [Known Limitations](#known-limitations).

## 3. Engineer Allocation

The backend allocation service evaluates candidate engineers using service category, service type, availability, working hours, service radius and existing booking schedule. Eligible engineers are ranked using straight-line geographic distance calculated with the Haversine formula. Active workload is also used as a tie-break consideration. If no eligible engineer is available, the booking can remain awaiting assignment rather than being assigned to an unsuitable engineer.

This is a prototype allocation model. Straight-line distance is not road distance or estimated travel time. A production implementation would normally use a mapping/routing service and more comprehensive scheduling rules.

## 4. Booking and Payment Workflow

The booking lifecycle is represented by the `BookingStatus` enum and status-history records. Implemented states include request received, awaiting assignment, engineer assigned, accepted, on the way, arrived, quotation shared, work in progress, completed and cancelled.

A quotation stores labour cost, parts cost, additional charges and total amount. A customer can respond to the quotation. A payment record stores amount, payment method, transaction reference, status and timestamps.

**Payments are simulated.** The application does not process real money and does not integrate a live payment gateway.

## 5. Repository Structure

```text
hardware_service_platform/
├── README.md
├── frontend/
│   ├── public/
│   └── src/
│       ├── context/          # Authentication context
│       ├── pages/
│       │   ├── admin/
│       │   ├── auth/
│       │   ├── customer/
│       │   └── engineer/
│       └── services/         # Axios API client
└── serviceplatform/
    ├── src/main/java/com/hardware/serviceplatform/
    │   ├── config/           # Spring Security, JWT and password config
    │   ├── controller/       # REST API controllers
    │   ├── dto/              # Request and response DTOs
    │   ├── entity/           # JPA entities
    │   ├── enums/            # Roles and workflow states
    │   ├── repository/       # Spring Data repositories
    │   └── service/          # Business logic
    ├── src/main/resources/
    │   └── application.properties
    ├── Dockerfile
    └── pom.xml
```

## 6. Local Setup

### Prerequisites
- JDK 21
- Node.js and npm (compatible with the Vite version declared in `frontend/package.json`)
- PostgreSQL
- Git

### 6.1 Create the database

Create a PostgreSQL database, for example:

```sql
CREATE DATABASE hardware_service_db;
```

The application currently uses `spring.jpa.hibernate.ddl-auto=update`, so Hibernate updates the schema from the JPA entity mappings when the backend starts. For production, use controlled database migrations rather than relying on automatic schema updates.

### 6.2 Configure and run the backend

Open a terminal in `serviceplatform/`. Configure these environment variables:

| Variable | Purpose |
|---|---|
| `DB_URL` | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Secret used to sign JWTs |
| `PORT` | Optional server port; defaults to `8080` |

Example local values (replace the credentials and secret):

```text
DB_URL=jdbc:postgresql://localhost:5432/hardware_service_db
DB_USERNAME=postgres
DB_PASSWORD=your_local_database_password
JWT_SECRET=your_long_random_secret
PORT=8080
```

Run on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Run on macOS/Linux:

```bash
./mvnw spring-boot:run
```

The backend defaults to `http://localhost:8080`.

### 6.3 Configure and run the frontend

Open a terminal in `frontend/`. Create `.env.local` for local development:

```text
VITE_API_BASE_URL=http://localhost:8080
```

The Axios client appends `/api` to this base URL. Do not include `/api` in the variable value.

Install dependencies and start Vite:

```bash
npm install
npm run dev
```

Vite normally serves the frontend at `http://localhost:5173`.

### 6.4 Lint and production build

```bash
npm run lint
npm run build
```

The production frontend build is generated in `frontend/dist/`.

## 7. API Overview

All application API routes use the `/api` prefix. Protected endpoints require a bearer token:

```http
Authorization: Bearer <JWT>
```

| Area | Base path | Purpose |
|---|---|---|
| Authentication | `/api/auth` | Register and sign in |
| Bookings | `/api/bookings` | Create and retrieve bookings and booking history |
| Admin | `/api/admin` | View bookings and manually assign engineers |
| Engineers | `/api/engineers` | Engineer profiles, skills, availability and job actions |
| Service categories | `/api/categories` | List and manage service categories |
| Quotations | `/api/quotations` | Create, retrieve and respond to quotations |
| Payments | `/api/payments` | Create, simulate and retrieve payment records |
| Feedback | `/api/feedback` | Submit and retrieve customer feedback |
| Health | `/api/test` | Basic API test endpoint |

See the separate API documentation for endpoint-level request and response details (to be prepared).

## 8. Authentication and Security Notes

- Authentication uses JWT bearer tokens.
- Passwords are handled through Spring Security's `PasswordEncoder` configuration.
- Spring Security is configured for stateless sessions.
- Backend CORS currently allows `http://localhost:5173` and `https://hardwareserviceplatform.netlify.app`.
- Never put secrets in frontend environment variables. Vite variables prefixed with `VITE_` are included in the browser bundle.
- Review role checks, resource ownership checks, validation and frontend route protection before treating the prototype as production-ready.

## 9. Deployment Configuration

The backend Dockerfile builds the Spring Boot application with Maven using Java 21 and runs the generated JAR. The frontend uses `VITE_API_BASE_URL` to locate the backend. The checked-in `frontend/.env.development` currently points to `https://hardware-service-platform.onrender.com`.

For another deployment, configure the frontend environment variable in the hosting provider and ensure the backend CORS allow-list includes the deployed frontend origin. Record the actual production database provider and deployment steps in the final deployment notes. Do not publish secret values here.

## 10. Demo Data and Assumptions

- Use sample customers, engineers, service categories and bookings for demonstration.
- Engineer locations are represented by latitude/longitude and a configured service radius.
- Allocation uses Haversine straight-line distance, not road routing.
- Engineer working hours and availability are allocation constraints.
- Payment is a clearly labelled simulation; no real transaction is performed.
- This is an assessment prototype and should not be used to collect real customer or payment data.

## 11. Known Limitations

The following gaps were identified in the reviewed source snapshot and should be completed or explicitly described during the demonstration:

- The frontend registration route currently displays a placeholder, although a registration API endpoint exists.
- The admin dashboard needs a payment overview.
- Engineer availability update and job acceptance backend endpoints need complete frontend integration.
- The admin reassignment interface needs a proper engineer selector and a reason field.
- Service-category management needs a complete admin interface.
- Optional booking image upload is not implemented.
- Frontend route-level authentication and role guards need strengthening.
- Automated tests need to cover allocation rules, overlapping bookings, authorization, quotations and payment simulation.
- Deployment and database setup instructions should be confirmed against the actual hosting configuration.

This list describes the reviewed source snapshot. Re-check the code and deployed application before submission if any items have since been completed.

## 12. Assignment Scope

This repository is the prototype for **Assignment 1: Hardware repair and service booking platform**. It is one of the two distinct prototypes requested in the full practical assignment. It focuses on customer, engineer and administrator workflows, engineer allocation, service tracking, quotations and simulated payment handling.

## 13. License

No open-source license has been specified in this repository. This project was created as a practical assessment prototype.
