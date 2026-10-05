-- Schema generated from the JPA entity classes.
-- Database: MySQL 8+
-- Cross-service identifiers remain scalar columns because the entities do not
-- declare JPA relationships and each service owns its own aggregate.

-- Change this value to deploy the schema to a different database.
SET @schema_name = 'school_management';

SET @create_schema_sql = CONCAT(
    'CREATE DATABASE IF NOT EXISTS `',
    REPLACE(@schema_name, '`', '``'),
    '` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci'
);
PREPARE create_schema_statement FROM @create_schema_sql;
EXECUTE create_schema_statement;
DEALLOCATE PREPARE create_schema_statement;

-- MySQL does not allow USE to be executed through a prepared statement.
-- Keep this literal in sync with @schema_name when changing the target.
-- Alternatively, select the target database in the client before execution.
USE school_management;

-- Schema generated from the JPA entity classes across all services.
-- Database: MySQL 8+
-- Note: cross-service identifiers are stored as scalar columns, matching the current
-- domain model where each service owns its own aggregate and no JPA relationships are
-- mapped across service boundaries.

CREATE TABLE IF NOT EXISTS schools (
    id BIGINT NOT NULL AUTO_INCREMENT,
    school_code VARCHAR(255) NOT NULL,
    school_name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    website VARCHAR(255),
    principal_name VARCHAR(255),
    announcement VARCHAR(255),
    logo LONGBLOB,
    favicon LONGBLOB,
    banner LONGBLOB,
    keywords VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_schools_school_code (school_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO schools (school_code, school_name, is_active, created_at, created_by, updated_by)
SELECT 'school_management', 'School Management Application', TRUE, NOW(), 'schema-seed', 'schema-seed'
WHERE NOT EXISTS (
    SELECT 1
    FROM schools
    WHERE school_code COLLATE utf8mb4_unicode_ci = 'school_management' COLLATE utf8mb4_unicode_ci
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    token VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS classes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    class_name VARCHAR(255) NOT NULL,
    academic_year VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sections (
    id BIGINT NOT NULL AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    section_name VARCHAR(255) NOT NULL,
    capacity INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS class_subjects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    subject_name VARCHAR(255) NOT NULL,
    subject_code VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_class_subject_code (class_id, subject_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS students (
    admission_number BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    roll_number BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    section_Name VARCHAR(255) NOT NULL,
    father_name VARCHAR(255),
    mother_name VARCHAR(255),
    date_of_birth DATE,
    admission_date DATE,
    address TEXT,
    is_ews BOOLEAN DEFAULT FALSE,
    parent_phone VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    PRIMARY KEY (admission_number),
    UNIQUE KEY uk_students_email (email),
    UNIQUE KEY uk_class_section_roll (class_id, section_Name, roll_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS teachers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    employee_id VARCHAR(255) NOT NULL,
    qualification VARCHAR(255),
    specialization VARCHAR(255),
    joining_date DATE,
    experience_years INT,
    address VARCHAR(255),
    phone VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    level VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_teachers_email (email),
    UNIQUE KEY uk_teachers_username (username),
    UNIQUE KEY uk_teachers_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS class_teacher (
    id BIGINT NOT NULL AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    section_name VARCHAR(255) NOT NULL,
    teacher_id BIGINT NOT NULL,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admission_number BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    section_name VARCHAR(255) NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    remarks VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    on_leave BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_schedule (
    id BIGINT NOT NULL AUTO_INCREMENT,
    exam_name VARCHAR(255) NOT NULL,
    class_id BIGINT NOT NULL,
    section_name VARCHAR(255) NOT NULL,
    subject_id BIGINT NOT NULL,
    exam_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room_number VARCHAR(255),
    max_marks INT DEFAULT 100,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS homework (
    id BIGINT NOT NULL AUTO_INCREMENT,
    teacher_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    section_name VARCHAR(255) NOT NULL,
    subject_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_url VARCHAR(255),
    due_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    work_type VARCHAR(50),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS homework_file (
    id BIGINT NOT NULL AUTO_INCREMENT,
    homework_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    file_size BIGINT NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    download_url VARCHAR(1000),
    PRIMARY KEY (id),
    CONSTRAINT fk_homework_file_homework
        FOREIGN KEY (homework_id) REFERENCES homework (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admission_number BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    exam_schedule_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    marks_obtained INT NOT NULL,
    out_of INT NOT NULL DEFAULT 100,
    grade VARCHAR(10),
    file_url VARCHAR(255),
    published_date DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS subjects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    subject_name VARCHAR(255) NOT NULL,
    subject_code VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_subjects_subject_code (subject_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fee_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    mandatory BOOLEAN NOT NULL,
    default_amount DOUBLE NOT NULL DEFAULT 0.0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS monthly_fees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admission_number BIGINT NOT NULL,
    month VARCHAR(7) NOT NULL,
    base_amount DOUBLE NOT NULL DEFAULT 0.0,
    waiver_amount DOUBLE NOT NULL DEFAULT 0.0,
    penalty_amount DOUBLE NOT NULL DEFAULT 0.0,
    total_payable DOUBLE NOT NULL DEFAULT 0.0,
    paid_amount DOUBLE NOT NULL DEFAULT 0.0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    PRIMARY KEY (id),
    UNIQUE KEY uk_monthly_fees_admission_month (admission_number, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admission_number BIGINT NOT NULL,
    monthly_fee_id BIGINT NOT NULL,
    month VARCHAR(7) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    payment_method VARCHAR(255) NOT NULL,
    amount_paid DOUBLE NOT NULL,
    payment_date DATETIME NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_payments_admission_number (admission_number),
    UNIQUE KEY uk_payments_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gateway_payment_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reference VARCHAR(64) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_reference VARCHAR(128) NOT NULL,
    admission_number BIGINT NOT NULL,
    monthly_fee_ids VARCHAR(2000) NOT NULL,
    amount_paise BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    qr_image_url VARCHAR(1000),
    qr_payload VARCHAR(4000),
    provider_payment_id VARCHAR(128),
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_gateway_payment_orders_reference (reference)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_reminders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    monthly_fee_id BIGINT NOT NULL,
    admission_number BIGINT NOT NULL,
    reminder_type VARCHAR(50) NOT NULL,
    amount DOUBLE NOT NULL,
    due_date DATE NOT NULL,
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS student_service_subscriptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admission_number BIGINT NOT NULL,
    fee_item_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_service_subscription (admission_number, fee_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_by BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    file_url VARCHAR(500),
    class_id BIGINT,
    section_name VARCHAR(255),
    posted_date DATETIME,
    expires_date DATETIME,
    is_active BOOLEAN,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_by BIGINT NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    description TEXT,
    event_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    location VARCHAR(255),
    file_url VARCHAR(500),
    video_url VARCHAR(500),
    class_id BIGINT,
    is_active BOOLEAN,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS leave_applications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admission_number BIGINT NOT NULL,
    reason TEXT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    total_days INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    approved_by VARCHAR(255),
    approval_date DATETIME,
    remarks VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL,
    notification_type VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    reference_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL,
    notification_channel VARCHAR(255) NOT NULL,
    recipient_address VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    status VARCHAR(255) NOT NULL,
    sent_at DATETIME,
    delivered_at DATETIME,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification_templates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    template_name VARCHAR(255) NOT NULL,
    notification_type VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message_template TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_templates_template_name (template_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS file_metadata (
    id BIGINT NOT NULL AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50),
    bucket_name VARCHAR(255),
    object_name VARCHAR(500),
    uploaded_by BIGINT NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    created_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed the standard classes, sections, subjects, and fee types.
-- Re-running this block will not create duplicate seed records.
-- Class ID 0 is reserved; -3, -2, and -1 represent Nursery, LKG, and UKG.
-- Change this value if the seeded class records should use another academic year.
SET @seed_academic_year = CONCAT(YEAR(CURDATE()), '-', YEAR(CURDATE()) + 1);

DROP TEMPORARY TABLE IF EXISTS seed_school_classes;
CREATE TEMPORARY TABLE seed_school_classes (
    class_id BIGINT NOT NULL PRIMARY KEY,
    class_name VARCHAR(255) NOT NULL
);

INSERT INTO seed_school_classes (class_id, class_name) VALUES
    (-3, 'Nursery'),
    (-2, 'LKG'),
    (-1, 'UKG'),
    (1, 'Class 1'),
    (2, 'Class 2'),
    (3, 'Class 3'),
    (4, 'Class 4'),
    (5, 'Class 5'),
    (6, 'Class 6'),
    (7, 'Class 7'),
    (8, 'Class 8'),
    (9, 'Class 9'),
    (10, 'Class 10'),
    (11, 'Class 11'),
    (12, 'Class 12');

INSERT INTO classes (class_id, class_name, academic_year, is_active, created_at)
SELECT seed.class_id, seed.class_name, @seed_academic_year, TRUE, NOW()
FROM seed_school_classes seed
WHERE NOT EXISTS (
    SELECT 1
    FROM classes existing
    WHERE existing.class_id = seed.class_id
      AND existing.academic_year COLLATE utf8mb4_unicode_ci =
          @seed_academic_year COLLATE utf8mb4_unicode_ci
);

INSERT INTO sections (class_id, section_name, is_active, created_at, updated_by)
SELECT seed.class_id, section_seed.section_name, TRUE, NOW(), 'schema-seed'
FROM seed_school_classes seed
CROSS JOIN (
    SELECT 'A' AS section_name
    UNION ALL SELECT 'B'
    UNION ALL SELECT 'C'
    UNION ALL SELECT 'D'
) section_seed
WHERE NOT EXISTS (
    SELECT 1
    FROM sections existing
    WHERE existing.class_id = seed.class_id
      AND existing.section_name = section_seed.section_name
);

INSERT INTO class_subjects (class_id, subject_name, subject_code, is_active)
SELECT seed.class_id, subject_seed.subject_name, subject_seed.subject_code, TRUE
FROM seed_school_classes seed
CROSS JOIN (
    SELECT 'Hindi' AS subject_name, 'HIN' AS subject_code
    UNION ALL SELECT 'English', 'ENG'
    UNION ALL SELECT 'Math', 'MAT'
    UNION ALL SELECT 'GK', 'GK'
    UNION ALL SELECT 'Activity', 'ACT'
) subject_seed
WHERE NOT EXISTS (
    SELECT 1
    FROM class_subjects existing
    WHERE existing.class_id = seed.class_id
      AND existing.subject_code = subject_seed.subject_code
);

INSERT INTO fee_items (class_id, service_name, mandatory, default_amount, active)
SELECT seed.class_id, fee_seed.service_name, fee_seed.mandatory, 0.00, TRUE
FROM seed_school_classes seed
CROSS JOIN (
    SELECT 'Tuition Fee' AS service_name, TRUE AS mandatory
    UNION ALL SELECT 'Exam Fee', TRUE
    UNION ALL SELECT 'Computer Lab', TRUE
    UNION ALL SELECT 'Annual Fee', TRUE
    UNION ALL SELECT 'Transport Fee', FALSE
    UNION ALL SELECT 'Other Fee', TRUE
) fee_seed
WHERE NOT EXISTS (
    SELECT 1
    FROM fee_items existing
    WHERE existing.class_id = seed.class_id
      AND existing.service_name = fee_seed.service_name
);

DROP TEMPORARY TABLE seed_school_classes;
