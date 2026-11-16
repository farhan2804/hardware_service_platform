# Demo Preparation Guide

## 1. Purpose

This guide provides a practical sequence for demonstrating the Hardware Service & Repair Booking Platform. It focuses on the customer booking journey, engineer allocation and service workflow, quotation handling, simulated payment, and the administrative view.

This guide is based on a static review of the submitted source code. The application should be run through the checks in this document before the demonstration. Do not describe a step as working until it has been tested in the actual deployed or local environment.

## 2. Recommended demonstration flow

A clear demonstration should follow one booking from creation through its service lifecycle. Prepare the required accounts and data before the presentation so the demo does not depend on creating users or configuring engineers live.

### Step 1: Introduce the application

Explain the problem the platform addresses:

- Customers need a way to request hardware repair, installation, maintenance, or diagnosis.
- The platform records the service request and preferred appointment window.
- Engineers can be associated with service skills and availability.
- Administrators can review bookings and manually assign an engineer when required.
- The service workflow supports quotation approval, simulated payment, and customer feedback.

Keep the introduction brief. Move quickly to the working application.

### Step 2: Show customer login and dashboard

1. Open the deployed frontend or the verified local frontend URL.
2. Sign in using a prepared customer account.
3. Show the customer dashboard and explain the information it displays.
4. Open the service booking form.

**Before the demo:** Confirm that the customer account exists, its password works, and the dashboard can load its data from the backend.

**Important:** The current frontend `/register` route is a placeholder page. Do not demonstrate it as a completed registration experience. The backend has a registration API, but the visible frontend registration page is not implemented in the submitted source.

### Step 3: Create a service booking

On the booking form:

1. Select an active service category.
2. Select a service type: Repair, Installation, Maintenance, or Diagnosis.
3. Enter a clear issue description.
4. Choose a future appointment start and end time.
5. Enter the service address.
6. Enter valid latitude and longitude values.
7. Submit the booking.

Explain that the booking request includes the service category, service type, issue description, preferred appointment window, address, and coordinates.

After submission, confirm that a booking ID or success message is returned. Then open the customer bookings/dashboard view and confirm that the new booking appears.

**Demo data advice:** Use a prepared address and valid coordinates that match the engineer's configured service area. Avoid entering arbitrary coordinates during the presentation.

### Step 4: Explain engineer allocation

The backend contains engineer-allocation logic. Explain the criteria at a high level:

- The engineer must be marked available.
- The engineer must have a matching skill for the requested service category.
- The requested appointment must fit the engineer's configured working hours.
- The engineer must be within the configured service radius, based on the stored coordinates and straight-line distance.
- Overlapping appointments are considered.
- Active workload is used when comparing otherwise eligible engineers.

If the system cannot find an eligible engineer, or the booking does not contain usable coordinates, the booking can remain in `AWAITING_ASSIGNMENT`.

**Demonstration choice:** If automatic allocation has been verified with prepared data, show the assigned engineer on the booking. Otherwise, show the booking status and explain the allocation rules without claiming that a live allocation occurred.

### Step 5: Show the administrator view

1. Sign in using a prepared administrator account in a separate browser session or after signing out.
2. Open the admin dashboard.
3. Locate the customer booking.
4. If it is awaiting assignment, use the available administrative assignment workflow if it has been tested.
5. Explain that the backend supports manual engineer assignment and records assignment history.

Do not promise a complete admin management experience for every area. The submitted frontend has gaps in areas such as category management and payment overview. Use only the controls that are present and verified in the current build.

### Step 6: Show the engineer workflow

1. Sign in using a prepared engineer account.
2. Open the engineer dashboard.
3. Locate the assigned booking, if it is available in the current UI.
4. Demonstrate only the job/status actions that have been tested.

The backend exposes engineer operations for viewing assigned jobs, accepting jobs, changing availability, and updating booking status. The completeness of the corresponding frontend workflow should be checked before the demo. If an action is not available or reliable in the UI, explain the backend capability separately rather than pretending to perform it through the interface.

### Step 7: Explain quotation handling

The backend supports an assigned engineer creating a quotation when the booking is in the `ARRIVED` state. The quotation contains:

- Labour cost
- Parts cost
- Additional charges
- Remarks
- A calculated total amount

The customer can approve or reject the quotation. Approval moves the booking into `WORK_IN_PROGRESS`.

If the frontend does not expose the complete quotation journey in the current build, present this as a backend-supported workflow and use API documentation or a prepared API demonstration only if it has been tested.

### Step 8: Explain payment

The payment feature is a simulation. For an approved quotation, the customer can create a payment record and simulate a successful or failed result.

Be explicit that this is not a live payment gateway integration. Do not enter real card details or claim that money is transferred.

If payment actions are not exposed in the frontend, explain the API workflow rather than presenting it as a completed customer-facing checkout.

### Step 9: Show feedback

The backend supports feedback for a customer's completed booking, with a rating from 1 to 5. A customer can submit feedback once for a completed booking.

Demonstrate the feedback UI only if it is present and has been verified. Otherwise, mention it as a backend capability and state that the current frontend flow is incomplete.

### Step 10: Close with the technical architecture

Summarize the implementation:

- Frontend: React, JavaScript, Vite, React Router, Axios, and CSS.
- Backend: Java 21 and Spring Boot.
- Persistence: PostgreSQL with Spring Data JPA/Hibernate.
- Authentication: Spring Security with JWT-based authentication.
- API communication: REST endpoints between the frontend and backend.

Explain that role-based authorization is enforced by the backend. Avoid implying that every frontend route has a route guard: the submitted `App.jsx` defines routes but does not implement an explicit route-guard wrapper.

