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
