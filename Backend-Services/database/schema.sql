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
    logo VARBINARY(255),
    favicon VARBINARY(255),
    banner VARBINARY(255),
    keywords VARCHAR(255),
    is_active BOOLEAN,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_schools_school_code (school_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    is_active BOOLEAN,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255) NOT NULL,
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
    is_active BOOLEAN,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sections (
    id BIGINT NOT NULL AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    section_name VARCHAR(255) NOT NULL,
    capacity INT,
    is_active BOOLEAN,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS students (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    admission_number BIGINT NOT NULL,
    roll_number BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    section_Name VARCHAR(255) NOT NULL,
    father_name VARCHAR(255),
    mother_name VARCHAR(255),
    date_of_birth DATE,
    address TEXT,
    parent_phone VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
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
    updated_by VARCHAR(255) NOT NULL,
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
    status VARCHAR(255) NOT NULL,
    remarks VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    on_leave boolean,
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
    max_marks INT,
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
    work_type VARCHAR(255),
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
    out_of INT NOT NULL,
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
    is_active BOOLEAN,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_subjects_subject_code (subject_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fee_structure (
    id BIGINT NOT NULL AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    fee_type VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    due_date DATE NOT NULL,
    academic_year VARCHAR(255) NOT NULL,
    is_active BOOLEAN,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    fee_structure_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    due_date DATE NOT NULL,
    academic_year VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fee_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    amount_paid DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(255),
    payment_date DATETIME NOT NULL,
    receipt_url VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    remarks TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_reminders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fee_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    reminder_type VARCHAR(255) NOT NULL,
    reminder_date DATE NOT NULL,
    is_sent BOOLEAN,
    sent_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_by BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    file_url VARCHAR(500),
    class_id BIGINT,
    section_name VARCHAR(2),
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
    status VARCHAR(20) NOT NULL,
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
    is_read BOOLEAN,
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
    retry_count INT,
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
    is_active BOOLEAN,
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