## 3. Pre-demo checklist

Complete this checklist before the presentation.

### Environment and deployment

- [ ] Confirm the frontend URL loads without a build or runtime error.
- [ ] Confirm the backend health endpoint responds.
- [ ] Confirm the backend can connect to the configured PostgreSQL database.
- [ ] Confirm the frontend API base URL points to the intended backend environment.
- [ ] Confirm the deployed backend CORS configuration allows the frontend origin.
- [ ] Confirm environment variables are configured on the deployment platform.
- [ ] Check browser developer tools for failed API requests and CORS errors.
- [ ] Check backend logs for startup errors and unexpected exceptions.

### Accounts and data

- [ ] Prepare a customer account.
- [ ] Prepare an engineer account with an engineer profile.
- [ ] Prepare an administrator account.
- [ ] Verify that each account can sign in and has the intended role.
- [ ] Configure an active service category.
- [ ] Configure the engineer's skills, availability, working hours, coordinates, and service radius.
- [ ] Ensure the engineer has no conflicting appointment during the demo slot.
- [ ] Prepare a future appointment window and valid service location coordinates.
- [ ] Keep credentials private. Do not include passwords, JWTs, or secrets in screenshots or documentation.

### Workflow checks

- [ ] Customer login succeeds.
- [ ] Customer dashboard loads.
- [ ] Service categories load in the booking form.
- [ ] Booking submission succeeds.
- [ ] The created booking is visible to the customer.
- [ ] Automatic allocation or manual assignment behaves as expected for the prepared data.
- [ ] Admin and engineer views show the expected booking information.
- [ ] Any status transition used in the presentation succeeds.
- [ ] Quotation creation and customer response work, if included.
- [ ] Simulated payment works, if included.
- [ ] Feedback submission works, if included.

### Presentation readiness

- [ ] Use a clean browser profile or close unrelated tabs.
- [ ] Sign in to each role before the demo, or prepare separate browser profiles.
- [ ] Keep a short set of prepared demo values available.
- [ ] Keep the README, architecture document, ER diagram, database schema, and API documentation accessible.
- [ ] Have screenshots or a short recorded walkthrough available as a fallback, if permitted.
- [ ] Do not show secrets, personal customer information, or unrelated browser notifications.

## 4. Suggested demo dataset

Use test data only. Replace the placeholders below with records that exist in the demo database.

| Record | Suggested preparation |
|---|---|
| Customer | A test customer account with the `CUSTOMER` role |
| Engineer | A test engineer account with an active profile |
| Engineer skill | A skill matching the selected service category |
| Engineer availability | Available, with working hours covering the appointment |
| Engineer location | Coordinates within the booking's service radius |
| Service category | An active category already present in the database |
| Booking | A future appointment with start time earlier than end time |
| Admin | A test account with the `ADMIN` role |

Do not create or publish sample passwords in this guide. Store test credentials securely and enter them only during the demonstration.

## 5. If something fails during the demo

| Problem | Recommended response |
|---|---|
| Frontend cannot reach backend | Check the API base URL, backend availability, CORS configuration, and deployment environment variables. Switch to the verified local environment or prepared screenshots if necessary. |
| Login fails | Verify the account, password, role, backend connection, and database record before retrying. Do not expose tokens or passwords while troubleshooting. |
| No service categories appear | Confirm that active categories exist in the database and that the categories API is responding. |
| Booking is not automatically assigned | Check the engineer's skill, availability, working hours, coordinates, service radius, and overlapping appointments. Explain `AWAITING_ASSIGNMENT` accurately if no engineer qualifies. |
| Engineer cannot see the booking | Check that the booking is assigned to that engineer and that the engineer is signed in with the correct account. |
| Quotation or payment UI is unavailable | Explain the backend-supported workflow and its current frontend limitation. Do not claim that the full UI flow is complete. |
| Deployment is unstable | Use a previously verified local setup or prepared screenshots, and clearly identify them as fallback material. |

## 6. What to emphasize

Focus on the business workflow and the implementation decisions:

1. A customer can submit a structured service request with an appointment window and location.
2. Engineer allocation considers skills, availability, working hours, distance, and scheduling conflicts.
3. Administrators have a manual assignment path when automatic allocation cannot assign an engineer.
4. The service lifecycle includes status tracking and history.
5. Quotations separate labour, parts, and additional charges.
6. Payment status is simulated rather than processed by a real payment provider.
7. The application uses a React frontend, Spring Boot backend, PostgreSQL database, and JWT authentication.

## 7. Important limitations to state accurately

- The visible frontend registration route is a placeholder, even though a backend registration API exists.
- Explicit frontend route guards are not present in the submitted `App.jsx`; backend authorization remains the security boundary.
- Some backend workflows may not have complete frontend screens or interactions.
- Payment is simulated. No live payment gateway is integrated.
- Engineer distance calculation uses straight-line coordinates, not road or travel distance.
- The project source includes limited automated tests. A successful build or live demo should not be described as comprehensive test coverage.
- The documentation and this guide were prepared from source inspection. Runtime behaviour and deployment status must be verified in the actual environment before the presentation.

## 8. Suggested closing statement

"The platform demonstrates the core workflow of a hardware service booking system, from customer request creation and engineer allocation through service tracking, quotation handling, simulated payment, and feedback support. The frontend is built with React, the backend uses Java and Spring Boot, and PostgreSQL stores the application data. Some workflows are currently backend-supported but not fully represented in the frontend, and payment is simulated rather than connected to a live provider."

---

**Final reminder:** Run the complete demo once from start to finish using the actual accounts and deployment you intend to present. Update this guide if the current build differs from the source reviewed here.
