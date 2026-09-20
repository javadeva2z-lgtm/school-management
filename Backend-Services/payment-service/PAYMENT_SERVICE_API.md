# Payment Service API Documentation

## Overview
The Payment Service is a comprehensive microservice responsible for managing all fee and payment-related operations in the School Management System. It handles fee structures, student fees, payment recording, and automated payment reminders.

## Key Features

### 1. Fee Structure Management
- Create fee structures for different classes
- Define multiple fee types (Tuition, Transport, Activity, etc.)
- Set due dates and amounts
- Manage fee structures by academic year
- Activate/Deactivate fee structures

### 2. Fee Management
- Create fees for students based on fee structures
- Track fee status (PENDING, PARTIAL, PAID, OVERDUE, CANCELLED)
- Query fees by student, academic year, or date range
- Update fee status automatically based on payments
- Generate fees in bulk for a class

### 3. Payment Processing
- Record payments with various methods (CASH, CARD, UPI, BANK_TRANSFER, CHEQUE)
- Automatic fee status updates after payment
- Support for partial payments
- Transaction ID tracking
- Receipt URL storage
- Payment remarks/notes

### 4. Payment Reminders
- Automated reminder generation
- Customizable reminder types (DUE_DATE, OVERDUE_7DAYS, OVERDUE_14DAYS)
- Send reminders via multiple channels (WhatsApp, Email, SMS)
- Track sent reminders
- Query pending reminders

## API Endpoints

### Base URL
```
http://localhost:8003/api/v1
```

### Authentication
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer <jwt_token>
```

## Fee Structure Endpoints

### Create Fee Structure
```http
POST /fee-structure
Content-Type: application/json
Authorization: Bearer <token>

{
  "classId": 1,
  "feeType": "TUITION",
  "amount": 5000.00,
  "dueDate": "2024-12-31",
  "academicYear": "2024-2025",
  "isActive": true
}
```

**Response:**
```json
{
  "status": "SUCCESS",
  "code": 201,
  "message": "Fee structure created successfully",
  "data": {
    "id": 1,
    "classId": 1,
    "feeType": "TUITION",
    "amount": 5000.00,
    "dueDate": "2024-12-31",
    "academicYear": "2024-2025",
    "isActive": true
  },
  "timestamp": "2024-09-05T10:30:00Z"
}
```

### Get Fee Structure by ID
```http
GET /fee-structure/{id}
Authorization: Bearer <token>
```

### Get Fee Structures by Class
```http
GET /fee-structure/class/{classId}?academicYear=2024-2025
Authorization: Bearer <token>
```

### Get All Active Fee Structures
```http
GET /fee-structure/active
Authorization: Bearer <token>
```

### Update Fee Structure
```http
PUT /fee-structure/{id}
Content-Type: application/json
Authorization: Bearer <token>

{
  "classId": 1,
  "feeType": "TUITION",
  "amount": 5500.00,
  "dueDate": "2024-12-31",
  "academicYear": "2024-2025",
  "isActive": true
}
```

### Delete Fee Structure
```http
DELETE /fee-structure/{id}
Authorization: Bearer <token>
```

## Fee Endpoints

### Create Fee for Student
```http
POST /fees
Content-Type: application/json
Authorization: Bearer <token>

{
  "studentId": 1,
  "feeStructureId": 1,
  "amount": 5000.00,
  "dueDate": "2024-12-31",
  "academicYear": "2024-2025"
}
```

### Get Fee by ID
```http
GET /fees/{id}
Authorization: Bearer <token>
```

### Get All Fees for Student
```http
GET /fees/student/{studentId}
Authorization: Bearer <token>
```

### Get Paginated Fees for Student
```http
GET /fees/student/{studentId}/paginated?page=0&size=10
Authorization: Bearer <token>
```

### Get Fees by Academic Year
```http
GET /fees/student/{studentId}/academic-year/{academicYear}
Authorization: Bearer <token>
```

### Get All Pending Fees
```http
GET /fees/pending
Authorization: Bearer <token>
```

**Response:**
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "studentId": 1,
      "feeStructureId": 1,
      "amount": 5000.00,
      "dueDate": "2024-12-31",
      "academicYear": "2024-2025",
      "status": "PENDING"
    }
  ],
  "timestamp": "2024-09-05T10:30:00Z"
}
```

### Get All Overdue Fees
```http
GET /fees/overdue
Authorization: Bearer <token>
```

### Update Fee Status
```http
PUT /fees/{id}/status?status=PARTIAL
Authorization: Bearer <token>
```

### Get Fees by Due Date Range
```http
GET /fees/due-date-range?fromDate=2024-09-01&toDate=2024-09-30
Authorization: Bearer <token>
```

### Delete Fee
```http
DELETE /fees/{id}
Authorization: Bearer <token>
```

## Payment Endpoints

