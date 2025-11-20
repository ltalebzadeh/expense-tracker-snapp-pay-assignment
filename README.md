# Expense Tracker API

A RESTful backend API for managing personal expenses, built with Spring Boot and PostgreSQL. Track your spending, categorize expenses, and generate monthly reports with automatic spending alerts.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)

---
## Why This Project?

This project was built as an interview assignment to demonstrate:
- Clean, production-ready code structure
- RESTful API design best practices
- Authentication and security implementation
- Database design and ORM usage
- Docker containerization
- Comprehensive testing
- Clear documentation

The expense tracker domain was chosen as it's relatable, requires CRUD operations, has clear business logic (reports, alerts), and allows demonstration of authentication patterns.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture & Design Decisions](#architecture--design-decisions)
  - [Layered Architecture](#layered-architecture)
  - [Key Design Decisions](#key-design-decisions)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Clone the Repository](#clone-the-repository)
  - [Run with Docker](#run-with-docker)
  - [Run Locally](#run-locally)
- [API Documentation](#api-documentation)
  - [Swagger UI](#swagger-ui)
  - [Authentication](#authentication)
  - [API Endpoints](#api-endpoints)
- [Usage Examples](#usage-examples)
  - [Using cURL](#using-curl)
  - [Using Swagger UI](#using-swagger-ui)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Future Enhancements](#future-enhancements)

---

## Features

### ✨ Core Functionality
- User registration and authentication (HTTP Basic Auth)
- Create, read, update, and delete expenses
- Create and manage expense categories
- Categorize expenses (shared categories across users)
- Filter expenses by category
- Generate monthly spending reports
- Automatic spending alerts when category spending exceeds thresholds

### 📊 Monthly Reports Include
- Total spending for the month
- Spending breakdown by category
- Number of expenses
- Alerts for overspending

### 🔒 Security
- Session-based authentication with Spring Security
- Password encryption with BCrypt
- User-specific expense isolation
- Input validation

### 📚 Documentation
- Interactive Swagger UI for API testing
- OpenAPI specification

---

## Tech Stack

**Backend:**
- Java 17
- Spring Boot 3.5.7
- Spring Security (HTTP Basic Auth)
- Spring Data JPA / Hibernate
- PostgreSQL 15
- SpringDoc OpenAPI (Swagger)

**Build & Deployment:**
- Maven
- Docker & Docker Compose

**Testing:**
- JUnit 5
- Mockito
- Spring MockMvc
- H2 (in-memory database for tests)

---

## Architecture & Design Decisions

### Layered Architecture

The application follows a clean **3-layer architecture**:

```
Controller Layer (REST APIs)
↓
Service Layer (Business Logic)
↓
Repository Layer (Data Access)
↓
PostgreSQL Database
```

**Benefits:**
- Clear separation of concerns
- Easy to test (mock dependencies)
- Maintainable and scalable
- Standard Spring Boot practice
- Adequate for assignment scope



### Key Design Decisions

#### 1. Session-Based Authentication
HTTP Basic Auth for simplicity and single-server architecture.

#### 2. Shared Categories
Global categories shared across users to simplify data model and prevent proliferation.

#### 3. REST API Design
POST for creation (server-generated IDs), PUT for full updates.

#### 4. User Isolation
Repository-level enforcement with `findByUserIdAndExpenseId()` - returns 404 for both non-existent and unauthorized access.

#### 5. Validation Strategy
DTO-level with `@Valid`, business logic in service, centralized exception handling.

#### 6. Monthly Report Alerts
Triggers when category spending exceeds 2000 units/month. Configurable in `ExpenseService`.

---

## Getting Started

### Prerequisites

**Option 1: Docker (Recommended)**
- Docker 20.10+
- Docker Compose 1.29+

**Option 2: Local Development**
- Java 17
- Maven 3.8+
- PostgreSQL 15

---

### Clone the Repository

```bash
git clone https://github.com/ltalebzadeh/expense-tracker-snapp-pay-assignment.git
cd expense-tracker-api
```

---

### Run with Docker

**1. Build and start all services:**

```bash
docker-compose up --build -d
```

The application will be available at `http://localhost:8080`

**2. Stop services:**

```bash
docker-compose down
```

**3. Stop and remove all data:**

```bash
docker-compose down -v
```

---

### Run Locally

**1. Start PostgreSQL:**

```bash
docker-compose up -d postgres
```

**2. Build the application:**

```bash
mvn clean package -DskipTests
```

**3. Run the application:**

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/expense-tracker-api-0.0.1-SNAPSHOT.jar
```

**4. Access the application:**

Open `http://localhost:8080/swagger-ui.html`

---

## API Documentation

### Swagger UI

Interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html
```

**Features:**
- Browse all endpoints
- Try API calls directly from the browser
- View request/response schemas
- Authentication support

**OpenAPI JSON specification:**
```
http://localhost:8080/v3/api-docs
```

---

### Authentication

The API uses **HTTP Basic Authentication**. Include credentials in every request (except registration).

**In cURL:**
```bash
curl -u username:password http://localhost:8080/api/expenses
```

**In Swagger UI:**
1. Click the **Authorize** button (green lock icon)
2. Enter username and password
3. Click **Authorize**
4. All subsequent requests will include credentials

---

### API Endpoints

#### Authentication
| Method | Endpoint             | Description          | Auth Required |
|--------|---------------------|----------------------|---------------|
| POST   | `/api/auth/register` | Register new user    | No            |

#### Expenses
| Method | Endpoint                        | Description              | Auth Required |
|--------|---------------------------------|--------------------------|---------------|
| POST   | `/api/expenses`                 | Create expense           | Yes           |
| GET    | `/api/expenses`                 | Get all expenses         | Yes           |
| GET    | `/api/expenses/category/{name}` | Filter by category       | Yes           |
| PUT    | `/api/expenses/{id}`            | Update expense           | Yes           |
| DELETE | `/api/expenses/{id}`            | Delete expense           | Yes           |
| GET    | `/api/expenses/report`          | Get monthly report       | Yes           |

#### Categories
| Method | Endpoint           | Description       | Auth Required |
|--------|--------------------|-------------------|---------------|
| POST   | `/api/categories`  | Create category   | Yes           |

---

## Usage Examples

### Using cURL

#### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "broke_developer",
    "password": "123456"
  }'
```

**Response:**
```json
{
  "message": "User registered successfully",
  "username": "broke_developer"
}
```

---

#### 2. Create a Category

```bash
curl -X POST http://localhost:8080/api/categories \
  -u broke_developer:123456 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Food"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Food"
}
```

---

#### 3. Create an Expense

```bash
curl -X POST http://localhost:8080/api/expenses \
  -u broke_developer:123456 \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 420.5,
    "description": "Pizza at 3 AM",
    "categoryName": "Food",
    "date": "2025-11-18"
  }'
```

**Response:**
```json
{
  "id": 1,
  "amount": 420.5,
  "description": "Pizza at 3 AM",
  "categoryName": "Food",
  "date": "2025-11-18"
}
```

---

#### 4. Get All Expenses

```bash
curl -X GET http://localhost:8080/api/expenses \
  -u broke_developer:123456
```

**Response:**
```json
[
  {
    "id": 1,
    "amount": 420.5,
    "description": "Pizza at 3 AM",
    "categoryName": "Food",
    "date": "2025-11-18"
  },
  {
    "id": 2,
    "amount": 50.0,
    "description": "Metro",
    "categoryName": "Transport",
    "date": "2025-11-19"
  }
]
```

---

#### 5. Filter Expenses by Category

```bash
curl -X GET http://localhost:8080/api/expenses/category/Food \
  -u broke_developer:123456
```

---

#### 6. Update an Expense

```bash
curl -X PUT http://localhost:8080/api/expenses/1 \
  -u broke_developer:123456 \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.0,
    "description": "Emergency pizza + coca",
    "categoryName": "Food",
    "date": "2025-11-18"
  }'
```

---

#### 7. Delete an Expense

```bash
curl -X DELETE http://localhost:8080/api/expenses/1 \
  -u broke_developer:123456
```

---

#### 8. Generate Monthly Report

```bash
curl -X GET "http://localhost:8080/api/expenses/report?year=2025&month=11" \
  -u broke_developer:123456
```

**Response:**
```json
{
  "year": 2025,
  "month": 11,
  "totalAmount": 2350.00,
  "expenseCount": 5,
  "spendingByCategory": {
    "Food": 2100.00,
    "Transport": 250.00
  },
  "alerts": [
    "Warning: You spent 2100.00 on Food this month!"
  ]
}
```

---

### Using Swagger UI

**1. Open Swagger UI:**

Navigate to `http://localhost:8080/swagger-ui.html`

**2. Register a User:**

- Expand **POST /api/auth/register**
- Click **Try it out**
- Edit the request body:
  ```json
  {
    "username": "broke_developer",
    "password": "123456"
  }

**2. Authorize:**

- Click the **Authorize** button (green lock icon)
- Enter credentials:
    - **Username:** `broke_developer`
    - **Password:** `123456`
- Click **Authorize**, then **Close**

**3. Try an Endpoint:**

- Expand **POST /api/expenses**
- Click **Try it out**
- Edit the request body:
  ```json
  {
    "amount": 100.0,
    "description": "Coffee",
    "categoryName": "Food",
    "date": "2025-11-20"
  }
  ```
- Click **Execute**
- View the response

---

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=ExpenseServiceTest
```

### Integration Tests (Optional)

Integration tests verify the complete system with real PostgreSQL using Testcontainers.

**Disabled by default** (requires Docker running).

**To enable:**
1. Ensure Docker is running
2. Remove `@Disabled` from `ExpenseTrackerIntegrationTest.java`
3. Run: `mvn test -Dtest=ExpenseTrackerIntegrationTest`

**What it tests:**
- Complete user journey: Register → Create expenses → Generate report with alert
- Real HTTP requests via TestRestTemplate
- Real PostgreSQL in Docker container
- True integration between Spring Boot and PostgreSQL

### Test Coverage

The project includes:
- **Unit tests** for services (business logic)
- **Unit tests** for controllers (API endpoints)
- **Integration test** (optional, requires Docker)
- **Exception handling tests**
- **Validation tests**

**Test Structure:**
```
src/test/java/
├── controller/
│   ├── AuthControllerTest
│   ├── CategoryControllerTest
│   └── ExpenseControllerTest
├── service/
│   ├── CategoryServiceTest
│   └── ExpenseServiceTest
└── integration/
    └── ExpenseTrackerIntegrationTest
```

---

## Project Structure

```
expense-tracker-api/
├── src/
│   ├── main/
│   │   ├── java/com/expensetracker/api/
│   │   │   ├── config/           # Security, OpenAPI configuration
│   │   │   ├── controller/       # REST API endpoints
│   │   │   │   └── exception/    # Custom exceptions & handlers
│   │   │   ├── dto/              # Request/Response objects
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── repository/       # Data access layer
│   │   │   └── service/          # Business logic
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/expensetracker/api/
│           ├── controller/       # Controller tests
│           └── service/          # Service tests
├── docker-compose.yml            # Docker orchestration
├── Dockerfile                    # Application container
├── pom.xml                       # Maven dependencies
└── README.md
```

---

## Future Enhancements

**Potential improvements for production:**

**Security:**
- Role-based access control (admin vs regular users)
- JWT authentication for stateless scaling
- Rate limiting on registration endpoint
- Password strength requirements

**Features:**
- Budget limits per category
- Recurring expenses
- Export reports to PDF/CSV
- Custom alert thresholds per user
- Multi-currency support

**Architecture:**
- Caching layer (Redis)
- Event-driven architecture for notifications
- Elasticsearch for advanced search

**DevOps:**
- CI/CD pipeline (GitHub Actions)
- Kubernetes deployment
- Monitoring with Prometheus/Grafana
- Centralized logging (ELK stack)

---

## Acknowledgments

Built as an interview assignment to demonstrate backend development skills with Spring Boot, RESTful API design, and containerization.

**Author:** Leila Talebzadeh  
**Contact:** l.talebzadeh93@gmail.com  
**LinkedIn:** https://www.linkedin.com/in/leila-talebzadeh/

---

**Happy Expense Tracking! 💸**