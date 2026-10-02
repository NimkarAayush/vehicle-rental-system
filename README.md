# Vehicle Rental Management System

A professional portfolio-quality web application for managing vehicle rentals, built with Java and Spring Boot.

## Tech Stack
- **Backend:** Java 21, Spring Boot 3.2.4
- **Modules:** Spring Web, Spring Data JPA, Spring Security, Validation
- **Template Engine:** Thymeleaf
- **Database:** H2 Database (In-Memory)
- **Frontend:** Bootstrap 5, FontAwesome

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven (installed globally or use Maven wrapper if added later)

### How to Run
1. Open a terminal in the project root directory.
2. Run the application using Maven:
   ```bash
   mvn spring-boot:run
   ```
3. Open your browser and navigate to:
   [http://localhost:8080](http://localhost:8080)
   
### Accessing the Database
The H2 Database console is enabled for development purposes.
- **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL:** `jdbc:h2:mem:vehicledb`
- **Username:** `sa`
- **Password:** *(leave blank)*


## Database Configuration (MySQL)

This project has been migrated to use **MySQL** as its persistent database, managed via **Flyway** migrations.

### Setup Instructions
1. Install MySQL (v8.0+ recommended) locally or use Docker:
   ```bash
   docker run --name vehicle-rental-mysql -e MYSQL_ROOT_PASSWORD=your_secure_password -p 3306:3306 -d mysql:8
   ```
2. Log into MySQL and create the database:
   ```sql
   CREATE DATABASE vehicle_rental;
   ```
3. Set your environment variables (or create an `application-local.properties` file based on `application-example.properties`):
   * `DB_URL` (default: `jdbc:mysql://localhost:3306/vehicle_rental?serverTimezone=Asia/Kolkata`)
   * `DB_USERNAME` (default: `root`)
   * `DB_PASSWORD` (Required! No default)
   * `SEED_ADMIN_PASSWORD` (default: `admin123`)
   * `SEED_CUSTOMER_PASSWORD` (default: `password123`)

### Running the Application

The default Spring profile is `dev`, which automatically connects to MySQL and applies Flyway migrations:
```powershell
.\mvnw.cmd spring-boot:run
```

**First-Time Boot:**
On the first run with an empty database, Flyway will create the schema (`V1__Initial_Schema.sql`). The `DatabaseSeeder` will then inject demo credentials if the `users` table is empty.

### Running Tests

The test suite runs using an in-memory **H2** database configured in MySQL compatibility mode. This ensures tests are isolated and extremely fast without requiring a live MySQL instance.
```powershell
.\mvnw.cmd test
```
Flyway automatically runs against H2 during testing to ensure the schema (`V1`) matches your JPA entities exactly (`ddl-auto=validate`).
