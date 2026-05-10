# SQL Query Performance Analyzer API

![CI](https://github.com/aqppann/sql-query-analyzer/actions/workflows/ci.yml/badge.svg)

A backend tool for analyzing SQL query performance.
Automatically classifies queries as **NORMAL**, **SLOW**, or **CRITICAL**
and provides optimization recommendations.

---

## Features

- Store and manage SQL queries via REST API
- Automatic performance classification (NORMAL / SLOW / CRITICAL)
- Smart query analysis with optimization recommendations:
  - `SELECT *` usage warning
  - Missing index detection on `email` column
  - `ORDER BY` performance warning
  - Missing `WHERE` / `LIMIT` clause warning
  - `LIKE` with wildcard warning
  - `NOT IN` performance warning
  - `OR` in WHERE clause warning
  - `DISTINCT` usage warning
  - Subquery in WHERE clause warning
  - `JOIN` without ON condition warning
  - `HAVING` without GROUP BY warning
- Pagination and filtering by performance status
- Search by SQL text
- Filtering by date range
- Top 10 slowest queries endpoint
- Performance statistics endpoint
- Global exception handling
- DTO validation
- Swagger / OpenAPI documentation
- Docker + docker-compose support
- CI/CD via GitHub Actions

---

## Tech Stack

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Lombok
- SpringDoc OpenAPI
- JUnit 5
- Mockito
- Docker
- GitHub Actions

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.x
- PostgreSQL (for local run) or Docker (for containerized run)

---

### Run Locally

#### Database Setup

Open pgAdmin or any PostgreSQL client and run:

```sql
CREATE DATABASE sql_analyzer;
```

#### Configuration

Create file `src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sql_analyzer
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
```

> This file is excluded from Git via `.gitignore` to protect credentials.

#### Run

1. Open **Run Configurations** in IntelliJ IDEA
2. Set **Active profiles**: `local`
3. Run `SqlAnalyzerApplication`

App will start on `http://localhost:8082`

---

### Run with Docker

#### Prerequisites

- Docker Desktop installed and running

#### Start

```bash
mvn clean package -DskipTests
docker-compose up --build
```

App will start on `http://localhost:8082`

#### Stop

```bash
docker-compose down
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/queries` | Create and analyze a query |
| `GET` | `/api/v1/queries` | Get all queries (pagination + filter) |
| `GET` | `/api/v1/queries/{id}` | Get query by ID |
| `PUT` | `/api/v1/queries/{id}` | Update query |
| `DELETE` | `/api/v1/queries/{id}` | Delete query |
| `GET` | `/api/v1/queries/top-slow` | Top 10 slowest queries |
| `GET` | `/api/v1/queries/statistics` | Performance statistics |

### Query Parameters for GET /api/v1/queries

| Parameter | Type | Description |
|-----------|------|-------------|
| `status` | String | Filter by NORMAL / SLOW / CRITICAL |
| `sqlText` | String | Search by SQL text (case-insensitive) |
| `from` | LocalDateTime | Filter from date (e.g. 2026-05-01T00:00:00) |
| `to` | LocalDateTime | Filter to date (e.g. 2026-05-31T23:59:59) |
| `page` | int | Page number (default: 0) |
| `size` | int | Page size (default: 10) |

### Example Request

```json
POST /api/v1/queries
{
  "sqlText": "SELECT * FROM users WHERE email = 'test@test.com' ORDER BY created_at",
  "executionTimeMs": 1500,
  "databaseName": "production",
  "notes": "slow query detected"
}
```

### Example Response

```json
{
  "id": 1,
  "sqlText": "SELECT * FROM users WHERE email = 'test@test.com' ORDER BY created_at",
  "executionTimeMs": 1500,
  "status": "SLOW",
  "databaseName": "production",
  "notes": "slow query detected",
  "createdAt": "2026-05-01T15:43:59.117933",
  "recommendations": [
    "Avoid SELECT * — specify only required columns to reduce data transfer",
    "Possible missing index on email column — consider adding an index",
    "ORDER BY on large datasets can be slow — consider adding an index on sorted column"
  ]
}
```

---

## Performance Thresholds

| Status | Execution Time | Description |
|--------|---------------|-------------|
| `NORMAL` | < 1000ms | Query performs well |
| `SLOW` | 1000ms - 4999ms | Query needs attention |
| `CRITICAL` | >= 5000ms | Query requires immediate fix |

---

## Query Analysis Rules

| Rule | Condition | Recommendation |
|------|-----------|----------------|
| SELECT * | Query contains `SELECT *` | Specify only required columns |
| Missing index | `WHERE email =` + SLOW/CRITICAL | Add index on email column |
| ORDER BY | `ORDER BY` + SLOW/CRITICAL | Add index on sorted column |
| No filter | No `WHERE` and no `LIMIT` | Query may return too many rows |
| LIKE wildcard | `LIKE '%...'` | Consider full-text search |
| NOT IN | Query contains `NOT IN` | Use NOT EXISTS instead |
| OR in WHERE | `WHERE ... OR ...` | Consider splitting into UNION |
| DISTINCT | Query contains `DISTINCT` | Check JOIN logic for duplicates |
| Subquery | `WHERE ... (SELECT ...)` | Use JOIN instead |
| JOIN without ON | `JOIN` without `ON` or `USING` | Verify join conditions |
| HAVING without GROUP BY | `HAVING` without `GROUP BY` | Use WHERE instead |

---

## Project Structure

```
src/main/java/com/aqppann/sqlanalyzer/
├── analyzer/
│   └── QueryAnalyzer.java          # SQL analysis logic
├── config/
│   └── OpenApiConfig.java          # Swagger configuration
├── controller/
│   └── QueryRecordController.java  # REST endpoints
├── dto/
│   ├── QueryRecordRequestDto.java  # Input DTO with validation
│   ├── QueryRecordResponseDto.java # Output DTO
│   └── QueryStatisticsDto.java     # Statistics DTO
├── entity/
│   ├── QueryRecord.java            # JPA entity
│   └── PerformanceStatus.java      # Enum: NORMAL, SLOW, CRITICAL
├── exception/
│   ├── GlobalExceptionHandler.java # Global error handling
│   └── ErrorResponse.java          # Error response structure
├── repository/
│   └── QueryRecordRepository.java  # Spring Data JPA repository
├── service/
│   └── QueryRecordService.java     # Business logic
└── SqlAnalyzerApplication.java     # Entry point
```

---

## Running Tests

### In IntelliJ IDEA

Right-click on `src/test/java` → **Run 'Tests in sql-analyzer'**

### Via Maven

```bash
mvn test "-Dspring.profiles.active=local"
```

### Test Coverage

| Class | Type | Tests |
|-------|------|-------|
| `QueryAnalyzer` | Unit | 14 tests |
| `QueryRecordService` | Unit | 5 tests |
| `QueryRecordController` | Integration | 6 tests |
| `SqlAnalyzerApplication` | Context | 1 test |

---

## CI/CD

Every push to `main` branch automatically:

1. Starts a PostgreSQL container
2. Builds the project with Maven
3. Runs all tests

Pipeline is configured via GitHub Actions in `.github/workflows/ci.yml`

---

## Swagger UI

```
http://localhost:8082/swagger-ui/index.html
```

---

## Summary

This project covers the full cycle of building a REST API — from database design and business logic to validation, error handling, testing, containerization, and CI/CD automation.
It demonstrates practical usage of Spring Boot, JPA, and Hibernate in a real-world scenario where performance monitoring and query analysis are essential parts of backend development.