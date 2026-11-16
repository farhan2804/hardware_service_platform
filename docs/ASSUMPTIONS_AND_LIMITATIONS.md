# Assumptions and Limitations

## 1. Purpose

This document records the business assumptions used by the Hardware Service Platform and the limitations visible in the submitted source-code snapshot.

It distinguishes between:
- **Implemented behaviour:** behaviour directly supported by the inspected source code.
- **Assumption:** a rule or simplification adopted for the current application scope.
- **Limitation / pending work:** a capability that is absent, simplified, or not yet verified.

This document is based on static inspection of the submitted project archive. It does not claim that the deployed application, production database, or every workflow has been runtime-tested.

## 2. Business and System Assumptions

The following assumptions describe the intended operating model for the current application.

| Area | Assumption for the current application |
|---|---|
| User roles | The application distinguishes customers, engineers, and administrators. |
| Public registration | Public self-registration creates a customer account. Engineer and administrator accounts are managed through privileged workflows. |
| Service catalogue | A booking refers to an existing service category and a service type. |
| Booking location | Automatic engineer allocation requires booking latitude and longitude. |
| Engineer location | Automatic allocation requires an engineer profile with latitude and longitude. |
| Engineer skills | An engineer must have a matching service-category and service-type skill to be considered for automatic allocation. |
| Availability | An engineer must be marked available and have working-hour values configured to pass the automatic allocation checks. |
| Appointment time | A booking has a start and end time, and the end must be later than the start for allocation. |
| Service radius | The engineer's configured service radius is used as a maximum straight-line distance for automatic allocation. |
| Payment | The current payment workflow is a simulation, not a confirmed connection to an external payment gateway. |
| Feedback | A customer may submit feedback only for their own completed booking, and only once per booking. |

These are assumptions and operating rules for this implementation. They should not be interpreted as proof of a real-world business policy unless the project owner confirms them.

## 3. Implemented Behaviour Confirmed in Source

The following behaviours are present in the inspected backend source:

1. The public registration controller assigns the `CUSTOMER` role to newly registered users.
2. The backend uses JWT-based stateless authentication through Spring Security.
3. Booking creation invokes the engineer-allocation service.
4. Automatic allocation checks matching engineer skills, availability, coordinates, working hours, service radius, and overlapping bookings.
5. The nearest eligible engineer is selected. Active booking count is used as a tie-breaker when distances are equal.
6. A booking is moved to `AWAITING_ASSIGNMENT` when automatic allocation cannot proceed because booking coordinates are missing or no eligible engineer is found.
7. Manual engineer assignment checks that the selected engineer is available, has the required skill, and has no overlapping appointment.
8. A quotation can be created only by the engineer assigned to the booking and only after the booking reaches `ARRIVED`.
9. A quotation's total is calculated from labour cost, parts cost, and additional charges. Negative or null labour and parts amounts are rejected by the service; null additional charges are treated as zero.
10. A customer can approve or reject their own pending quotation. Approval moves the booking to `WORK_IN_PROGRESS`.
11. A payment record can be created only for an approved quotation, and only one payment record is allowed per quotation.
12. Payment processing is simulated through an endpoint that changes a pending payment to `SUCCESSFUL` or `FAILED`.
13. Feedback is accepted only from the customer who owns a completed booking, with a rating from 1 to 5, and only once per booking.

## 4. Current Implementation Limitations

The following limitations were identified in the submitted source snapshot.

### 4.1 Frontend limitations

| Item | Current source finding | Impact / follow-up |
|---|---|---|
| Registration page | The `/register` route renders a placeholder heading (`Registration Page`). | Build the registration form and connect it to the backend registration endpoint. |
| Frontend route protection | `App.jsx` declares dashboard and booking routes directly without visible role-based route guards. | Add authenticated and role-aware route protection. Backend authorization must remain the security boundary. |
| Engineer availability UI | A backend endpoint exists to update engineer availability, but the inspected frontend route/page code does not establish a complete integrated availability control. | Add and test the control in the engineer interface. |
| Engineer job acceptance UI | A backend job-acceptance endpoint exists, but the inspected frontend does not establish a completed integration for it. | Add the accept action and test the full workflow. |
| Admin payment overview | The inspected admin dashboard does not establish a dedicated payment overview workflow. | Add a payment summary/list if required by the assignment. |
| Admin category management | Backend category create/update/deactivate endpoints exist, but a complete admin management interface is not established in the inspected frontend. | Add category management UI if required. |
| Manual reassignment experience | The backend supports manual assignment. The existing admin UI workflow should be checked for a proper engineer selector and meaningful reason input. | Improve the UI and verify that the reason is sent to the API. |
| Booking image upload | No completed booking image-upload workflow was identified in the inspected source snapshot. | Implement file storage, upload API, validation, and UI only if required by scope. |