### Record Payment
```http
POST /payments
Content-Type: application/json
Authorization: Bearer <token>

{
  "feeId": 1,
  "studentId": 1,
  "amountPaid": 5000.00,
  "paymentMethod": "CARD",
  "transactionId": "TXN123456",
  "paymentDate": "2024-09-05T10:30:00Z",
  "receiptUrl": "https://gcs.example.com/receipt.pdf",
  "remarks": "Payment received successfully"
}
```

**Response:**
```json
{
  "status": "SUCCESS",
  "code": 201,
  "message": "Payment recorded successfully",
  "data": {
    "id": 1,
    "feeId": 1,
    "studentId": 1,
    "amountPaid": 5000.00,
    "paymentMethod": "CARD",
    "transactionId": "TXN123456",
    "paymentDate": "2024-09-05T10:30:00Z",
    "receiptUrl": "https://gcs.example.com/receipt.pdf",
    "status": "COMPLETED",
    "remarks": "Payment received successfully"
  },
  "timestamp": "2024-09-05T10:30:00Z"
}
```

### Get Payment by ID
```http
GET /payments/{id}
Authorization: Bearer <token>
```

### Get All Payments for Student
```http
GET /payments/student/{studentId}
Authorization: Bearer <token>
```

### Get Paginated Payments for Student
```http
GET /payments/student/{studentId}/paginated?page=0&size=10
Authorization: Bearer <token>
```

### Get All Payments for Fee
```http
GET /payments/fee/{feeId}
Authorization: Bearer <token>
```

### Get Payment by Transaction ID
```http
GET /payments/transaction/{transactionId}
Authorization: Bearer <token>
```

### Get Payments by Date Range
```http
GET /payments/date-range?fromDate=2024-09-01T00:00:00Z&toDate=2024-09-30T23:59:59Z
Authorization: Bearer <token>
```

### Update Payment
```http
PUT /payments/{id}
Content-Type: application/json
Authorization: Bearer <token>

{
  "feeId": 1,
  "studentId": 1,
  "amountPaid": 5000.00,
  "paymentMethod": "CARD",
  "receiptUrl": "https://gcs.example.com/receipt.pdf",
  "remarks": "Updated remarks"
}
```

### Get Total Payments for Student
```http
GET /payments/student/{studentId}/total
Authorization: Bearer <token>

Response: { "status": "SUCCESS", "data": 15000.00, ... }
```

### Delete Payment
```http
DELETE /payments/{id}
Authorization: Bearer <token>
```

## Payment Reminder Endpoints

### Create Payment Reminder
```http
POST /payment-reminders
Content-Type: application/json
Authorization: Bearer <token>

{
  "feeId": 1,
  "studentId": 1,
  "reminderType": "DUE_DATE",
  "reminderDate": "2024-12-28"
}
```

### Get Reminder by ID
```http
GET /payment-reminders/{id}
Authorization: Bearer <token>
```

### Get Reminders for Student
```http
GET /payment-reminders/student/{studentId}
Authorization: Bearer <token>
```

### Get All Pending Reminders
```http
GET /payment-reminders/pending
Authorization: Bearer <token>
```

**Response:**
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "feeId": 1,
      "studentId": 1,
      "reminderType": "DUE_DATE",
      "reminderDate": "2024-12-28",
      "isSent": false,
      "feeType": "TUITION",
      "amount": 5000.00,
      "dueDate": "2024-12-31"
    }
  ],
  "timestamp": "2024-09-05T10:30:00Z"
}
```

### Get Reminders by Date
```http
GET /payment-reminders/date/{reminderDate}
Authorization: Bearer <token>
```

### Get Reminders for Fee
```http
GET /payment-reminders/fee/{feeId}
Authorization: Bearer <token>
```

### Mark Reminder as Sent
```http
PUT /payment-reminders/{id}/mark-sent
Authorization: Bearer <token>
```

### Delete Reminder
```http
DELETE /payment-reminders/{id}
Authorization: Bearer <token>
```

### Generate Payment Reminders
```http
POST /payment-reminders/generate?daysBeforeDue=3&daysAfterDue=7
Authorization: Bearer <token>

