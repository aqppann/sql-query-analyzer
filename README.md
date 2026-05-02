# SQL Query Performance Analyzer API

A backend developer tool for analyzing SQL query performance.
Built with Java 17, Spring Boot 3.5, Spring Data JPA, PostgreSQL.

## Features

- Store and manage SQL queries
- Automatic performance classification: `NORMAL` / `SLOW` / `CRITICAL`
- Smart recommendations based on query analysis:
    - `SELECT *` warning
    - Missing index detection
    - `ORDER BY` performance warning
    - Missing `WHERE` / `LIMIT` warning
- Filtering by performance status
- Pagination support
- Top 10 slowest queries endpoint
- Statistics endpoint
- Global exception handling
- DTO validation
- Swagger/OpenAPI documentation

## Tech Stack

- Java 17
- Spring Boot 3.5.14
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Lombok
- Swagger / SpringDoc OpenAPI 2.8.8

## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL (running locally)

### Database Setup

```sql
CREATE DATABASE sql_analyzer;
```

### Configuration

Create `src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sql_analyzer
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
```

### Run

Set active profile to `local` in IntelliJ Run Configurations, then run `SqlAnalyzerApplication`.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/queries` | Create and analyze a query |
| GET | `/api/v1/queries` | Get all queries (pagination + filter) |
| GET | `/api/v1/queries/{id}` | Get query by ID |
| PUT | `/api/v1/queries/{id}` | Update query |
| DELETE | `/api/v1/queries/{id}` | Delete query |
| GET | `/api/v1/queries/top-slow` | Top 10 slowest queries |
| GET | `/api/v1/queries/statistics` | Performance statistics |

## Swagger UI