### 4.2 Backend and business-logic limitations

| Item | Current source finding | Impact / follow-up |
|---|---|---|
| Automatic allocation distance | The service uses the Haversine formula, which measures straight-line distance. | It does not calculate road distance, traffic, or travel time. |
| Working hours | The allocation logic requires working-hour start and end values. Missing values make an engineer ineligible. | Ensure engineer working hours are configured and validated. |
| Overnight appointments | The allocation service explicitly does not support appointments spanning midnight. | Add overnight scheduling logic if the business requires it. |
| Time-zone handling | The inspected DTO/entity flow uses `LocalDateTime`; no explicit time-zone conversion strategy is established in the inspected source. | Define a consistent time-zone policy before multi-region use. |
| Payment provider | The payment service creates an internal transaction reference and simulates success or failure. | No external payment gateway integration is established by this code. |
| Database schema management | `spring.jpa.hibernate.ddl-auto=update` is configured. | Use versioned migrations for controlled production schema changes. |
| API validation | Some DTOs have validation annotations, but not every corresponding controller parameter uses `@Valid`. | Add `@Valid` where appropriate and test invalid requests. |
| API error consistency | The inspected controllers/services throw exceptions for several error cases. A uniform API error response contract is not established by the inspected source. | Add and test centralized exception handling and a consistent error format. |
| Automated tests | The earlier source audit found sparse automated test coverage, with a context-loading test but no comprehensive workflow test suite identified. | Add tests for authentication, authorization, booking, allocation, quotation, payment, and feedback. |

### 4.3 Security and deployment considerations

1. Spring Security requires authentication for requests outside the configured public paths. Role restrictions are applied in controller methods and method-security annotations where present. Each sensitive endpoint should be tested with customer, engineer, and administrator accounts.
2. Frontend route visibility is not a substitute for backend authorization.
3. The frontend stores the JWT in browser local storage. This is convenient for the current implementation but increases exposure if a cross-site scripting vulnerability is present. Review token storage and frontend security before production use.
4. The configured CORS allowlist contains a local development origin and a specific Netlify origin. Update it if the deployed frontend origin changes.
5. Database credentials and the JWT secret are supplied through environment variables. Real secrets must not be committed to the repository.
6. The source configuration enables SQL logging with `spring.jpa.show-sql=true`. Review this setting for production because SQL logs can be noisy and may expose operational details.

## 5. Scope Boundaries

Unless the assignment explicitly requires them and they are implemented separately, the following should not be claimed as completed features:

- Live payment processing through UPI, cards, wallets, or a bank/payment provider.
- Road-route distance, traffic-aware engineer matching, or estimated travel time.
- Real-time GPS tracking of engineers.
- SMS, WhatsApp, or email notifications.
- Customer/engineer chat or live messaging.
- Booking photo or document upload.
- Production-grade monitoring, alerting, and audit dashboards.
- Comprehensive automated test coverage.

The presence of a related database field, enum, or endpoint does not by itself prove that a complete user-facing or third-party integrated feature exists.

## 6. Verification Still Required

Before final submission, run the application and verify the following against the current code and deployed environment:

- Customer registration and login.
- Access denial when a customer tries to use an engineer/admin operation.
- Access denial when one customer tries to view another customer's booking.
- Booking creation with valid and invalid input.
- Automatic allocation with matching and non-matching skills.
- Allocation when coordinates are missing.
- Allocation when an engineer is unavailable, outside the radius, outside working hours, or has an overlapping booking.
- Admin manual assignment and reassignment.
- Engineer acceptance and booking status progression.
- Quotation creation, customer approval, and customer rejection.
- Simulated payment success and failure.
- Feedback submission for completed and non-completed bookings.
- Frontend behaviour when the API returns validation, authorization, or not-found errors.

Record the actual test outcome separately. Do not mark an item as tested merely because the relevant code or endpoint exists.

## 7. Conclusion

The current project provides a layered service-booking application with JWT authentication, customer bookings, engineer allocation, admin assignment, quotations, simulated payments, and feedback support in the backend.

The main limitations concern incomplete frontend integrations, simplified distance and scheduling logic, simulated rather than gateway-based payment, validation and error-handling consistency, and the need for broader automated and manual testing.

This document should be updated as those items are completed. The final submission should describe the state of the latest committed version, not an earlier development snapshot.
