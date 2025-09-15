# Banking Management System

Simple banking backend (Spring Boot) with a static frontend (HTML/CSS/JS) implementing:

Features:
- User signup / login (JWT) and forgot password with OTP (email)
- Deposit money
- Withdraw money
- Transfer money by target username
- Transaction history listing

## Tech Stack
Backend: Spring Boot (Web, Security, Data JPA, Validation, Mail, JWT via jjwt)
Frontend: HTML, CSS, Vanilla JS (fetch + localStorage for token)
Database: MySQL

## Project Structure
```
backend/  # Spring Boot project
frontend/ # Static pages served separately (e.g., VS Code Live Server or simple file server)
```

## Running Backend
1. Create MySQL database:
   ```sql
   CREATE DATABASE banking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Edit `backend/src/main/resources/application.properties` with your DB credentials and mail credentials (use Gmail App Password or another SMTP provider).
3. From `backend` directory run Maven (ensure JDK 17):
   ```bash
   mvn spring-boot:run
   ```
4. Backend listens on `http://localhost:8080`.

Environment properties to adjust:
```
spring.datasource.url=jdbc:mysql://localhost:3306/banking_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
app.jwt.secret=CHANGE_ME_SUPER_SECRET_KEY_256
app.jwt.expiration-ms=3600000
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
```

## API Summary (JSON)
Auth:
- POST /api/auth/signup {username,email,password}
- POST /api/auth/login {username,password}
- POST /api/auth/forgot-password {email}
- POST /api/auth/reset-password {token,newPassword}

Account (Authorization: Bearer <JWT>):
- POST /api/account/deposit {amount}
- POST /api/account/withdraw {amount}
- POST /api/account/transfer {targetUsername,amount}
- GET  /api/account/transactions

## Frontend Usage
Open `frontend/login.html` in a browser (or use a simple static server). After login the JWT is stored in `localStorage` and used for subsequent requests.

## Notes / Improvements (Next Steps)
- Serve frontend from Spring Boot (place under `resources/static`) if desired.
- Add pagination for transactions.
- Add unit/integration tests.
- Improve error feedback on frontend (display server messages).
- Add rate limiting / account lockout for security.
- Encrypt sensitive configuration via environment variables or Vault.
- Use Flyway or Liquibase for DB migrations.

## Disclaimer
This is an educational example and omits production-grade hardening (comprehensive validation, auditing, monitoring, etc.). Do not use as-is for real banking.
