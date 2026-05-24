-- ═══════════════════════════════════════════════════════════════
-- ResolvIt - Complaint Management System
-- Database Schema v1.0.0
-- "Every complaint heard. Every issue resolved."
-- ═══════════════════════════════════════════════════════════════

-- Create database
CREATE DATABASE IF NOT EXISTS resolvit_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE resolvit_db;

-- ═══════════════════════════════════════════════════════════════
-- TABLE: departments
-- Stores department information for complaint routing
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS departments (
    dept_id       INT AUTO_INCREMENT PRIMARY KEY,
    dept_name     VARCHAR(100) NOT NULL UNIQUE,
    dept_head     VARCHAR(100),
    contact_email VARCHAR(150),
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_dept_name (dept_name),
    INDEX idx_dept_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════
-- TABLE: users
-- Stores all user accounts (Students, Admins, SuperAdmins)
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS users (
    user_id       VARCHAR(36) PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('STUDENT', 'ADMIN', 'SUPERADMIN') NOT NULL,
    dept_id       INT,
    roll_number   VARCHAR(20),
    class_name    VARCHAR(50),
    phone         VARCHAR(15),
    profile_pic   VARCHAR(500),
    is_active     BOOLEAN DEFAULT TRUE,
    last_login    TIMESTAMP NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE SET NULL,
    
    INDEX idx_user_email (email),
    INDEX idx_user_role (role),
    INDEX idx_user_active (is_active),
    INDEX idx_user_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════
-- TABLE: complaints
-- Core table for storing all complaints
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS complaints (
    complaint_id  VARCHAR(36) PRIMARY KEY,
    user_id       VARCHAR(36) NOT NULL,
    title         VARCHAR(200) NOT NULL,
    description   TEXT NOT NULL,
    category      ENUM('HOSTEL', 'ACADEMICS', 'IT', 'TRANSPORT', 'CANTEEN', 'OTHER') NOT NULL,
    priority      ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
    status        ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'REOPENED') NOT NULL DEFAULT 'PENDING',
    attachment    VARCHAR(500),
    is_anonymous  BOOLEAN DEFAULT FALSE,
    view_count    INT DEFAULT 0,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at   TIMESTAMP NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    INDEX idx_complaint_user (user_id),
    INDEX idx_complaint_status (status),
    INDEX idx_complaint_category (category),
    INDEX idx_complaint_priority (priority),
    INDEX idx_complaint_created (created_at),
    INDEX idx_complaint_status_category (status, category),
    INDEX idx_complaint_status_priority (status, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════
-- TABLE: assignments
-- Tracks complaint assignments to departments
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS assignments (
    assignment_id VARCHAR(36) PRIMARY KEY,
    complaint_id  VARCHAR(36) NOT NULL,
    dept_id       INT NOT NULL,
    assigned_by   VARCHAR(36) NOT NULL,
    assigned_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remarks       TEXT,
    
    FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id) ON DELETE CASCADE,
    FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(user_id) ON DELETE CASCADE,
    
    INDEX idx_assignment_complaint (complaint_id),
    INDEX idx_assignment_dept (dept_id),
    INDEX idx_assignment_date (assigned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════
-- TABLE: status_history
-- Full audit trail of status changes
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS status_history (
    history_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id  VARCHAR(36) NOT NULL,
    old_status    VARCHAR(20),
    new_status    VARCHAR(20) NOT NULL,
    changed_by    VARCHAR(36) NOT NULL,
    remarks       TEXT,
    changed_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE CASCADE,
    
    INDEX idx_history_complaint (complaint_id),
    INDEX idx_history_changed_at (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════
-- TABLE: notifications
-- User notifications for status updates, assignments, etc.
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS notifications (
    notif_id      VARCHAR(36) PRIMARY KEY,
    user_id       VARCHAR(36) NOT NULL,
    title         VARCHAR(200) NOT NULL,
    message       TEXT NOT NULL,
    type          ENUM('STATUS_UPDATE', 'ASSIGNMENT', 'REMINDER', 'SYSTEM') NOT NULL,
    is_read       BOOLEAN DEFAULT FALSE,
    related_id    VARCHAR(36),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    INDEX idx_notif_user (user_id),
    INDEX idx_notif_read (is_read),
    INDEX idx_notif_created (created_at),
    INDEX idx_notif_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════
-- TABLE: audit_log
-- MNC-grade compliance audit logging
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS audit_log (
    log_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       VARCHAR(36),
    action        VARCHAR(100) NOT NULL,
    entity_type   VARCHAR(50),
    entity_id     VARCHAR(36),
    old_value     TEXT,
    new_value     TEXT,
    ip_address    VARCHAR(45),
    user_agent    VARCHAR(500),
    timestamp     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════
-- TABLE: feedback
-- User feedback on resolved complaints
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS feedback (
    feedback_id   VARCHAR(36) PRIMARY KEY,
    complaint_id  VARCHAR(36) NOT NULL UNIQUE,
    user_id       VARCHAR(36) NOT NULL,
    rating        TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment       TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    INDEX idx_feedback_complaint (complaint_id),
    INDEX idx_feedback_user (user_id),
    INDEX idx_feedback_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════
-- SEED DATA: Departments
-- ═══════════════════════════════════════════════════════════════
INSERT INTO departments (dept_name, dept_head, contact_email) VALUES
    ('Hostel Management',    'Mr. Rajan Kumar',   'hostel@college.edu'),
    ('Academic Affairs',     'Dr. Priya Sharma',  'academics@college.edu'),
    ('IT Department',        'Mr. Arjun Nair',    'it@college.edu'),
    ('Transport',            'Mr. Suresh Babu',   'transport@college.edu'),
    ('Canteen & Facilities', 'Ms. Lakshmi Devi',  'canteen@college.edu'),
    ('General Affairs',      'Mr. Vijay Kumar',   'general@college.edu');

-- ═══════════════════════════════════════════════════════════════
-- SEED DATA: SuperAdmin Account
-- Password: Admin@123 (BCrypt hashed)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO users (
    user_id, name, email, password_hash, role, dept_id, 
    roll_number, class_name, phone, profile_pic, is_active, 
    last_login, created_at, updated_at
) VALUES (
    'sa-001-superadmin-uuid',
    'Super Admin',
    'superadmin@resolvit.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4VmZqWQG4w7z4IWm',
    'SUPERADMIN',
    1,
    NULL,
    NULL,
    '9999999999',
    NULL,
    TRUE,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════════
-- SEED DATA: Sample Admin Account
-- Password: Admin@123 (BCrypt hashed)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO users (
    user_id, name, email, password_hash, role, dept_id, 
    roll_number, class_name, phone, profile_pic, is_active
) VALUES (
    'admin-001-uuid',
    'IT Admin',
    'itadmin@resolvit.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4VmZqWQG4w7z4IWm',
    'ADMIN',
    3,
    NULL,
    NULL,
    '9876543210',
    NULL,
    TRUE
);

-- ═══════════════════════════════════════════════════════════════
-- SEED DATA: Sample Student Account
-- Password: Student@123 (BCrypt hashed)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO users (
    user_id, name, email, password_hash, role, dept_id, 
    roll_number, class_name, phone, profile_pic, is_active
) VALUES (
    'student-001-uuid',
    'John Doe',
    'john.doe@college.edu',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'STUDENT',
    2,
    'CS2021001',
    'B.Tech CSE - 3rd Year',
    '9123456780',
    NULL,
    TRUE
);

-- ═══════════════════════════════════════════════════════════════
-- SEED DATA: Sample Complaints
-- ═══════════════════════════════════════════════════════════════
INSERT INTO complaints (
    complaint_id, user_id, title, description, category, priority, status
) VALUES 
(
    'cmp-001-uuid',
    'student-001-uuid',
    'Hostel WiFi Not Working in Block B',
    'The WiFi connection in Block B, Room 205 has been extremely slow for the past 3 days. Unable to attend online classes or submit assignments. Multiple students are affected.',
    'HOSTEL',
    'HIGH',
    'PENDING'
),
(
    'cmp-002-uuid',
    'student-001-uuid',
    'Canteen Food Quality Issue',
    'The food served in the main canteen has been of poor quality recently. Found insects in the dal curry twice this week. Please take immediate action.',
    'CANTEEN',
    'CRITICAL',
    'IN_PROGRESS'
),
(
    'cmp-003-uuid',
    'student-001-uuid',
    'Library AC Not Functioning',
    'The air conditioning in the main library reading hall is not working properly. It is very hot and uncomfortable to study.',
    'OTHER',
    'MEDIUM',
    'RESOLVED'
);

-- ═══════════════════════════════════════════════════════════════
-- SEED DATA: Sample Status History
-- ═══════════════════════════════════════════════════════════════
INSERT INTO status_history (complaint_id, old_status, new_status, changed_by, remarks) VALUES
('cmp-002-uuid', 'PENDING', 'IN_PROGRESS', 'admin-001-uuid', 'Assigned to canteen management for investigation'),
('cmp-003-uuid', 'PENDING', 'IN_PROGRESS', 'admin-001-uuid', 'Maintenance team notified'),
('cmp-003-uuid', 'IN_PROGRESS', 'RESOLVED', 'admin-001-uuid', 'AC repaired and functioning properly now');

-- ═══════════════════════════════════════════════════════════════
-- SEED DATA: Sample Notifications
-- ═══════════════════════════════════════════════════════════════
INSERT INTO notifications (notif_id, user_id, title, message, type, is_read, related_id) VALUES
(
    'notif-001-uuid',
    'student-001-uuid',
    'Welcome to ResolvIt!',
    'Welcome to ResolvIt - Your voice matters! Submit your first complaint and we will ensure it gets resolved.',
    'SYSTEM',
    FALSE,
    NULL
),
(
    'notif-002-uuid',
    'student-001-uuid',
    'Complaint Status Updated',
    'Your complaint "Canteen Food Quality Issue" has been moved to In Progress. Our team is working on it.',
    'STATUS_UPDATE',
    FALSE,
    'cmp-002-uuid'
),
(
    'notif-003-uuid',
    'student-001-uuid',
    'Complaint Resolved',
    'Great news! Your complaint "Library AC Not Functioning" has been resolved. Please rate our service.',
    'STATUS_UPDATE',
    FALSE,
    'cmp-003-uuid'
);

-- ═══════════════════════════════════════════════════════════════
-- SEED DATA: Sample Assignment
-- ═══════════════════════════════════════════════════════════════
INSERT INTO assignments (assignment_id, complaint_id, dept_id, assigned_by, remarks) VALUES
('assign-001-uuid', 'cmp-002-uuid', 5, 'admin-001-uuid', 'Please investigate the food quality issue and take corrective measures');

-- ═══════════════════════════════════════════════════════════════
-- VIEWS: Useful aggregate views
-- ═══════════════════════════════════════════════════════════════

-- View: Complaint summary with user info
CREATE OR REPLACE VIEW vw_complaints_summary AS
SELECT 
    c.complaint_id,
    c.title,
    c.description,
    c.category,
    c.priority,
    c.status,
    c.is_anonymous,
    c.view_count,
    c.created_at,
    c.updated_at,
    c.resolved_at,
    CASE WHEN c.is_anonymous THEN 'Anonymous' ELSE u.name END AS submitted_by_name,
    CASE WHEN c.is_anonymous THEN NULL ELSE u.email END AS submitted_by_email,
    u.user_id AS submitted_by_id,
    d.dept_name AS assigned_department,
    a.assigned_at
FROM complaints c
LEFT JOIN users u ON c.user_id = u.user_id
LEFT JOIN assignments a ON c.complaint_id = a.complaint_id
LEFT JOIN departments d ON a.dept_id = d.dept_id;

-- View: Complaint statistics by status
CREATE OR REPLACE VIEW vw_complaint_stats AS
SELECT 
    status,
    COUNT(*) AS count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM complaints), 2) AS percentage
FROM complaints
GROUP BY status;

-- View: Complaint statistics by category
CREATE OR REPLACE VIEW vw_category_stats AS
SELECT 
    category,
    COUNT(*) AS count,
    SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved_count,
    SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS pending_count
FROM complaints
GROUP BY category;

-- View: Monthly complaint trend
CREATE OR REPLACE VIEW vw_monthly_trend AS
SELECT 
    YEAR(created_at) AS year,
    MONTH(created_at) AS month,
    DATE_FORMAT(created_at, '%Y-%m') AS month_year,
    COUNT(*) AS total_complaints,
    SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved,
    SUM(CASE WHEN status IN ('PENDING', 'REOPENED') THEN 1 ELSE 0 END) AS pending
FROM complaints
GROUP BY YEAR(created_at), MONTH(created_at), DATE_FORMAT(created_at, '%Y-%m')
ORDER BY year DESC, month DESC;

-- ═══════════════════════════════════════════════════════════════
-- END OF SCHEMA
-- ═══════════════════════════════════════════════════════════════
