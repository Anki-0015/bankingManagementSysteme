# Banking Management System – Technical Explanation

## 1. Overview
A full‑stack demo banking platform. Users register, log in (JWT), view balance, perform transactions, reset password via OTP, and (if ADMIN) manage all user accounts (CRUD, role, balances). Stateless backend (Spring Boot + MySQL + JPA). Frontend is plain HTML/CSS/JS (token stored in localStorage). Theme: black / white / gold.

---

## 2. High-Level Architecture
Browser (HTML/JS/CSS)
  ↕ (HTTPS / JSON, Bearer JWT)
Spring Boot REST API
  Controllers → Services → Repositories → MySQL
  Security Filter Chain (JWT)
  Support components (OTP mail, admin bootstrap)

---

## 3. Feature Set & Workflows

### 3.1 Authentication (Signup / Login)
Flow:
1. User submits credentials.
2. Signup: password hashed (BCrypt), User saved with default role USER, balance initialized.
3. Login: credentials validated; on success a JWT is generated (claims: username, role, issuedAt, expiry).
4. JWT returned; frontend stores token + role in localStorage.
5. Subsequent calls include Authorization: Bearer <token>.

### 3.2 JWT Security
- Stateless: No server session.
- Filter extracts token, validates signature & expiration, loads UserDetails, sets SecurityContext.
- Role from token & DB enforces access (e.g. /api/admin/**).

### 3.3 Password Reset via OTP
Flow:
1. User requests OTP (email provided).
2. Backend generates 6‑digit code, stores (DB or in-memory) with expiry.
3. Email service sends OTP.
4. User submits OTP + new password.
5. Backend validates & invalidates OTP; password rehashed & stored.

### 3.4 Account & Balance
- Balance endpoint returns precise numeric value (BigDecimal).
- Transactions logged separately.
- Direct read endpoint prevents computing from aggregates each time.

### 3.5 Transactions
- Create: Adjust balance atomically; persist Transaction entity (type, amount, timestamp).
- List: Service fetches user’s transactions ordered by timestamp; mapped to DTO to avoid lazy-loading issues.

### 3.6 Admin Module
Capabilities:
- List all users.
- Create user (with role).
- Update balance (force set).
- Change role (USER ↔ ADMIN).
- Delete user.
- Secure: ROLE_ADMIN required. Frontend conditionally reveals admin UI if role === ADMIN.

### 3.7 Theming & UX
- Central CSS variables for palette.
- Skeleton loaders while fetching.
- Toast notifications for actions.
- Show/hide password toggles.
- Admin table inline editing.

### 3.8 Default Admin Bootstrap
- On startup creates admin (configurable username/password) if no ADMIN exists.
- Optional password sync on restart.

---

## 4. Technologies & Purpose

Backend:
- Spring Boot: Auto-config, embedded server.
- Spring Web (MVC): REST endpoints.
- Spring Security: Authentication & authorization.
- JWT (io.jsonwebtoken or similar lib): Stateless auth.
- JPA / Hibernate: ORM & entity persistence.
- MySQL: Durable relational storage (ACID semantics).
- HikariCP: Efficient JDBC connection pooling.
- BCrypt (PasswordEncoder): Secure password hashing.
- JavaMail / Email Sender (if implemented): OTP delivery.
- Maven: Build + dependency management.

Frontend:
- Vanilla JS + Fetch API: Lightweight HTTP calls.
- localStorage: Persist JWT + role across page loads.
- HTML/CSS (custom components): Faster load, no SPA overhead.
- CSS Custom Properties: Theme centralization.

Cross-Cutting:
- DTO Pattern: Avoid exposing entities directly.
- Layered Architecture: Separation of concerns.
- Logging: Observability for startup & admin bootstrap.
- Optional Actuator (if added): Health checks.

---

## 5. Core Spring / JPA Annotations Explained

Annotation | Why / What It Does
-----------|--------------------
@SpringBootApplication | Combines @Configuration + @EnableAutoConfiguration + @ComponentScan to bootstrap app.
@RestController | Marks class as REST controller (implicit @ResponseBody on methods).
@RequestMapping / @GetMapping / @PostMapping / @PatchMapping / @DeleteMapping | Maps HTTP method + path to handler methods.
@RequestBody | Binds JSON request body to a method parameter.
@PathVariable | Extracts URI template variable.
@RequestParam | Extracts query parameter.
@Valid | Triggers bean validation (Hibernate Validator) on request body.
@ResponseStatus | Forces a specific HTTP status on return or exception.
@ExceptionHandler | Maps thrown exceptions to custom responses.
@CrossOrigin (if used) | Enables CORS for specified origins.
@Service | Marks service layer bean (business logic).
@Repository | Marks persistence bean; adds exception translation.
@Component | Generic Spring-managed bean.
@Configuration | Defines a configuration class with @Bean methods.
@Bean | Declares a Spring-managed object returned from method.
@EnableWebSecurity | Activates Spring Security’s web security.
@EnableMethodSecurity (if present) | Enables @PreAuthorize style annotations.
@Entity | Marks a JPA entity mapped to a database table.
@Table | Customizes table name / schema (optional).
@Id | Primary key identifier.
@GeneratedValue | Strategy for primary key generation (IDENTITY / AUTO).
@Column | Customizes column (nullable, length, name, precision).
@Enumerated(EnumType.STRING) | Persists enum using its String name (stable vs ordinal).
@ManyToOne / @OneToMany | Defines relational associations between entities.
@JoinColumn | Specifies foreign key column.
@JsonIgnore (if used) | Prevents serialization of property (avoid cycles).
@EventListener(ApplicationReadyEvent) | Runs method after app fully started (used in bootstrap).
@Value("${...}") | Injects property value from application.properties.
@Transactional (if used) | Ensures atomicity; rollback on runtime exceptions.
@ConstructorBinding / @ConfigurationProperties (if used) | Type-safe grouped property binding.
@RequiredArgsConstructor / @Data / @Getter (Lombok) | Generates constructors/getters—reduces boilerplate.

---

## 6. Detailed Feature Mechanics

### 6.1 Signup
- Validate uniqueness (username/email) via repository queries.
- Hash password (BCrypt).
- Assign default role USER if none provided.
- Initialize balance (e.g. 0.00).
- Persist User.
- Return AuthResponse with JWT & metadata.

### 6.2 Login
- AuthenticationManager authenticates (delegates to UserDetailsService + PasswordEncoder).
- On success: Generate JWT with claims (sub=username, role=…).
- Return token & user data.

### 6.3 JWT Validation
- Filter intercepts requests (except permitAll list).
- Extracts Authorization header.
- Verifies signature + expiry.
- Loads User from DB (ensures user still valid).
- Sets Authentication in SecurityContextHolder.

### 6.4 OTP Generation
- Generate random 6-digit numeric code.
- Store with timestamp + username (DB or in-memory map).
- Send via email service.
- Reset endpoint checks:
  - Code matches & not expired.
  - Updates password hash.
  - Invalidates OTP (delete record).

### 6.5 Transactions (Deposit/Withdraw/Transfer future)
- Validate amount > 0.
- For withdraw ensure balance ≥ amount (race-safe if using @Transactional + proper isolation).
- Adjust balance.
- Insert Transaction row (with type, amount, postBalance).
- Return updated balance and/or transaction DTO.

### 6.6 Listing Transactions
- Repository query: findByUserOrderByCreatedAtDesc / Pageable (if added).
- Map entities to DTO (avoid exposing lazy relations).
- Serialize JSON.

### 6.7 Balance Retrieval
- Direct select by user id (fast).
- Avoids recomputing from transaction sum (O(1) vs O(n)).

### 6.8 Admin Operations
- Secured endpoints annotated/configured for ROLE_ADMIN.
- Balance patch: Validates numeric format; sets absolute value (not delta).
- Role patch: Ensures new role is recognized enum.
- Delete user: Removes row (cascade rules define whether transactions remain or are orphaned).
- Create user: Similar to signup but admin-specified role.

### 6.9 Bootstrap Admin
- On ApplicationReadyEvent:
  - Sanitizes role column (NULL/invalid → USER).
  - Checks if any ADMIN exists.
  - Creates default admin if none.
  - Optional sync of password hash if property enabled.

---

## 7. Entities & Relationships (Simplified)

User
- id (PK)
- username (unique)
- email (unique)
- passwordHash
- balance (BigDecimal)
- role (ENUM: USER/ADMIN)
- createdAt (timestamp)
- transactions (OneToMany)

Transaction
- id (PK)
- user (ManyToOne)
- type (DEPOSIT/WITHDRAW/TRANSFER etc.)
- amount
- createdAt
- Optional: postBalance (snapshot)

Rationale:
- Storing postBalance per transaction supports audit and dispute resolution without recalculating.

---

## 8. Security Pipeline (Request Lifecycle)

1. Incoming HTTP request.
2. CORS (if configured).
3. Security filter chain → JWT Filter (skips if login/signup).
4. Token parsed & validated; Authentication set.
5. DispatcherServlet → Controller mapping.
6. Controller → Service → Repository.
7. Response serialized (Jackson).
8. Sent to client (status + JSON body).

---

## 9. Error Handling

Type | Origin | Response Strategy
-----|--------|------------------
BadCredentials | AuthManager | 401 JSON {error:"Invalid username or password"}
Missing JWT | Filter | 401
Malformed JWT | Filter | 401
Validation error | @Valid | 400 with field messages
Data access error | Repository | 500 (could be refined)
IllegalArgument | Service preconditions | 400 or 422 (implementation-specific)
Unhandled runtime | Anywhere | 500 generic

Improvement potential: Uniform error envelope {timestamp, path, status, error, message, trace (dev)}.

---

## 10. Performance & Scalability Considerations

Aspect | Current | Notes
-------|---------|------
Stateless Auth | JWT | Horizontal scaling easy.
DB Load | Single MySQL | Add indexes on username, role, createdAt.
Transactions Query | Simple list | Add pagination for large histories.
Password Hash | BCrypt | Cost factor tunable (balance security vs CPU).
Caching | None | Could cache balance reads (short TTL) if hot.
Concurrency | Depends on transaction boundaries | Use @Transactional for atomic balance changes.
Admin List | Full table load | Add paging + filtering later.

---

## 11. Security Hardening (Potential)

- Add account lock after repeated failed logins.
- Add refresh token & shorter JWT TTL.
- Add CSRF protection for future cookie-based flows (not needed for pure JWT header).
- Enforce password strength & history.
- Audit log table for admin actions.

---

## 12. Frontend Interaction Pattern

Step | Action
-----|-------
Page load | JS checks localStorage token; if absent redirect to login.
Login submit | POST /api/auth/login → store token, role → redirect (admin or dashboard).
Dashboard load | Parallel fetch balance + recent transactions.
Admin panel | GET /api/admin/users → render table; inline PATCH/DELETE via fetch.
Logout | Clear localStorage; redirect login.

---

## 13. Rationale for Key Choices

Choice | Reason
-------|-------
JWT | Stateless, reduces server memory footprint.
BCrypt | Industry-standard adaptive hashing.
DTOs | Prevent lazy loading issues + minimize payload.
Separate balance endpoint | Constant-time retrieval vs summation.
Enum roles (STRING) | Readable, stable across reorder.
Vanilla JS | Simplicity, zero build chain overhead.
Skeleton loaders | Improve perceived performance.
Bootstrap admin | Guarantees control path if DB empty.

---

## 14. Common Pitfalls Avoided / Addressed

Issue | Mitigation
------|-----------
Null roles in legacy rows | Startup sanitation.
Lazy initialization errors | DTO mapping, not exposing entities.
Password mismatch after property change | Optional password sync logic.
Intermittent startup failure | Hikari fail-timeout adjustment + DB readiness approach.
Overfetching users for repair | Moved to raw SQL sanitation (if implemented).

---

## 15. Future Improvements (Optional Roadmap)

Priority | Item
--------|------
High | Pagination & search for admin users.
High | Transaction pagination & filtering.
Medium | Transfer feature with dual-ledger entries.
Medium | Audit log (admin actions).
Medium | Refresh token & token rotation.
Low | WebSocket push for live balance.
Low | Rate limiting OTP & login attempts.

---

## 16. Quick Glossary

Term | Meaning
-----|--------
JWT | Signed token proving identity.
DTO | Data Transfer Object; serialized shape.
BCrypt | Password hashing algorithm with adaptive cost.
ORM | Object-Relational Mapping layer (JPA/Hibernate).
Stateless | Server keeps no session per client.
Principal | Authenticated user identity in security context.

---

## 17. Summary
The system cleanly separates concerns (API, business logic, persistence), uses standard Spring Security + JWT for stateless auth, applies JPA for relational persistence, and offers an admin surface for operational control. It remains extensible: you can add features (transfers, pagination, audits) without reworking core infrastructure.

