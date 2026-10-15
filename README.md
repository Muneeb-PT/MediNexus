# MediNexus — Smart Hospital Management System

**A Java-based desktop application for streamlined hospital operations, patient management, and role-based healthcare workflows.**

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-Desktop-blue)
![MySQL](https://img.shields.io/badge/Database-MySQL-4479A1?logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)
![Status](https://img.shields.io/badge/Project-Academic%20Development-purple)

## Overview

**MediNexus** is a Smart Hospital Management System developed using Java and Object-Oriented Programming principles. It aims to simplify hospital administration by organizing patient information, appointments, medical records, prescriptions, billing, and medicine inventory in a unified desktop application.

The system follows a modular architecture and uses role-based access control to separate the responsibilities and permissions of administrators, doctors, receptionists, and patients.

MediNexus also includes a rule-based appointment prioritization concept that classifies appointments by severity, such as emergency, chronic, and routine cases. A built-in rule-based assistant helps users navigate common system features.

The project combines object-oriented design, JavaFX-based user interfaces, and MySQL database integration to demonstrate the development of a maintainable, database-driven application.

## Objectives

- Digitize and organize hospital management workflows.
- Reduce manual handling of patient and appointment information.
- Apply encapsulation, inheritance, polymorphism, and abstraction.
- Implement role-based access control for different user categories.
- Maintain patient medical records and prescription information.
- Manage medicine inventory and prescription-related stock checks.
- Introduce severity-based appointment prioritization.
- Improve maintainability through separation of application responsibilities.
- Strengthen practical skills in Java, database connectivity, testing, and software development.

## Key Features

### 1. Role-Based Access Control

MediNexus supports four intended user roles, each with distinct responsibilities and permissions.

| Role | Main responsibilities |
|---|---|
| Administrator | Hospital administration and authorized system management |
| Doctor | Patient medical records, appointments, and prescriptions |
| Receptionist | Patient registration and appointment-related workflows |
| Patient | Access to permitted patient-facing functions |

Authorization checks help prevent users from performing operations outside their assigned permissions. The exact functionality available to each role depends on the implemented modules.

### 2. Patient Management

- Register patient information.
- Maintain patient details and demographic information.
- Retrieve patient records for relevant workflows.
- Validate required fields and user input.

### 3. Appointment Management

- Organize appointment-related workflows.
- Associate appointments with patients and doctors.
- Support doctor-related appointment handling.
- Incorporate a rule-based priority concept for emergency, chronic, and routine cases.

### 4. Electronic Medical Records

- Associate medical records with patients and doctors.
- Record symptoms, diagnosis, and treatment details.
- Support medical-record-based prescription workflows.
- Apply checks for doctor ownership and appointment eligibility where implemented.

### 5. Prescription Management

- Select medicines from the medicine inventory.
- Record dosage, frequency, duration, and instructions.
- Validate prescription quantities against displayed stock.
- Associate prescriptions with medical records, patients, and doctors.
- Check for duplicate medicine prescriptions within a medical record.

### 6. Medicine Inventory

- Retrieve available medicine information.
- Display medicine quantities and prices.
- Indicate low-stock and out-of-stock conditions.
- Check requested quantities before prescription submission.

**Note:** Inventory integrity should also be enforced at the service and database levels, particularly when multiple users may update stock concurrently.

### 7. Billing and Payments

Billing and payment management form part of the system's intended hospital workflow. The availability of specific billing operations depends on the implemented billing module.

### 8. Rule-Based Assistant

The MediNexus assistant responds to predefined keywords and directs users toward relevant system information, including:

- Appointments and scheduling
- Prescriptions and medicines
- Billing and payments
- Medical records
- Emergency-related guidance
- Identity verification
- General system assistance

The current assistant is **rule-based**, not a generative AI model. Its responses depend on programmed rules and keywords.

### 9. Identity Verification Concept

The project includes a DigiLocker-inspired patient identity verification concept intended to support more organized identity-related workflows. Actual integration with DigiLocker or an external identity provider should not be assumed unless separately implemented and configured.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Application logic and object-oriented design |
| JavaFX | Desktop graphical user interface |
| Maven | Dependency management and project build |
| JDBC | Java-to-database connectivity |
| MySQL | Relational data persistence |
| JUnit | Automated testing, where configured |
| IntelliJ IDEA | Development environment |
| Git and GitHub | Version control and collaboration |

## Object-Oriented Programming Concepts

MediNexus demonstrates the four major principles of OOP.

- **Encapsulation:** Organizing data and related operations within classes and controlling access to object state.
- **Inheritance:** Reusing and extending class behavior where appropriate.
- **Polymorphism:** Supporting different implementations or behaviors through shared interfaces and parent types.
- **Abstraction:** Separating essential operations from implementation details.

The modular controller, service, model, and database-access responsibilities also help demonstrate separation of concerns.

## System Architecture

The application follows a layered approach:

```text
┌─────────────────────────────────────┐
│       JavaFX Presentation Layer     │
│     Controllers, Forms, Dashboards  │
└──────────────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────┐
│       Service / Business Layer      │
│ Validation, Workflows, Authorization│
└──────────────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────┐
│      Data Access / JDBC Layer       │
│     Repositories and SQL Queries    │
└──────────────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────┐
│             MySQL Database          │
│       Persistent Application Data   │
└─────────────────────────────────────┘
```

The diagram represents the intended architectural separation; the precise responsibilities of individual classes depend on the current implementation.

## Project Structure

The following is a simplified view of the main source layout. Actual packages and resources may vary as development continues.

```text
MediNexus/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── medinexus/
│   │   │           ├── config/
│   │   │           ├── controller/
│   │   │           ├── model/
│   │   │           ├── repository/
│   │   │           ├── service/
│   │   │           └── ...
│   │   └── resources/
│   │       ├── css/
│   │       └── ...
│   └── test/
│       └── java/
└── .gitignore
```

- `config/` — application and database configuration.
- `controller/` — JavaFX controller classes and UI event handling.
- `model/` — domain objects such as patients, doctors, appointments, and prescriptions.
- `repository/` — database access components, where organized separately.
- `service/` — business rules and application workflows.
- `resources/` — FXML files, CSS stylesheets, and other application resources.

## Prerequisites

Before running MediNexus, install and configure:

1. **JDK 25** or the Java version configured in the project.
2. **IntelliJ IDEA** or another Maven-compatible Java IDE.
3. **MySQL Server** with a configured database.
4. Maven support through IntelliJ or the project's Maven Wrapper.
5. The database schema and any required initialization scripts supplied by the project.

The project uses JavaFX and Maven dependencies declared in `pom.xml`.

## Setup and Installation

### 1. Clone the repository

```bash
git clone https://github.com/Muneeb-PT/MediNexus.git
cd MediNexus
```

Alternatively, download the repository and open its root folder in IntelliJ IDEA.

### 2. Configure the database

Start your MySQL server and prepare the `medinexus` database using the schema or initialization scripts available in the project.

For a local development setup using MySQL on port `3307`, configure the following environment variables:

**Windows PowerShell example:**

```powershell
$env:MEDINEXUS_DB_URL="jdbc:mysql://127.0.0.1:3307/medinexus?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true"
$env:MEDINEXUS_DB_USER="root"
$env:MEDINEXUS_DB_PASSWORD="YOUR_DATABASE_PASSWORD"
```

Replace `YOUR_DATABASE_PASSWORD` with your own database password. Do not commit real passwords to GitHub.

These PowerShell variables apply to commands launched from that same PowerShell session. If running the application through IntelliJ, configure the variables in the relevant Run Configuration or Maven run configuration.

**Security recommendation:** Use a dedicated database user with only the permissions required by the application rather than using the MySQL root account for a production deployment.

### 3. Load the Maven project

In IntelliJ IDEA:

1. Open the project folder containing `pom.xml`.
2. Allow Maven to import and resolve dependencies.
3. Confirm that the configured JDK matches the project requirements.
4. Verify the database connection settings.

### 4. Build and test

Using the Maven Wrapper, if `mvnw.cmd` exists in the project root:

```powershell
.\mvnw.cmd clean test
```

Alternatively, use IntelliJ's Maven tool window and run the `clean` and `test` lifecycle goals.

If Maven is installed and available on your system PATH, you can use:

```bash
mvn clean test
```

### 5. Run the application

Use the JavaFX Maven run configuration declared in the project. For a project configured with the JavaFX Maven plugin, the command is typically:

```powershell
.\mvnw.cmd javafx:run
```

You can also launch the configured Maven goal `javafx:run` from IntelliJ's Maven tool window.

If startup fails, check the Maven output, JavaFX configuration, environment variables, database availability, and database schema.

## Testing

MediNexus uses automated tests to help verify application behavior and business rules.

Areas suitable for testing include:

- Role and permission enforcement
- Patient registration and validation
- Appointment eligibility and ownership
- Medical-record access
- Prescription validation
- Medicine quantity checks
- Database repository behavior
- Error handling for missing or invalid configuration

Run the project's tests with:

```powershell
.\mvnw.cmd test
```

**Testing status:** Run the tests against the latest source code before publishing a test count or claiming a passing build. Results from an earlier version may not reflect subsequent changes.

## Security and Data Integrity

The project incorporates role-based authorization and input validation into relevant workflows. For a real healthcare deployment, additional controls would be necessary, including:

- Secure authentication and password hashing
- Server-side authorization for every sensitive operation
- Transactional inventory updates and database constraints
- Protected database credentials and encrypted connections where appropriate
- Audit logs and controlled access to sensitive patient information
- Backups, recovery procedures, and appropriate data retention policies
- Compliance review for applicable healthcare and personal-data regulations

MediNexus is an academic development project and should not be treated as a certified clinical or production hospital system without appropriate validation and security review.

## Current Scope and Future Enhancements

Potential future improvements include:

- More comprehensive role-specific dashboards
- Enhanced appointment scheduling and doctor availability management
- Transaction-safe inventory and billing operations
- Improved reporting and analytics
- Stronger patient identity verification workflows
- Better automated test coverage and integration testing
- A more advanced assistant using a properly integrated AI service
- Deployment documentation and database migration support
- Improved accessibility and user-interface consistency

These are potential enhancements, not claims that every item is already implemented.

## Learning Outcomes

This project provides practical experience in:

- Java programming and object-oriented design
- JavaFX desktop application development
- Maven dependency and build management
- JDBC and relational database integration
- Role-based authorization
- Validation and exception handling
- Modular software architecture
- Automated testing and collaborative development
- Git and GitHub-based version control

## Academic Context

**Project:** MediNexus — Smart Hospital Management System  
**Programme:** B.Tech Computer Science and Engineering  
**Semester:** S3  
**Institution:** KMEA Engineering College  
**Academic Year:** 2026–2027  

Developed as an academic software project to apply Java, Object-Oriented Programming, database connectivity, and software engineering concepts to a practical hospital management scenario.

## Author

**Mohammed Muneeb PT**

- GitHub: [Muneeb-PT](https://github.com/Muneeb-PT)
- Project Repository: [MediNexus](https://github.com/Muneeb-PT/MediNexus)
- LinkedIn: [Mohammed Muneeb PT](https://www.linkedin.com/in/mohammed-muneeb-pt/)

## License

No license has been specified here. Add a `LICENSE` file and update this section if you decide to distribute the project under an open-source license.

---

**MediNexus — Connecting hospital workflows through structured, maintainable software.**
