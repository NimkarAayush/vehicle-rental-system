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

## Deploy to Railway

This application is ready to be deployed to Railway using the included Dockerfile.

### 1. Setup Service and MySQL
1. In your Railway project, click **New** -> **Database** -> **Add MySQL**.
2. Click **New** -> **GitHub Repo** and select your repository.
3. The application will detect the Dockerfile and build automatically.

### 2. Environment Variables
In your web application service on Railway, set the following environment variables:
* `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`: Reference these directly from your MySQL plugin.
* `PORT`: `8080` (Railway injects this automatically).
* `UPLOAD_DIR`: Path to the mounted volume, e.g., `/app/uploads`.
* `ADMIN_EMAIL`: The email for the first system admin (e.g., `admin@yourdomain.com`).
* `ADMIN_PASSWORD`: A secure password for the first admin (must be at least 12 characters).
* `APP_SEED_DEMO_VEHICLES`: Set to `true` to insert demo vehicles on first boot (optional).

### 3. Persistent Volume for Uploads
Vehicle images require persistent storage to survive redeploys.
1. In your web service settings, go to **Volumes**.
2. Create a new volume and mount it to `/app/uploads` (or match your `UPLOAD_DIR` variable).
3. **Important Permission Issue:** Because the Docker container runs as a non-root user (`spring`), it may not have write permissions to a volume created by root. Railway allows you to override the UID for volume permissions. You must add the environment variable `RAILWAY_RUN_UID=0` (or `RAILWAY_RUN_UID` matching the `spring` user's UID) if you encounter permission denied errors on startup. The app will fail to start and log a clear error message if the directory is not writable.

### 4. Local Verification (Prod Profile)
To verify the production configuration locally:
```bash
# Pass the required database and admin variables
$env:MYSQLHOST="localhost"; $env:MYSQLPORT="3306"; $env:MYSQLDATABASE="vehicle_rental"
$env:MYSQLUSER="root"; $env:MYSQLPASSWORD="your_password"
$env:ADMIN_EMAIL="admin@test.com"; $env:ADMIN_PASSWORD="securepassword123"
$env:UPLOAD_DIR="./uploads"

.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```
*Note: The `prod` profile sets session cookies to `secure: true`. If you are testing over `http://localhost`, your browser may reject the session cookie unless you test over HTTPS or use a browser/flag that explicitly permits secure cookies on localhost.*

## Security Review Open Recommendations

While the application applies several secure defaults, the following are recommended before a fully public release:
* **Rate Limiting:** Implement rate limiting (e.g., via Bucket4j) on the `/login` and `/register` endpoints to mitigate brute-force attacks.
* **Content Security Policy (CSP):** Add a strict CSP header in `SecurityConfig.java` to prevent Cross-Site Scripting (XSS).
* **Two-Factor Authentication (2FA):** Add 2FA for Admin accounts.
* **Password Expiry / Lockout:** Implement account lockout after consecutive failed login attempts.
