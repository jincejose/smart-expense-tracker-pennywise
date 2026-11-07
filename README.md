# Pennywise — Java Smart Expense Tracker

Pennywise is a Java full stack expense tracker built with Spring Boot, Thymeleaf, HTML, CSS, a small amount of browser JavaScript, and PostgreSQL. The same expense and budget features are available as JSON REST APIs for Postman.

## Run the app

Requirements: Java 17 or later, Maven 3.6.3 or later, and Docker Desktop or a local PostgreSQL server.

Start PostgreSQL with Docker Compose:

```powershell
docker compose up -d postgres
```

Start Pennywise from the project folder:

```powershell
.\mvnw.cmd spring-boot:run
```
Hosted on Render : https://pennywise-rvxa.onrender.com/

Open <http://localhost:8080>. On a new database, the app creates the expense and budget tables and adds a small set of sample entries. Database settings can be overridden with `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`.

The simple sign-in page protects the tracker and API. Development defaults are username `admin` and password `pennywise`. Set `APP_USERNAME` and `APP_PASSWORD` before starting the app to change them. Use a strong password before exposing the app beyond your own machine.

If Maven is already installed, `mvn spring-boot:run` and `mvn test` work too.

## Deploy on Render

The app also runs as a Spring Boot web service on Render. The repository includes a Dockerfile and `render.yaml` Blueprint for Render.

1. In Render, choose **New + → Blueprint** and connect `jincejose/smart-expense-tracker-pennywise`.
2. Select the `master` branch and approve the `render.yaml` Blueprint. It creates the web service and PostgreSQL database in Oregon.
3. After the first deploy, open the `pennywise` service and check **Environment** for the generated `APP_PASSWORD`. Sign in with username `admin`.

The Blueprint uses Render's free plans. Free web services can sleep when idle, and free PostgreSQL databases are temporary and have limited storage; use a paid database plan for expense data you need to keep long term.

## PostgreSQL connection

The default connection matches `compose.yaml`:

- Database: `pennywise`
- Username: `postgres`
- Password: `postgres`
- Port: `5432`

For another PostgreSQL server, set environment variables before starting the app:

```powershell
$env:DATABASE_URL = 'jdbc:postgresql://localhost:5432/pennywise'
$env:DATABASE_USERNAME = 'postgres'
$env:DATABASE_PASSWORD = 'your-password'
```

## Postman

Import [`postman/Pennywise.postman_collection.json`](postman/Pennywise.postman_collection.json) into Postman. It includes health, expense list/create/update/delete, and budget list/save requests. The collection uses `http://localhost:8080` by default, supplies HTTP Basic authentication with the development credentials, and stores the created expense ID for the follow-up update and delete requests.

The API routes are:

- `GET /api/health`
- `GET /api/expenses?month=2026-11&category=Food%20%26%20dining&search=coffee`
- `GET`, `POST /api/expenses`
- `GET`, `PUT`, `DELETE /api/expenses/{id}`
- `GET`, `PUT /api/budgets`

## Interface features

- Add, edit, search, filter, and delete daily expenses
- Monthly category budgets with 80% budget alerts
- Monthly report, six month spending bars, and downloadable CSV
- Month-end forecast based on the current daily average
- INR-only amounts and responsive desktop/mobile layouts
- Sign-in and sign-out for the tracker and authenticated API

All stored expense amounts and budgets are denominated in Indian rupees (INR).

## Quality checks

Run the automated Spring MVC/API integration checks with:

```powershell
.\mvnw.cmd test
```

Tests use an isolated in-memory H2 database. The application itself is configured to use PostgreSQL.
