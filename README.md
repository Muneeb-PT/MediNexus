# MediNexus
### Smart Hospital Management System

<p align="center">
  <strong>A JavaFX desktop application for managing connected hospital workflows.</strong>
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-Application%20Logic-orange">
  <img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-Desktop%20UI-2E75B6">
  <img alt="FXML" src="https://img.shields.io/badge/FXML-UI%20Layouts-6A5ACD">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-Database-4479A1">
  <img alt="Project" src="https://img.shields.io/badge/Type-OOP%20Mini%20Project-218B82">
</p>

<p align="center">
  <a href="https://github.com/Muneeb-PT/MediNexus">Source Code</a> ·
  <a href="https://github.com/Muneeb-PT/MediNexus/releases">Windows Releases</a> ·
  <a href="https://muneeb-pt.github.io/MediNexus/">Project Website</a>
</p>

---

## About the Project

**MediNexus** is a **Smart Hospital Management System** developed as a group mini-project for the Object-Oriented Programming (OOP) course during S3 of B.Tech Computer Science and Engineering at KMEA Engineering College.

The idea was to build more than a basic record-keeping application: a desktop system that connects common hospital workflows such as patient management, appointments, medical records, prescriptions, medicine inventory, billing, and role-based access.

The application is built using **Java, JavaFX, FXML, JDBC, and MySQL**. It gave me an opportunity to apply OOP concepts to a larger application, work with a relational database, design a multi-screen interface, and understand how separate modules fit together.

This is an academic project intended for learning and demonstration, not a certified clinical system.

## Features and Modules

The repository contains a broad set of application modules, including:

- **Role-based access:** Separate workflows for Administrator, Doctor, Receptionist, and Patient roles.
- **Patient management:** Patient registration, profile information, and patient records.
- **Appointment management:** Appointment creation and management, with rule-based priority handling for emergency, chronic, and routine cases.
- **Medical records:** Store and access patient medical information through role-appropriate workflows.
- **Prescriptions:** Create and manage prescriptions with medicine and dosage information.
- **Pharmacy and inventory:** Medicine records, stock management, restocking, inventory alerts, and prescription-related dispensing/billing workflows.
- **Billing and payments:** Hospital billing, payment records, and receipt/report-related functionality.
- **Emergency workflows:** Emergency request handling and related vehicle information.
- **Notifications:** In-app notifications and observer-based notification components.
- **Identity verification:** A verification workflow with a DigiLocker integration structure; availability depends on configuration and provider setup.
- **Rule-based assistant:** An in-app helper for navigating common system functions. This is a programmed rule-based assistant, not a generative AI chatbot.
- **Support and feedback:** Support ticket and feedback workflows.

The exact behaviour of each feature depends on the current implementation and local configuration. Some integrations use demo providers and should not be mistaken for live third-party services.

## User Roles

| Role | Main area |
|---|---|
| **Administrator** | Authorized system administration and management |
| **Doctor** | Patient care workflows, medical records, and prescriptions |
| **Receptionist** | Patient registration and appointment handling |
| **Patient** | Access to permitted patient-facing functions |

## Object-Oriented Programming

MediNexus was developed as an opportunity to use OOP principles in a practical application:

- **Encapsulation** — keeping data and related operations together while controlling access to object state.
- **Inheritance** — reusing and extending behaviour through class relationships where appropriate.
- **Polymorphism** — using shared interfaces and different implementations for related operations.
- **Abstraction** — exposing essential operations while keeping implementation details separate.

The codebase also uses design patterns and separation of responsibilities in areas such as appointment prioritization, notifications, payment handling, and data access.

## Technology Stack

| Technology | Purpose |
|---|---|
| **Java** | Application logic and OOP implementation |
| **JavaFX** | Desktop user interface |
| **FXML** | JavaFX screen layouts |
| **CSS** | Interface styling |
| **JDBC** | Java-to-database communication |
| **MySQL** | Relational database |
| **Maven** | Dependency and build management |
| **JUnit 5** | Automated tests |

## Application Architecture

At a high level, the application follows this flow:

```text
User
 |
 v
JavaFX Interface
(FXML layouts + Controllers)
 |
 v
Service Layer
(Validation, permissions, workflows)
 |
 v
Repository / JDBC Layer
 |
 v
MySQL Database
```

