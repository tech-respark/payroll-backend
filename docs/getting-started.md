# Getting started

> **Audience:** New backend developers  
> **Purpose:** Run the API against a local MySQL database  
> **Last verified:** 2026-10-19  
> **Source of truth:** `pom.xml`, `application.properties`, and `PayrollManagementApplication`

## Prerequisites

- Java 21
- Maven 3.9 or newer
- MySQL 8 with a local `payroll_management` database
- A local JKS keystore, because HTTPS is enabled
- The frontend repository when exercising the UI

The committed datasource default uses port 3306. A machine that already has another MySQL server on 3306 can run this project's MySQL on another port and override the JDBC URL.

## First run

```bash
export JAVA_HOME='<path-to-java-21>'
export SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3307/payroll_management?useSSL=false&characterEncoding=utf8&useUnicode=true'
export DB_USERNAME='<local-user>'
export DB_PASSWORD='<local-database-secret>'

mvn spring-boot:run
```

Expected startup:

1. Java 21 is the runtime reported by Maven.
2. Hikari connects to `payroll_management`.
3. Tomcat starts on HTTPS port 8092.
4. The log says `Started PayrollManagementApplication`.

Place the local keystore at `src/main/resources/keystore.jks`. `*.jks` is gitignored. The alias and store password must match the server SSL settings. Keep those values out of Git and out of documentation.

## Confirm it responds

From another terminal, call the public login route with an account that exists in the local database:

```bash
curl -sk -H 'Content-Type: application/json' \
  -d '{"username":"<local-username>","password":"<local-account-secret>"}' \
  https://127.0.0.1:8092/payroll-management/v1/login
```

A valid account returns `success: true` and a token. An unknown account returns HTTP 401 and `success: false`.

## Frontend connection

Start the frontend with `npm run dev` in `payroll-frontend`. Open `http://localhost:5174/login`. Vite forwards `/payroll-management/v1` to `https://localhost:8092` and disables certificate verification for that local proxy only.

## Optional dependencies

Login and database-backed screens do not require every integration. These features fail or no-op until configured:

| Feature | Dependency |
| --- | --- |
| Password-reset OTP notification | RabbitMQ |
| Leave and regularization email | SMTP |
| Biometric pull | Tenant vendor URL and credentials in the database |
| External staff import | Tenant external URL, app name, and store branch ID |

## Schema warning

`spring.jpa.hibernate.ddl-auto` is `update`. Starting the API can alter the local schema to match entities. Use a disposable local database, not a shared server. There is no Flyway or Liquibase history yet.
