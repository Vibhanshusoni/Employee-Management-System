# Employee Management System

A secure backend application built using **Spring Boot** and **Spring Security** that provides authentication, employee management, role-based access control, and advanced security mechanisms.

The system implements **JWT authentication, refresh token rotation, OAuth2 login (Google & GitHub), account protection, and centralized exception handling** to ensure secure and scalable API access.

---

## 🚀 Features

### 🔐 Authentication & Authorization

* JWT-based authentication
* Refresh token rotation
* Secure logout using token blacklist
* OAuth2 login support (Google & GitHub)
* Role Based Access Control (RBAC)

### 👤 User & Employee Management

* Create employee
* Update employee
* Delete employee
* Fetch employee by ID
* Get own profile
* Get all employees
* Get all users with roles

### 🛡 Security Features

* Login attempt tracking
* Temporary account lock after multiple failed attempts
* Permanent account block by admin
* Self account blocking
* Token blacklist for logout protection
* Global exception handling

### 📊 Admin Monitoring

* Admin dashboard with system statistics
* Temporary blocked users list
* Permanently blocked users list

### 📑 Logging & Monitoring

* SLF4J logging
* Authentication logs
* Admin activity logs
* Error tracking

---

## 🧰 Technology Stack

* **Java**
* **Spring Boot**
* **Spring Security**
* **JWT Authentication**
* **OAuth2 Client (Google & GitHub)**
* **MySQL**
* **Maven**
* **SLF4J Logging**

---

## 🏗 System Architecture

```
Client (Postman / Frontend)
        │
        ▼
Spring Boot REST API
        │
        ▼
Spring Security Filter Chain
        │
        ├── JWT Authentication Filter
        ├── OAuth2 Authentication
        │
        ▼
Service Layer
        │
        ▼
Repository Layer (JPA)
        │
        ▼
MySQL Database
```

---

## 🗄 Database Schema Overview

### Users Table

Stores authentication and security information.

| Field          | Description              |
| -------------- | ------------------------ |
| id             | User ID                  |
| username       | Login username           |
| password       | Encrypted password       |
| role           | User role (ADMIN / USER) |
| email          | Email address            |
| department     | Department               |
| blocked        | Account block status     |
| blockType      | TEMPORARY / PERMANENT    |
| failedAttempts | Login attempt count      |
| enabled        | Account status           |

---

### Employees Table

Stores employee information.

| Field      | Description         |
| ---------- | ------------------- |
| id         | Employee ID         |
| name       | Employee name       |
| email      | Employee email      |
| department | Department          |
| user_id    | Linked user account |

---

### Refresh Tokens Table

| Field      | Description      |
| ---------- | ---------------- |
| id         | Token ID         |
| token      | Refresh token    |
| username   | Linked user      |
| expiryDate | Token expiration |

---

### Token Blacklist Table

| Field      | Description       |
| ---------- | ----------------- |
| id         | Blacklist ID      |
| token      | Revoked JWT token |
| expiryDate | Token expiration  |

---

## 🔑 Authentication Flow

### Login Flow

```
User Login Request
        │
        ▼
Spring Security Authentication
        │
        ▼
JWT Token Generated
        │
        ├── Access Token
        └── Refresh Token
```

---

### Refresh Token Flow

```
Client sends Refresh Token
        │
        ▼
Validate Refresh Token
        │
        ▼
Generate New Access Token
```

---

### Logout Flow

```
User Logout
        │
        ▼
JWT Token added to Blacklist
        │
        ▼
Token cannot be reused
```

---

## 📡 API Endpoints

| Endpoint               | Method | Access        | Description               |
| ---------------------- | ------ | ------------- | ------------------------- |
| /api/login             | POST   | Public        | User login                |
| /api/refresh           | POST   | Public        | Refresh token             |
| /api/logout            | POST   | Authenticated | Logout user               |
| /api/self-block        | POST   | USER          | Block own account         |
| /api/admin/block/{id}  | POST   | ADMIN         | Block user                |
| /api/unblock/{id}      | POST   | ADMIN         | Unblock user              |
| /api                   | POST   | ADMIN         | Create employee           |
| /api/{id}              | PUT    | ADMIN         | Update employee           |
| /api/{id}              | DELETE | ADMIN         | Delete employee           |
| /api                   | GET    | ADMIN         | Get all employees         |
| /api/users             | GET    | ADMIN         | Get all users             |
| /api/employee/{id}     | GET    | ADMIN         | Get employee              |
| /api/me                | GET    | USER          | Get own profile           |
| /api/dashboard         | GET    | ADMIN         | Dashboard stats           |
| /api/temp-blocked      | GET    | ADMIN         | Temporary blocked users   |
| /api/permanent-blocked | GET    | ADMIN         | Permanently blocked users |

---

## ⚠️ Security Considerations

Sensitive configuration values such as:

* OAuth Client Secrets
* Database Credentials
* JWT Secrets

are **not included in the repository**.

Use a local configuration file such as:

```
application-local.yml
```

or environment variables for secure configuration.

---

## ▶️ Running the Project

### 1. Clone the repository

```
git clone https://github.com/Vibhanshusoni/Employee-Management-System.git
```

### 2. Navigate to the project

```
cd Employee-Management-System
```

### 3. Configure database and OAuth credentials

Create a local configuration file and provide your credentials.

### 4. Run the application

```
./mvnw spring-boot:run
```

The server will start on:

```
http://localhost:8090
```

---

## 👨‍💻 Author

**Vibhanshu Soni**

Computer Science Engineer
Java Backend Developer

GitHub:
https://github.com/Vibhanshusoni