The project is organized into packages for configuration, controllers, models, navigation, observers, payments, repositories, services, strategies, utilities, and views.

## Screenshots and Demo

The project website provides a quick overview of MediNexus. For the actual desktop application, check the available screenshots and Windows release files in the repository.

- [Open the project website](https://muneeb-pt.github.io/MediNexus/)
- [Browse source code](https://github.com/Muneeb-PT/MediNexus)
- [View Windows releases](https://github.com/Muneeb-PT/MediNexus/releases)

## Getting Started

### Requirements

- A JDK compatible with the version configured in `pom.xml` (currently Java 25).
- MySQL Server.
- IntelliJ IDEA or another Java IDE with Maven support.
- Git, if you want to clone the repository.

### 1. Clone the repository

```bash
git clone https://github.com/Muneeb-PT/MediNexus.git
cd MediNexus
```

### 2. Open the project

Open the folder in your Java IDE and allow Maven to download the configured dependencies.

### 3. Configure MySQL

1. Start MySQL Server.
2. Create the `medinexus` database and configure the tables required by the application's JDBC repositories.
3. The default connection URL targets `127.0.0.1:3307/medinexus`. Set `MEDINEXUS_DB_URL` if your local MySQL configuration is different.
4. Provide the database username and password through environment variables or supported JVM properties. Do not commit credentials.

**Windows PowerShell example:**

```powershell
$env:MEDINEXUS_DB_PASSWORD = "your-local-mysql-password"
$env:MEDINEXUS_DB_USER = "your-mysql-username"
```

The application also supports the JVM properties `-Dmedinexus.db.password`, `-Dmedinexus.db.user`, and `-Dmedinexus.db.url`.

> **Database setup note:** A complete fresh-install SQL schema and seed-data script is not currently provided. You will need to prepare the database schema expected by the application before running it on a new machine.

### 4. Run the application

Run the JavaFX Maven configuration from your IDE, or use:

```bash
mvn javafx:run
```

If the application does not start, check the JDK version, Maven dependencies, MySQL service, database URL, credentials, and required tables.

## Project Roadmap

MediNexus is a project I would like to continue developing beyond the initial OOP mini-project. Possible next improvements include:

- **Patient photo support:** Add profile photographs and a clear, secure image-storage workflow.
- **Mobile application:** Develop a companion mobile app for selected patient-facing features such as appointments, notifications, and prescription access.
- **Pharmacy improvements:** Extend the existing medicine inventory and pharmacy billing modules with more complete workflows and usability improvements.
- **Deployment and setup:** Make database initialization and installation easier on a fresh computer.
- **Further testing:** Expand automated tests and validate workflows across all four user roles.

These are planned improvements, not claims that the features are already complete.

## Academic Details and Contribution

| Detail | Information |
|---|---|
| **Project** | MediNexus — Smart Hospital Management System |
| **Course** | Object-Oriented Programming (OOP) |
| **Project type** | Group mini-project |
| **Programme** | B.Tech Computer Science and Engineering |
| **Semester** | S3 |
| **Institution** | KMEA Engineering College |
| **Academic year** | 2026–2027 |
| **Submitted to** | Sajna Saleem |

MediNexus was submitted as a **group project**. I worked with the group, but I was responsible for the majority of the implementation and integration work—approximately 90% of the development effort by my own estimate. This included building and connecting the main application modules, working on the database layer, and preparing the project for demonstration.

## Project Developer

**Mohammed Muneeb PT**

- [GitHub Profile](https://github.com/Muneeb-PT)
- [LinkedIn](https://www.linkedin.com/in/mohammed-muneeb-pt/)
- [MediNexus Repository](https://github.com/Muneeb-PT/MediNexus)

## Security and Responsible Use

MediNexus is an academic project and is **not intended for real clinical or production hospital use**. A production healthcare system would require extensive independent security, privacy, reliability, usability, and regulatory review.

Use only test or appropriately authorized data. Never commit real patient information, passwords, API credentials, or database secrets. Any credential that has previously been committed to a public repository should be considered exposed and rotated; removing it from the latest version does not erase it from Git history.

## License

No license has currently been specified. Unless a license is added, do not assume the source code is available for unrestricted reuse or redistribution.

---

<p align="center">
  <strong>MediNexus</strong><br>
  Built as an OOP mini-project, with room to grow.
</p>