Response: { "status": "SUCCESS", "data": "Reminders generated successfully", ... }
```

## Status Codes

- **200 OK** - Successful GET/PUT/DELETE request
- **201 CREATED** - Successful POST request
- **400 BAD REQUEST** - Invalid input/validation error
- **401 UNAUTHORIZED** - Missing or invalid JWT token
- **403 FORBIDDEN** - Insufficient permissions
- **404 NOT FOUND** - Resource not found
- **409 CONFLICT** - Resource already exists
- **500 INTERNAL SERVER ERROR** - Server error

## Error Response Format

```json
{
  "status": "ERROR",
  "code": 400,
  "message": "Validation failed or resource not found",
  "data": null,
  "timestamp": "2024-09-05T10:30:00Z"
}
```

## Role-Based Access Control

### ADMIN Role
- ✅ Full access to all endpoints
- ✅ Create/Update/Delete fee structures
- ✅ Create/Update/Delete fees
- ✅ Record payments
- ✅ Create/Update/Delete reminders
- ✅ Generate reminders
- ✅ View all payment data

### TEACHER Role
- ✅ View fee structures
- ✅ View student fees
- ✅ View payments
- ✅ View reminders
- ❌ Cannot create/modify fees or payments
- ❌ Cannot manage fee structures

### STUDENT Role
- ✅ View own fees
- ✅ View own payments
- ✅ View own payment reminders
- ❌ Cannot modify any payment data
- ❌ Cannot view other students' data

## Swagger UI

API documentation is available at:
```
http://localhost:8003/swagger-ui.html
```

## Common Use Cases

### 1. Create Fee Structure for New Academic Year
```bash
# Create tuition fee
curl -X POST http://localhost:8003/api/v1/fee-structure \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "classId": 1,
    "feeType": "TUITION",
    "amount": 5000,
    "dueDate": "2024-12-31",
    "academicYear": "2024-2025",
    "isActive": true
  }'
```

### 2. Create Fees in Bulk for All Students in Class
```bash
# Get all fee structures for class
curl http://localhost:8003/api/v1/fee-structure/class/1?academicYear=2024-2025 \
  -H "Authorization: Bearer $TOKEN"

# For each fee structure and student, create fee
curl -X POST http://localhost:8003/api/v1/fees \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "feeStructureId": 1,
    "amount": 5000,
    "dueDate": "2024-12-31",
    "academicYear": "2024-2025"
  }'
```

### 3. Record Student Payment
```bash
curl -X POST http://localhost:8003/api/v1/payments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "feeId": 1,
    "studentId": 1,
    "amountPaid": 5000,
    "paymentMethod": "CARD",
    "transactionId": "TXN123456",
    "paymentDate": "2024-09-05T10:30:00Z",
    "remarks": "Full payment received"
  }'
```

### 4. Generate Payment Reminders
```bash
curl -X POST http://localhost:8003/api/v1/payment-reminders/generate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}
```

### 5. Get Pending Fees Report
```bash
curl http://localhost:8003/api/v1/fees/pending \
  -H "Authorization: Bearer $TOKEN"
```

## Database Schema

### fee_structure table
```sql
CREATE TABLE fee_structure (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  fee_type VARCHAR(100) NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  due_date DATE NOT NULL,
  academic_year VARCHAR(10) NOT NULL,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (class_id) REFERENCES classes(id),
  INDEX idx_class_id (class_id),
  INDEX idx_academic_year (academic_year)
);
```

### fees table
```sql
CREATE TABLE fees (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  fee_structure_id BIGINT NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  due_date DATE NOT NULL,
  academic_year VARCHAR(10) NOT NULL,
  status VARCHAR(20) DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  FOREIGN KEY (fee_structure_id) REFERENCES fee_structure(id),
  INDEX idx_student_id (student_id),
  INDEX idx_status (status),
  INDEX idx_due_date (due_date)
);
```

### payments table
```sql
CREATE TABLE payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  fee_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  amount_paid DECIMAL(10, 2) NOT NULL,
  payment_method VARCHAR(50) NOT NULL,
  transaction_id VARCHAR(100) UNIQUE,
  payment_date TIMESTAMP NOT NULL,
  receipt_url VARCHAR(500),
  status VARCHAR(20) DEFAULT 'COMPLETED',
  remarks TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (fee_id) REFERENCES fees(id),
  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  INDEX idx_student_id (student_id),
  INDEX idx_payment_date (payment_date),
  INDEX idx_transaction_id (transaction_id)
);
```

### payment_reminders table
```sql
CREATE TABLE payment_reminders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  fee_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  reminder_type VARCHAR(50) NOT NULL,
  reminder_date DATE NOT NULL,
  is_sent BOOLEAN DEFAULT false,
  sent_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (fee_id) REFERENCES fees(id),
  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  INDEX idx_student_id (student_id),
  INDEX idx_reminder_date (reminder_date),
  INDEX idx_is_sent (is_sent)
);
```

## Transaction Safety

All payment-related operations are wrapped in `@Transactional` annotations to ensure:
- Atomicity: All-or-nothing operations
- Consistency: Database state remains consistent
- Isolation: Concurrent operations don't interfere
- Durability: Committed data survives failures

## Performance Optimization

- Database indexes on frequently queried columns
- Pagination support for large datasets
- Lazy loading for related entities
- Connection pooling via HikariCP

## Future Enhancements

- Integration with payment gateways (Razorpay, PayPal)
- Automated email receipts
- SMS payment confirmations
- Advanced reporting and analytics
- Payment plan/installment support
- Refund management
- Discount/waiver support

---

**Version:** 1.0.0  
**Last Updated:** September 5, 2026