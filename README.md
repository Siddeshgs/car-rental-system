# Car Rental Management System

A full-stack enterprise **Car Rental Management System** developed with **Java, Spring Boot 3, Spring Data JPA, and MySQL/H2 Database**.

---

## 🚀 Key Features

- **Fleet Management**: Real-time vehicle inventory tracking (Sedans, SUVs, Luxury, EVs, Sports) with category, fuel, transmission, rate, and status filters.
- **Booking Lifecycle Workflow**:
  - Reservation creation with instant date availability & conflict prevention algorithms.
  - Multi-tier dynamic pricing calculation (daily rate &times; rental days + optional insurance + promo codes).
  - Workflow states: `PENDING` &rarr; `CONFIRMED` &rarr; `ACTIVE` (car hand-over) &rarr; `COMPLETED` (car return & late fee penalty calculation) / `CANCELLED`.
- **Customer Profiles**: Driver's license validation, contact information, and rental history tracking.
- **Billing & Payments**: Integrated payment records, transaction tracking, and printable invoice generation.
- **Operations Dashboard**: Fleet utilization rates, active rentals counter, and business revenue metrics.
- **Dual Database Flexibility**:
  - **H2 In-Memory Database** (enabled by default for zero-setup execution).
  - **MySQL Database** (ready for production with `schema.sql` and `data.sql`).
- **RESTful APIs & Swagger**: Clean REST architecture with Swagger UI (`/swagger-ui.html`) and ready-to-import Postman collection.
- **Automated Test Suite**: JUnit 5 + Mockito tests covering core service-layer business logic and mock MVC controller tests.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.3 |
| **ORM / Data Access** | Spring Data JPA / Hibernate |
| **Databases** | MySQL 8.x / H2 In-Memory DB |
| **API Documentation** | Springdoc OpenAPI 3 / Swagger UI |
| **Testing** | JUnit 5, Mockito, AssertJ, MockMvc |
| **Build Tool** | Apache Maven |
| **Frontend** | HTML5, Modern CSS Glassmorphism, Vanilla JS |

---

## 📂 Project Architecture

```
d:/car rental system/
├── pom.xml
├── schema.sql                                # MySQL Table Definitions
├── data.sql                                  # MySQL Seed Records
├── Car_Rental_System_Postman_Collection.json # Postman Import Collection
├── src/
│   ├── main/
│   │   ├── java/com/carrental/
│   │   │   ├── CarRentalApplication.java     # Application Main Entry
│   │   │   ├── config/                       # OpenAPI, CORS, and DataInitializer
│   │   │   ├── controller/                   # REST Endpoints (Cars, Customers, Bookings, Dashboard)
│   │   │   ├── dto/                          # Request / Response DTOs
│   │   │   ├── entity/                       # JPA Entities (Car, Customer, Booking, Payment)
│   │   │   ├── enums/                        # Domain Enums (Status, Category, Fuel, Transmission)
│   │   │   ├── exception/                    # Global Exception Handler & Custom Errors
│   │   │   ├── repository/                   # Spring Data JPA Repositories
│   │   │   └── service/                      # Business Logic Interfaces & Implementations
│   │   └── resources/
│   │       ├── application.properties        # Main Spring Config (Active Profile Selector)
│   │       ├── application-h2.properties     # In-Memory DB Config
│   │       ├── application-mysql.properties  # MySQL Production DB Config
│   │       └── static/                       # Frontend Web UI (HTML, CSS, JS, Invoices)
│   └── test/
│       └── java/com/carrental/
│           ├── service/                      # JUnit 5 & Mockito Service Layer Tests
│           └── controller/                   # MockMvc Controller Integration Tests
```

---

## ⚡ Quick Start

### 1. Prerequisites
- **Java 17 or higher**
- **Maven 3.8+**
- *(Optional)* MySQL Server 8.x (if using MySQL mode)

### 2. Run the Application (Default H2 In-Memory Mode)
Run the following command in the project root directory:

```bash
mvn spring-boot:run
```

Once started, access:
- **Interactive Web App**: [http://localhost:8080](http://localhost:8080)
- **Swagger UI Documentation**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) *(JDBC URL: `jdbc:h2:mem:carrentaldb`, User: `sa`, Password: empty)*

---

### 3. Running with MySQL Database

1. Create the MySQL database and seed initial data:
   ```sql
   mysql -u root -p < schema.sql
   mysql -u root -p < data.sql
   ```
2. Update your database credentials in `src/main/resources/application-mysql.properties`.
3. Start the application with the `mysql` profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

---

## 🧪 Running Automated Tests

Run the test suite with Maven:

```bash
mvn clean test
```

### Test Coverage Highlights:
- **`BookingServiceTest`**:
  - Availability verification & date conflict rejection.
  - Duration calculation, optional insurance, and promo code discounts.
  - Car status transition upon pickup (`ACTIVE` / `RENTED`).
  - Return check-in with automatic late-fee penalty calculation.
  - Cancellation workflow and resource release.
- **`CarServiceTest`**:
  - Duplicate registration number validation.
  - Status updates (`AVAILABLE`, `RENTED`, `MAINTENANCE`).
  - Search & multi-parameter filter queries.
- **`CustomerServiceTest`**:
  - Driver's license number uniqueness checks.
  - Customer profile creation and updates.
- **`BookingControllerTest`**:
  - MockMvc HTTP endpoint status code & JSON response verification.

---

## 📡 REST API Summary

### Vehicle Management (`/api/cars`)
- `GET /api/cars` — List all cars (supports filters: `category`, `status`, `fuelType`, `transmission`, `maxRate`, `search`)
- `GET /api/cars/available` — List only available vehicles
- `GET /api/cars/{id}` — Fetch vehicle details by ID
- `POST /api/cars` — Add a new vehicle to fleet
- `PUT /api/cars/{id}` — Update vehicle specifications
- `PATCH /api/cars/{id}/status?status=MAINTENANCE` — Update vehicle status
- `DELETE /api/cars/{id}` — Remove vehicle from fleet

### Customer Management (`/api/customers`)
- `GET /api/customers` — List registered customers
- `GET /api/customers/{id}` — Fetch customer profile
- `POST /api/customers` — Register new customer

### Booking Operations (`/api/bookings`)
- `GET /api/bookings` — List all bookings (supports `?status=` or `?customerId=`)
- `GET /api/bookings/{id}` — Fetch booking details
- `GET /api/bookings/reference/{ref}` — Lookup booking by reference code (e.g., `BK-3019AB24`)
- `GET /api/bookings/check-availability?carId=1&startDate=...&endDate=...` — Verify date availability
- `POST /api/bookings` — Create a new car reservation
- `PATCH /api/bookings/{id}/start` — Hand over car & start rental (`ACTIVE`)
- `POST /api/bookings/{id}/return` — Return car, verify mileage/damage, calculate late fee (`COMPLETED`)
- `PATCH /api/bookings/{id}/cancel` — Cancel reservation

### Dashboard & Analytics (`/api/dashboard`)
- `GET /api/dashboard/stats` — Fleet size, available cars, active rentals, total revenue, utilization rate %

---

## 📮 Postman Collection

Import `Car_Rental_System_Postman_Collection.json` directly into Postman to test all endpoints with pre-configured requests and sample request payloads.
