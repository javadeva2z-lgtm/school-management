# School Management Microservices

A comprehensive Spring Boot microservices architecture for school management system with features including fee management, attendance tracking, homework assignment, result publishing, and more.

## Table of Contents
1. [Project Overview](#project-overview)
2. [System Architecture](#system-architecture)
3. [Microservices](#microservices)
4. [Technology Stack](#technology-stack)
5. [Prerequisites](#prerequisites)
6. [Installation & Setup](#installation--setup)
7. [Database Setup](#database-setup)
8. [Running the Services](#running-the-services)
9. [API Documentation](#api-documentation)
10. [Project Structure](#project-structure)
11. [Configuration](#configuration)
12. [Security](#security)
13. [Deployment](#deployment)

---

## Project Overview

This is an enterprise-grade microservices application built with Spring Boot 3.1+ for managing school operations. It provides:

- **Fee & Payment Management**: Track fees, process payments, send reminders via WhatsApp
- **Attendance System**: Mark attendance and notify students/parents
- **Academic Management**: Assign homework/classwork, publish results, manage exam schedules
- **Communication**: Announcements, events, leave applications
- **User Management**: Student profiles, teacher profiles, admin management with bulk upload capability
- **Notifications**: Multi-channel notifications (WhatsApp, Email, Push)
- **File Management**: Upload and manage documents in Google Cloud Storage

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (Port 8000)                   │
│              Spring Cloud Gateway / Zuul                     │
└─────────────────────────────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
    ┌─────────┐      ┌──────────┐      ┌──────────────┐
    │ User    │      │ Academic │      │   Payment    │
    │Service  │      │ Service  │      │   Service    │
    │:8001    │      │  :8002   │      │    :8003     │
    └─────────┘      └──────────┘      └──────────────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
    ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
    │Notification │  │Communication │  │  Utility     │
    │  Service    │  │   Service    │  │  Service     │
    │   :8004     │  │    :8005     │  │   :8006      │
    └──────────────┘  └──────────────┘  └──────────────┘
                           │
                    ┌──────────────┐
                    │  GCP Pub/Sub │
                    │  (Event Bus) │
                    └──────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
    ┌─────────┐      ┌──────────┐      ┌──────────┐
    │ MySQL   │      │GCP Cloud │      │   GCS    │
    │Database │      │ Storage  │      │  Bucket  │
    └─────────┘      └──────────┘      └──────────┘
```

---

## Microservices

### 1. User Service (Port 8001)
**Responsible for:**
- User authentication and authorization
- Student profile management (CRUD operations, bulk upload)
- Teacher profile management
- Admin profile management
- Class and section management
- Role-based access control

**Key Endpoints:**
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - User login
- `GET /api/v1/students/{id}` - Get student details
- `POST /api/v1/students` - Create student
- `GET /api/v1/classes` - Get all classes
- `GET /api/v1/sections/class/{classId}` - Get sections by class

### 2. Academic Service (Port 8002)
**Responsible for:**
- Attendance marking and tracking
- Homework/classwork assignment
- Result publishing
- Exam date sheet management
- Attendance reports

**Key Endpoints:**
- `POST /api/v1/attendance/mark` - Mark attendance
- `GET /api/v1/attendance/student/{studentId}` - Get student attendance
- `POST /api/v1/homework` - Assign homework
- `GET /api/v1/homework/class/{classId}/section/{sectionId}` - Get class homework
- `POST /api/v1/results/publish` - Publish results
- `GET /api/v1/exam-schedule/class/{classId}` - Get exam schedule

### 3. Payment Service (Port 8003)
**Responsible for:**
- Fee structure management
- Payment tracking
- Payment reminders
- Invoice generation

**Key Endpoints:**
- `GET /api/v1/fees/student/{studentId}` - Get student fees
- `POST /api/v1/payments` - Record payment
- `GET /api/v1/payments/student/{studentId}` - Get payment history
- `GET /api/v1/payment-reminders/pending` - Get pending reminders

### 4. Notification Service (Port 8004)
**Responsible for:**
- WhatsApp notifications
- Email notifications
- Push notifications
- SMS notifications

### 5. Communication Service (Port 8005)
**Responsible for:**
- Announcement management
- Event management
- Leave application handling

### 6. Utility Service (Port 8006)
**Responsible for:**
- File upload/download (GCS integration)
- Report generation
- PDF generation
- Bulk operations

---

## Technology Stack

### Backend
- **Framework:** Spring Boot 3.1.0
- **Language:** Java 17+
- **Build Tool:** Maven 3.8+
- **ORM:** Spring Data JPA/Hibernate
- **Dependency Injection:** Spring Context
- **Validation:** Spring Validation (Jakarta Validation)
- **Security:** Spring Security 6.x + JWT
- **API Documentation:** Springdoc OpenAPI 2.0

### Database
- **Primary:** MySQL 8.0+
- **Connection Pool:** HikariCP
- **Migration:** Flyway (optional)

### Cloud & Messaging
- **Cloud Provider:** Google Cloud Platform (GCP)
- **Messaging:** Google Cloud Pub/Sub
- **File Storage:** Google Cloud Storage (GCS)
- **Service Discovery:** Spring Cloud Eureka (optional)
- **API Gateway:** Spring Cloud Gateway

### Libraries & Tools
- **Logging:** SLF4J + Logback
- **Code Generation:** Lombok 1.18.30
- **JWT:** JJWT 0.11.5
- **Testing:** JUnit 5, Mockito, Spring Boot Test
- **HTTP Client:** Feign (for inter-service communication)

---

## Prerequisites

### System Requirements
- Java 17 or higher
- Maven 3.8 or higher
- MySQL 8.0 or higher
- Docker (optional, for containerization)
- Docker Compose (optional, for multi-container deployment)

### Software Installations

1. **Java Development Kit (JDK) 17+**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install openjdk-17-jdk
   
   # macOS
   brew install openjdk@17
   
   # Verify installation
   java -version
   ```

2. **Maven 3.8+**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install maven
   
   # macOS
   brew install maven
   
   # Verify installation
   mvn -version
   ```

3. **MySQL 8.0+**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install mysql-server
   
   # macOS
   brew install mysql
   
   # Start MySQL
   sudo systemctl start mysql
   mysql -u root -p
   ```

4. **GCP Setup** (optional, for cloud features)
   - Create GCP project
   - Enable Cloud Pub/Sub API
   - Enable Cloud Storage API
   - Create service account and download JSON key
   - Set environment variable: `export GOOGLE_APPLICATION_CREDENTIALS=/path/to/keyfile.json`

---

## Installation & Setup

### 1. Clone Repository
```bash
git clone https://github.com/javadeva2z-lgtm/school-management-microservices.git
cd school-management-microservices
```

### 2. Verify Directory Structure
```bash
ls -la
```

Expected structure:
```
school-management-microservices/
├── pom.xml (parent)
├── DESIGN_DOCUMENT.md
├── database/
│   └── schema.sql
├── common-library/
│   └── pom.xml
├── user-service/
│   ├── pom.xml
│   └── src/
├── academic-service/
│   ├── pom.xml
│   └── src/
├── payment-service/
│   ├── pom.xml
│   └── src/
├── notification-service/
│   ├── pom.xml
│   └── src/
├── communication-service/
│   ├── pom.xml
│   └── src/
├── utility-service/
│   ├── pom.xml
│   └── src/
├── api-gateway/
│   ├── pom.xml
│   └── src/
└── README.md
```

### 3. Build Parent Project
```bash
# Build all modules
mvn clean install -DskipTests

# Build specific module
mvn clean install -DskipTests -pl user-service -am
```

### 4. Update Configuration Files

Update database credentials in each service's `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/school_management?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
```

---

## Database Setup

### 1. Create Database
```bash
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS school_management;
USE school_management;
```

### 2. Execute Schema Script
```bash
mysql -u root -p school_management < database/schema.sql
```

### 3. Verify Tables
```sql
SHOW TABLES;
```

### 4. Create Initial Data (Optional)
```sql
-- Insert sample class
INSERT INTO classes (class_name, academic_year, is_active) VALUES ('Class 10', '2024-2025', true);

-- Insert sample section
INSERT INTO sections (class_id, section_name, capacity, is_active) VALUES (1, 'A', 30, true);

-- Insert sample subject
INSERT INTO subjects (subject_name, subject_code, is_active) VALUES ('Mathematics', 'MATH101', true);
```

---

## Running the Services

### Option 1: Run Individual Services

#### User Service
```bash
cd user-service
mvn spring-boot:run
# Service runs on: http://localhost:8001
# Swagger UI: http://localhost:8001/swagger-ui.html
```

#### Academic Service
```bash
cd academic-service
mvn spring-boot:run
# Service runs on: http://localhost:8002
# Swagger UI: http://localhost:8002/swagger-ui.html
```

#### Payment Service
```bash
cd payment-service
mvn spring-boot:run
# Service runs on: http://localhost:8003
```

### Option 2: Run Using Docker Compose

Create `docker-compose.yml` in root directory:
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: school_management
    ports:
      - "3306:3306"
    volumes:
      - ./database/schema.sql:/docker-entrypoint-initdb.d/schema.sql

  user-service:
    build:
      context: ./user-service
      dockerfile: Dockerfile
    ports:
      - "8001:8001"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/school_management
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root

  academic-service:
    build:
      context: ./academic-service
      dockerfile: Dockerfile
    ports:
      - "8002:8002"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/school_management
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
```

```bash
docker-compose up -d
```

### Option 3: Run All Services (Maven)
```bash
# Terminal 1
cd user-service && mvn spring-boot:run

# Terminal 2
cd academic-service && mvn spring-boot:run

# Terminal 3
cd payment-service && mvn spring-boot:run

# And so on...
```

---

## API Documentation

### Swagger UI URLs
- User Service: http://localhost:8001/swagger-ui.html
- Academic Service: http://localhost:8002/swagger-ui.html
- Payment Service: http://localhost:8003/swagger-ui.html

### Authentication
All endpoints (except `/api/v1/auth/**`) require JWT token in header:
```
Authorization: Bearer <your_jwt_token>
```

### Sample API Calls

#### Register User
```bash
curl -X POST http://localhost:8001/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student1",
    "email": "student1@school.com",
    "password": "password123",
    "phoneNumber": "9876543210",
    "role": "STUDENT"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8001/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student1@school.com",
    "password": "password123"
  }'
```

#### Mark Attendance
```bash
curl -X POST http://localhost:8002/api/v1/attendance/mark \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "studentId": 1,
    "teacherId": 2,
    "classId": 1,
    "sectionId": 1,
    "attendanceDate": "2024-09-05",
    "status": "PRESENT"
  }'
```

---

## Project Structure

### Common Library
```
common-library/
├── src/main/java/com/school/common/
│   ├── response/
│   │   └── ApiResponse.java
│   ├── dto/
│   │   └── PageResponseDTO.java
│   ├── exception/
│   │   ├── ResourceNotFoundException.java
│   │   └── DuplicateResourceException.java
│   ├── util/
│   │   └── JwtTokenProvider.java
│   └── enums/
│       ├── UserRole.java
│       ├── AttendanceStatus.java
│       ├── PaymentStatus.java
│       └── LeaveStatus.java
```

### User Service
```
user-service/
├── src/main/java/com/school/userservice/
│   ├── entity/
│   │   ├── User.java
│   │   ├── UserRole.java
│   │   ├── Student.java
│   │   ├── Teacher.java
│   │   ├── Class.java
│   │   └── Section.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── StudentRepository.java
│   │   ├── TeacherRepository.java
│   │   ├── ClassRepository.java
│   │   └── SectionRepository.java
│   ├── dto/
│   │   ├── UserRegistrationDTO.java
│   │   ├── LoginRequestDTO.java
│   │   ├── LoginResponseDTO.java
│   │   ├── StudentDTO.java
│   │   ├── TeacherDTO.java
│   │   ├── ClassDTO.java
│   │   └── SectionDTO.java
│   ├── converter/
│   │   ├── StudentConverter.java
│   │   ├── TeacherConverter.java
│   │   ├── ClassConverter.java
│   │   └── SectionConverter.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── StudentService.java
│   │   ├── TeacherService.java
│   │   ├── ClassService.java
│   │   └── SectionService.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── StudentController.java
│   │   ├── TeacherController.java
│   │   ├── ClassController.java
│   │   └── SectionController.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── OpenApiConfig.java
│   └── UserServiceApplication.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

### Academic Service
```
academic-service/
├── src/main/java/com/school/academicservice/
│   ├── entity/
│   │   ├── Attendance.java
│   │   ├── Homework.java
│   │   ├── ExamSchedule.java
│   │   ├── Result.java
│   │   └── Subject.java
│   ├── repository/
│   │   ├── AttendanceRepository.java
│   │   ├── HomeworkRepository.java
│   │   ├── ExamScheduleRepository.java
│   │   ├── ResultRepository.java
│   │   └── SubjectRepository.java
│   ├── dto/
│   │   ├── AttendanceDTO.java
│   │   ├── HomeworkDTO.java
│   │   ├── ExamScheduleDTO.java
│   │   ├── ResultDTO.java
│   │   └── SubjectDTO.java
│   ├── converter/
│   │   ├── AttendanceConverter.java
│   │   ├── HomeworkConverter.java
│   │   ├── ExamScheduleConverter.java
│   │   └── ResultConverter.java
│   ├── service/
│   │   ├── AttendanceService.java
│   │   ├── HomeworkService.java
│   │   ├── ExamScheduleService.java
│   │   └── ResultService.java
│   ├── controller/
│   │   ├── AttendanceController.java
│   │   ├── HomeworkController.java
│   │   ├── ExamScheduleController.java
│   │   └── ResultController.java
│   └── AcademicServiceApplication.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

---

## Configuration

### Environment Profiles
Shared settings are stored in `common-library/src/main/resources/common.yml` with profile overlays for `dev`, `uat`, and `prod`. The default profile is `dev`. Activate a profile when starting any service:

### Schema-per-school tenancy

Every database-backed request must include the school schema/database name in
the `X-School-Code` header:

```http
X-School-Code: school_a
```

Swagger UI includes the configured `DB_DEFAULT_SCHEMA` value as the
`X-School-Code` header for requests made with **Try it out**. Change the
header value in the request editor when testing another school.

The common library validates the name (`A-Z`, `a-z`, `0-9`, and `_` only) and
Hibernate switches the MySQL connection to that schema before executing the
request. Configure the schema used during startup and for Hibernate metadata
operations with `DB_DEFAULT_SCHEMA` (defaults to `school_management`).

Each tenant schema must contain the same tables. In production, use
`validate` or a migration tool rather than creating tables from application
startup. Requests without a valid `X-School-Code` header receive HTTP 400.

```bash
java -jar user-service/target/user-service-1.0.0.jar --spring.profiles.active=dev
java -jar user-service/target/user-service-1.0.0.jar --spring.profiles.active=uat
java -jar user-service/target/user-service-1.0.0.jar --spring.profiles.active=prod
```

The equivalent environment variable is `SPRING_PROFILES_ACTIVE`. UAT and production values without defaults are intentionally required at startup.

Shared profile variables:

```text
# Required for uat/prod; DEV_* values have local defaults
DEV_DB_URL=jdbc:mysql://localhost:3306/school_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DEV_DB_USERNAME=root
DEV_DB_PASSWORD=root
UAT_DB_URL=<uat-jdbc-url>
UAT_DB_USERNAME=<uat-username>
UAT_DB_PASSWORD=<uat-password>
PROD_DB_URL=<prod-jdbc-url>
PROD_DB_USERNAME=<prod-username>
PROD_DB_PASSWORD=<prod-password>

DEV_EUREKA_URL=http://localhost:8761/eureka/
UAT_EUREKA_URL=<uat-eureka-url>
PROD_EUREKA_URL=<prod-eureka-url>
DEV_APP_BASE_URL=http://localhost:8000/ui
UAT_APP_BASE_URL=<uat-ui-url>
PROD_APP_BASE_URL=<prod-ui-url>
DEV_JWT_SECRET_KEY=<dev-secret>
UAT_JWT_SECRET_KEY=<uat-secret>
PROD_JWT_SECRET_KEY=<prod-secret>
DEV_CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:8000
UAT_CORS_ALLOWED_ORIGINS=<uat-origins>
PROD_CORS_ALLOWED_ORIGINS=<prod-origins>
```

Common tuning variables include `DB_MAX_POOL_SIZE`, `DB_MIN_IDLE`, `DB_CONNECTION_TIMEOUT`, `JPA_FORMAT_SQL`, `JPA_DDL_AUTO`, `JPA_SHOW_SQL`, and `*_JWT_EXPIRATION_MS`.

Service-specific variables:

```text
API_GATEWAY_PORT, USER_SERVICE_PORT, ACADEMIC_SERVICE_PORT
PAYMENT_SERVICE_PORT, NOTIFICATION_SERVICE_PORT, COMMUNICATION_SERVICE_PORT
UTILITY_SERVICE_PORT, EUREKA_SERVER_PORT
REDIS_HOST, REDIS_PORT, GATEWAY_CORS_ALLOWED_ORIGINS
PAYMENT_REMINDER_DAYS_BEFORE_DUE, PAYMENT_REMINDER_DAYS_AFTER_DUE
PAYMENT_REMINDER_DAYS_AFTER_DUE_14
TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_WHATSAPP_NUMBER
NOTIFICATION_EMAIL_FROM, NOTIFICATION_SMTP_SERVER
GCP_STORAGE_ENABLED, GCS_BUCKET
```

### GCP Configuration
Create `application-gcp.yml` for GCP-specific configuration:
```yaml
spring:
  cloud:
    gcp:
      project-id: your-gcp-project-id
      credentials:
        location: file:path/to/service-account-key.json
```

---

## Security

### Authentication Flow
1. User registers via `/api/v1/auth/register`
2. User logs in via `/api/v1/auth/login`
3. Server returns JWT token
4. Client includes token in `Authorization: Bearer <token>` header
5. JwtAuthenticationFilter validates token for each request
6. User info and roles extracted from token

### Role-Based Access Control (RBAC)
```
ADMIN:
  - Full system access
  - Create/Update/Delete users
  - Create/Update/Delete classes and sections
  - Manage all fees and payments

TEACHER:
  - Mark attendance
  - Assign homework/classwork
  - Publish results
  - Create announcements
  - View student information
  - Approve/Reject leave applications

STUDENT:
  - View own profile
  - View classmates
  - View own attendance
  - View own homework and results
  - View own fees and payments
  - Apply for leave
  - View announcements and events
```

### Password Encoding
Passwords are encoded using BCrypt with default strength of 10.

---

## Deployment

### Containerization with Docker

Create Dockerfile for each service:
```dockerfile
FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build Docker image:
```bash
cd user-service
docker build -t school-user-service:1.0.0 .
```

### GCP Cloud Run Deployment
```bash
# Configure gcloud
gcloud config set project your-project-id

# Build with Cloud Build
gcloud builds submit --tag gcr.io/your-project-id/user-service .

# Deploy to Cloud Run
gcloud run deploy user-service \
  --image gcr.io/your-project-id/user-service \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

### Kubernetes Deployment

Create deployment manifest:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: gcr.io/your-project-id/user-service:1.0.0
        ports:
        - containerPort: 8001
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
```

Deploy:
```bash
kubectl apply -f deployment.yaml
```

---

## Monitoring & Logging

### Health Check Endpoints
```bash
# Actuator health endpoint
curl http://localhost:8001/actuator/health
```

### Logging Configuration
Update `logback-spring.xml` for custom logging.

### Monitoring Tools
- **Spring Boot Actuator:** `/actuator/metrics`
- **Google Cloud Logging:** Integrated with GCP
- **Application Insights:** (Optional) Azure Application Insights

---

## Troubleshooting

### Common Issues

1. **Database Connection Error**
   ```
   Solution: Check MySQL is running and credentials are correct
   ```

2. **Port Already in Use**
   ```bash
   # Find process using port
   lsof -i :8001
   
   # Kill process
   kill -9 <PID>
   ```

3. **JWT Token Validation Failed**
   ```
   Solution: Ensure JWT secret key is same across all services
   ```

4. **Build Failure**
   ```bash
   # Clean build
   mvn clean install -DskipTests
   ```

---

## Contributing

1. Create feature branch: `git checkout -b feature/feature-name`
2. Commit changes: `git commit -am 'Add feature'`
3. Push to branch: `git push origin feature/feature-name`
4. Submit pull request

---

## License

MIT License - See LICENSE file for details

---

## Support & Contact

- **Issues:** GitHub Issues
- **Email:** support@school-management.com
- **Documentation:** See DESIGN_DOCUMENT.md

---

## Acknowledgments

- Spring Boot Team
- Google Cloud Platform
- MySQL Community
- Lombok Project

---

**Last Updated:** September 5, 2026
**Version:** 1.0.0