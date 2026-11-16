# AI Tools and Libraries

## 1. Purpose and accuracy note

This document records the development tools, frameworks, and libraries identifiable in the submitted project source snapshot.

**Important:** The inspected project does not contain an identified AI/ML feature, AI model integration, LLM API client, machine-learning framework, or AI agent workflow. Therefore, this document does not claim that an AI tool or AI library was used to implement the application's runtime functionality.

The project is a conventional React + Spring Boot + PostgreSQL service-booking application. If AI assistants were used during development for coding support, that should be disclosed separately and only according to the actual tools and extent of use.

## 2. AI Tools and AI Libraries

| Category | Finding from submitted source |
|---|---|
| AI/LLM API integration | No AI or LLM API integration identified in the source or dependency manifests. |
| Machine-learning framework | No TensorFlow, PyTorch, scikit-learn, or similar ML framework identified. |
| Generative AI framework | No LangChain, LlamaIndex, or similar framework identified. |
| Embedding or vector database | No embedding model or vector database integration identified. |
| AI model files | No model weights or model-serving code identified in the submitted project archive. |

**Conclusion:** No AI-powered runtime feature is documented for this version of the application. Do not list AI libraries as project dependencies unless they are added to the codebase and used.

## 3. Frontend Technologies and Libraries

The frontend dependency information is taken from `frontend/package.json`.

| Technology / library | Version declared | Purpose |
|---|---:|---|
| React | `^19.3.1` | Builds the component-based user interface. |
| React DOM | `^19.3.1` | Renders React components in the browser DOM. |
| React Router DOM | `^7.18.5` | Defines client-side application routes. |
| Axios | `^1.21.1` | Sends HTTP requests from the frontend to the backend REST API. |
| Vite | `^8.3.6` | Frontend development server and build tool. |
| `@vitejs/plugin-react` | `^6.1.2` | Enables React support in Vite. |
| ESLint | `^10.14.0` | Static code linting. |
| `eslint-plugin-react-hooks` | `^7.1.1` | Provides lint rules for React Hooks. |
| `eslint-plugin-react-refresh` | `^0.5.7` | Provides lint support related to React Fast Refresh. |
| `@eslint/js` | `^10.0.1` | ESLint's JavaScript recommended configuration. |
| `globals` | `^17.13.0` | Provides global-variable definitions for ESLint configuration. |
| `@types/react` | `^19.3.0` | React type declarations included in the development dependencies. |
| `@types/react-dom` | `^19.3.0` | React DOM type declarations included in the development dependencies. |

### Frontend implementation notes

- The application source uses JavaScript and JSX files.
- Styling is implemented with CSS files.
- No Tailwind CSS, Bootstrap, Material UI, or other component/UI framework is listed in the frontend package manifest.
- No Redux or other dedicated state-management library is listed. The inspected source uses React Context for authentication state.

### Frontend scripts

The `frontend/package.json` defines these scripts:

| Command | Purpose |
|---|---|
| `npm run dev` | Starts the Vite development server. |
| `npm run build` | Creates the production frontend build. |
| `npm run lint` | Runs ESLint. |
| `npm run preview` | Serves the built frontend locally for preview. |

## 4. Backend Technologies and Libraries

The backend dependency information is taken from `serviceplatform/pom.xml`.

| Technology / library | Version declared | Purpose |
|---|---:|---|
| Java | `21` | Backend programming language and configured Java version. |
| Spring Boot | `4.1.2` | Backend application framework and dependency-management parent. |
| Spring Boot Data JPA starter | Managed by Spring Boot `4.1.2` | Integrates Spring Data JPA for persistence. |
| Spring Boot Security starter | Managed by Spring Boot `4.1.2` | Provides authentication and authorization infrastructure. |
| Spring Boot Validation starter | Managed by Spring Boot `4.1.2` | Provides Jakarta Bean Validation integration. |
| Spring Boot Web MVC starter | Managed by Spring Boot `4.1.2` | Provides Spring MVC and REST endpoint support. |
| JJWT API | `0.12.6` | JWT creation and parsing API used by the authentication implementation. |
| JJWT implementation | `0.12.6` | Runtime implementation for JJWT. |
| JJWT Jackson integration | `0.12.6` | Runtime JSON integration for JJWT. |
| PostgreSQL JDBC driver | Managed by Spring Boot `4.1.2` | Connects the backend to PostgreSQL at runtime. |

### Backend implementation notes

- Spring Data JPA repositories are used for database access.
- Hibernate is used through the JPA persistence setup managed by Spring Boot.
- Spring Security is configured for stateless JWT authentication.
- BCrypt password encoding is used through Spring Security's password-encoding support.
- Jakarta Persistence annotations define entity mappings.
- Jakarta Validation annotations are present on request DTOs and entity fields. Whether validation executes for a particular endpoint also depends on controller usage, such as `@Valid`.
- JJWT is used for token handling; it is not an AI library.

## 5. Backend Testing Dependencies

The Maven file declares these test-scope dependencies, managed by Spring Boot `4.1.2`:

- `spring-boot-starter-data-jpa-test`
- `spring-boot-starter-security-test`
- `spring-boot-starter-validation-test`
- `spring-boot-starter-webmvc-test`

The inspected source snapshot contains a Spring Boot context-loading test. The presence of test dependencies does not mean that comprehensive automated tests exist or have passed. Test coverage and execution should be reported separately based on actual test runs.

## 6. Development and Build Tools

The submitted project files establish the following:

| Tool | Evidence in project | Purpose |
|---|---|---|
| npm | `frontend/package.json` and `package-lock.json` | Installs and manages frontend dependencies and runs frontend scripts. |
| Node.js | Required runtime for the npm/Vite toolchain | Runs frontend development and build tooling. |
| Maven Wrapper | `serviceplatform/mvnw` and `mvnw.cmd` | Runs Maven using the wrapper configuration for the backend. |
| Maven | `serviceplatform/pom.xml` | Defines backend dependencies and build configuration. |
| Docker | `serviceplatform/Dockerfile` | Provides a Docker build definition for the backend. |
| Git | Project source-control workflow | Tracks code and documentation changes. |

The submitted archive does not establish which specific IDE, operating system, or cloud deployment dashboard was used by the developer. Those should only be added if they reflect the actual development environment.

## 7. Database Technology

| Technology | Purpose |
|---|---|
| PostgreSQL | Relational database used to persist application data. |
| Spring Data JPA / Hibernate | Object-relational persistence between Java entities and relational tables. |
| PostgreSQL JDBC driver | Runtime database connectivity. |

The database connection is configured through environment variables in `application.properties`:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

The inspected configuration uses Hibernate `ddl-auto=update`. This is a schema-update setting, not a database migration tool.

## 8. Summary

The submitted application uses the following principal stack:

- **Frontend:** React, React DOM, React Router DOM, Axios, Vite, and CSS.
- **Backend:** Java 21, Spring Boot 4.1.2, Spring MVC, Spring Security, Spring Data JPA, Jakarta Validation, and JJWT 0.12.6.
- **Database:** PostgreSQL with the PostgreSQL JDBC driver.
- **Build and development:** npm, Node.js, Maven Wrapper, and Docker support.

No AI/ML library or AI service integration was identified in the submitted source snapshot. If AI coding assistants were used while developing the project, add a separate disclosure describing the actual assistant(s) and how they were used. Do not describe those assistants as runtime dependencies of the application.
