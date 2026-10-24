# Getting started

> **Audience:** New backend developers  
> **Purpose:** Run the API against a local MySQL database  
> **Last verified:** 2026-10-23  
> **Source of truth:** `pom.xml`, `application.properties`, and `PayrollManagementApplication`

## Prerequisites

- Java 21
- Maven 3.9 or newer
- MySQL 8 with a local `payroll_management` database
- A local JKS keystore, because HTTPS is enabled
- The frontend repository when exercising the UI

Tracked `application.properties` has no credential defaults. Local secrets belong in gitignored `src/main/resources/application-local.properties` or in environment variables.

## First run

Copy the example local profile and fill values that exist only on this machine:

```bash
cp src/main/resources/application-local.example.properties src/main/resources/application-local.properties
```

`jwt.secret` must be at least 32 bytes. SSL keystore passwords must match `keystore.jks`.

```bash
export JAVA_HOME='<path-to-java-21>'
export SPRING_PROFILES_ACTIVE=local

mvn spring-boot:run
```

Environment-variable alternative (no local profile file):

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3307/payroll_management?useSSL=false&characterEncoding=utf8&useUnicode=true'
export DB_USERNAME='<local-user>'
export DB_PASSWORD='<local-database-secret>'
export JWT_SECRET='<local-secret-at-least-32-bytes>'
export SSL_KEY_STORE_PASSWORD='<local-keystore-secret>'
export SSL_TRUST_STORE_PASSWORD='<local-keystore-secret>'

mvn spring-boot:run
```

Expected startup:

1. Java 21 is the runtime reported by Maven.
2. Hikari connects to `payroll_management`.
3. Tomcat starts on HTTPS port 8092.
4. The log says `Started PayrollManagementApplication`.
5. Missing or short `jwt.secret` fails startup with `IllegalStateException`.

Place the local keystore at `src/main/resources/keystore.jks`. `*.jks` is gitignored. The alias and store password must match the server SSL settings. Keep those values out of Git and out of documentation.

## Confirm it responds

From another terminal, call the public login route with an account that exists in the local database:

```bash
curl -sk -H 'Content-Type: application/json' \
  -d '{"username":"<local-username>","password":"<local-account-secret>"}' \
  https://127.0.0.1:8092/payroll-management/v1/login
```

A valid active account returns `success: true` and a token. An unknown, inactive, or wrong-password account returns HTTP 401 and `success: false`. `/actuator/health` is public; other actuator paths require a JWT.

QA and production deployment, secret injection, reverse proxy, and acceptance checks live in the frontend portal: `payroll-frontend` `docs/operations/qa-and-production-deployment.md`. Do not copy this machine's `application-local.properties` onto a server.

## Frontend connection

Start the frontend with `npm run dev` in `payroll-frontend`. Open `http://localhost:5174/login` for client staff or `http://localhost:5174/ops/login` for the internal console. Vite forwards `/payroll-management/v1` to `https://localhost:8092` and disables certificate verification for that local proxy only. The first boot seeds a master platform user when `platform_user` is empty.

## Optional dependencies

Login and database-backed screens do not require every integration. These features fail or no-op until configured:

| Feature | Dependency |
| --- | --- |
| Password-reset OTP notification | RabbitMQ |
| Leave and regularization email | SMTP |
| Biometric pull | Tenant vendor URL and credentials in the database |
| External staff import | Tenant external URL, app name, and store branch ID |

## Schema warning

`spring.jpa.hibernate.ddl-auto` defaults to `update` and can be overridden with `HIBERNATE_DDL_AUTO`. Starting the API can alter the local schema to match entities. Use a disposable local database, not a shared server. There is no Flyway or Liquibase history yet.
