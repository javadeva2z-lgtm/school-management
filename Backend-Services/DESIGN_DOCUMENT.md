# School Management Microservices - Design Document

## Table of Contents
1. [System Architecture](#system-architecture)
2. [Microservices Overview](#microservices-overview)
3. [Database Design](#database-design)
4. [Technology Stack](#technology-stack)
5. [API Specifications](#api-specifications)
6. [Security & Authorization](#security--authorization)
7. [Integration Points](#integration-points)

---

## System Architecture

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────────┐
│                       API Gateway                                 │
│                   (Spring Cloud Gateway)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│   User Service   │  │ Academic Service │  │ Payment Service  │
│   - Auth         │  │ - Attendance     │  │ - Fee Management │
│   - Profile      │  │ - Homework       │  │ - Payment Track  │
│   - Student      │  │ - Results        │  │ - Reminders      │
│   - Teacher      │  │ - Exam Schedule  │  │                  │
│   - Admin        │  │                  │  │                  │
└──────────────────┘  └──────────────────┘  └──────────────────┘
        │                     │                     │
┌───────��──────────┐  ┌──────────────────┐  ┌──────────────────┐
│ Notification     │  │ Communication    │  │ Utility Service  │
│ Service          │  │ Service          │  │ - File Upload    │
│ - WhatsApp       │  │ - Announcement   │  │ - Storage        │
│ - Email          │  │ - Events         │  │ - Report Gen     │
│ - Push Notify    │  │ - Leave Request  │  │                  │
└──────────────────┘  └──────────────────┘  └──────────────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              │
                    ┌─────────────────────┐
                    │   GCP Pub/Sub       │
                    │   (Event Bus)       │
                    └─────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
    ┌────────────┐       ┌────────────┐       ┌────────────┐
    │ MySQL DB   │       │ Cloud      │       │ GCS        │
    │            │       │ Storage    │       │ (Files)    │
    └────────────┘       └────────────┘       └────────────┘
```

---

## Microservices Overview

### 1. User Service (Port: 8001)
**Responsibilities:**
- User authentication and authorization
- Student profile management (bulk upload, individual update)
- Teacher profile management
- Admin profile management
- Class and section segregation
- Role-based access control

**Key Entities:**
- User, Student, Teacher, Admin
- Class, Section, UserRole

---

### 2. Academic Service (Port: 8002)
**Responsibilities:**
- Attendance marking and tracking
- Homework/Classwork assignment
- Result publishing
- Exam date sheet management
- Attendance reports

**Key Entities:**
- Attendance, Homework, Result, ExamSchedule
- Subject, Marks

---

### 3. Payment Service (Port: 8003)
**Responsibilities:**
- Fee structure management
- Payment tracking
- Payment reminders
- Invoice generation

**Key Entities:**
- Fee, Payment, PaymentReminder
- FeeStructure, Invoice

---

### 4. Notification Service (Port: 8004)
**Responsibilities:**
- WhatsApp notifications
- Email notifications
- Push notifications
- SMS notifications

**Key Entities:**
- Notification, NotificationTemplate

---

### 5. Communication Service (Port: 8005)
**Responsibilities:**
- Announcement management
- Event management
- Leave application handling

**Key Entities:**
- Announcement, Event, LeaveApplication

---

### 6. Utility Service (Port: 8006)
**Responsibilities:**
- File upload/download (GCS integration)
- Report generation
- PDF generation
- Bulk operations

**Key Entities:**
- FileMetadata, Report

---

## Database Design

### Database: `school_management`

Detailed SQL scripts are in the `database/schema.sql` file.

**Key Tables:**
- users, user_roles
- students, teachers, admin
- classes, sections
- attendance, subjects, homework, exam_schedule, results
- fees, fee_structure, payments, payment_reminders
- announcements, events, leave_applications
- notifications, notification_templates
- file_metadata

---

## Technology Stack

### Backend
- **Framework:** Spring Boot 3.1.x
- **Language:** Java 17+
- **Build Tool:** Maven
- **ORM:** Spring Data JPA
- **Annotation Processing:** Lombok
- **API Documentation:** Swagger/Springdoc-openapi

### Database
- **Primary:** MySQL 8.0+
- **Connection Pool:** HikariCP

### Cloud
- **Cloud Provider:** Google Cloud Platform (GCP)
- **Messaging:** Google Cloud Pub/Sub
- **File Storage:** Google Cloud Storage (GCS)

### Security
- **Authentication:** Spring Security + JWT
- **Authorization:** Role-Based Access Control (RBAC)

### Other
- **API Gateway:** Spring Cloud Gateway
- **Logging:** SLF4J + Logback
- **Validation:** Spring Validation
- **Testing:** JUnit 5, Mockito

---

## API Specifications

### Base URL
`http://localhost:8000/api/v1` (via API Gateway)

### Authentication Flow
```
POST /auth/login
POST /auth/register
POST /auth/refresh-token
POST /auth/logout
```

### Common Response Format
```json
{
  "status": "SUCCESS/ERROR",
  "code": 200,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2026-09-05T10:30:00Z"
}
```

---

## Security & Authorization

### JWT Token Claims
```json
{
  "sub": "user_id",
  "username": "username",
  "email": "email@school.com",
  "roles": ["STUDENT"],
  "class_id": "classId",
  "section_id": "sectionId",
  "iat": 1234567890,
  "exp": 1234571490
}
```

### Role-Based Access Control
- **ADMIN:** Full system access
- **TEACHER:** Attendance, homework, results, announcements
- **STUDENT:** View classmates, homework, results, fees, apply leave
- **PARENT:** (Future) View child's progress

---

## Integration Points

### GCP Pub/Sub Topics
- `attendance-events`
- `homework-events`
- `result-events`
- `payment-events`
- `notification-events`
- `event-events`
- `leave-events`

### External Services
- **WhatsApp:** Twillio / Gupshup
- **Email:** SendGrid / AWS SES
- **GCS Buckets:** `school-homework`, `school-results`, `school-files`

---

## File Structure
```
school-management-microservices/
├── user-service/
├── academic-service/
├── payment-service/
├── notification-service/
├── communication-service/
├── utility-service/
├── api-gateway/
├── database/
│   └── schema.sql
├── docs/
├── docker-compose.yml
├── pom.xml (parent)
└── README.md
```

---

**Document Version:** 1.0  
**Last Updated:** 2026-09-05
