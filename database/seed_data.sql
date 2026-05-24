-- ═══════════════════════════════════════════════════════════════
-- ResolvIt - Seed Data v1.0.0
-- Additional sample data for development and testing
-- ═══════════════════════════════════════════════════════════════

USE resolvit_db;

-- ═══════════════════════════════════════════════════════════════
-- Additional Sample Students
-- ═══════════════════════════════════════════════════════════════

INSERT INTO users (
    user_id, name, email, password_hash, role, dept_id, 
    roll_number, class_name, phone, is_active
) VALUES 
(
    'student-002-uuid',
    'Sarah Johnson',
    'sarah.johnson@college.edu',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'STUDENT',
    2,
    'CS2021002',
    'B.Tech CSE - 3rd Year',
    '9123456781',
    TRUE
),
(
    'student-003-uuid',
    'Rahul Sharma',
    'rahul.sharma@college.edu',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'STUDENT',
    1,
    'CS2021003',
    'B.Tech CSE - 3rd Year',
    '9123456782',
    TRUE
),
(
    'student-004-uuid',
    'Priya Verma',
    'priya.verma@college.edu',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'STUDENT',
    3,
    'EC2021001',
    'B.Tech ECE - 2nd Year',
    '9123456783',
    TRUE
),
(
    'student-005-uuid',
    'Amit Kumar',
    'amit.kumar@college.edu',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'STUDENT',
    4,
    'ME2021001',
    'B.Tech ME - 4th Year',
    '9123456784',
    TRUE
);

-- ═══════════════════════════════════════════════════════════════
-- Additional Sample Admins
-- ═══════════════════════════════════════════════════════════════

INSERT INTO users (
    user_id, name, email, password_hash, role, dept_id, 
    phone, is_active
) VALUES 
(
    'admin-002-uuid',
    'Hostel Admin',
    'hosteladmin@resolvit.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4VmZqWQG4w7z4IWm',
    'ADMIN',
    1,
    '9876543211',
    TRUE
),
(
    'admin-003-uuid',
    'Academic Admin',
    'academicadmin@resolvit.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4VmZqWQG4w7z4IWm',
    'ADMIN',
    2,
    '9876543212',
    TRUE
),
(
    'admin-004-uuid',
    'Transport Admin',
    'transportadmin@resolvit.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4VmZqWQG4w7z4IWm',
    'ADMIN',
    4,
    '9876543213',
    TRUE
),
(
    'admin-005-uuid',
    'Canteen Admin',
    'canteenmgmt@resolvit.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4VmZqWQG4w7z4IWm',
    'ADMIN',
    5,
    '9876543214',
    TRUE
);

-- ═══════════════════════════════════════════════════════════════
-- Additional Sample Complaints
-- ═══════════════════════════════════════════════════════════════

INSERT INTO complaints (
    complaint_id, user_id, title, description, category, priority, status
) VALUES 
(
    'cmp-004-uuid',
    'student-002-uuid',
    'Bus Service Delay',
    'The morning bus service from Gate 2 has been delayed by 30+ minutes for 3 consecutive days. This is causing students to miss classes.',
    'TRANSPORT',
    'HIGH',
    'IN_PROGRESS'
),
(
    'cmp-005-uuid',
    'student-003-uuid',
    'Laboratory Equipment Shortage',
    'The computer lab has insufficient systems for all students. Many have to share one desktop during practical sessions.',
    'ACADEMICS',
    'MEDIUM',
    'PENDING'
),
(
    'cmp-006-uuid',
    'student-004-uuid',
    'Electrical Issues in Hostel Block A',
    'Power fluctuations causing frequent blackouts. Electronics are getting damaged. Need immediate inspection.',
    'HOSTEL',
    'CRITICAL',
    'RESOLVED'
),
(
    'cmp-007-uuid',
    'student-005-uuid',
    'Water Supply Problem',
    'Water supply is irregular in the hostel. No water during morning hours for the past week.',
    'HOSTEL',
    'CRITICAL',
    'IN_PROGRESS'
),
(
    'cmp-008-uuid',
    'student-001-uuid',
    'Printing Services Not Available',
    'The central printing service is down. Unable to print assignments. Emergency situation.',
    'IT',
    'HIGH',
    'PENDING'
),
(
    'cmp-009-uuid',
    'student-002-uuid',
    'Unfair Grading in Midterm',
    'The grading for the midterm exam seems unfair. Please review the answer sheets.',
    'ACADEMICS',
    'MEDIUM',
    'REOPENED'
),
(
    'cmp-010-uuid',
    'student-003-uuid',
    'Lack of Vegetarian Options',
    'The canteen rarely serves vegetarian meals. Only available on specific days.',
    'CANTEEN',
    'LOW',
    'PENDING'
);

-- ═══════════════════════════════════════════════════════════════
-- Additional Status History Records
-- ═══════════════════════════════════════════════════════════════

INSERT INTO status_history (complaint_id, old_status, new_status, changed_by, remarks) VALUES
('cmp-004-uuid', 'PENDING', 'IN_PROGRESS', 'admin-004-uuid', 'Transport department investigating bus delay'),
('cmp-005-uuid', 'PENDING', 'PENDING', 'admin-003-uuid', 'Awaiting budget approval for additional systems'),
('cmp-006-uuid', 'PENDING', 'IN_PROGRESS', 'admin-001-uuid', 'Electrician notified for inspection'),
('cmp-006-uuid', 'IN_PROGRESS', 'RESOLVED', 'admin-001-uuid', 'Power supply stabilized after repairs'),
('cmp-007-uuid', 'PENDING', 'IN_PROGRESS', 'admin-002-uuid', 'Water tank being inspected and repaired'),
('cmp-008-uuid', 'PENDING', 'PENDING', 'admin-001-uuid', 'IT team investigating server issues'),
('cmp-009-uuid', 'PENDING', 'IN_PROGRESS', 'admin-003-uuid', 'Submitted for academic review'),
('cmp-009-uuid', 'IN_PROGRESS', 'REOPENED', 'admin-003-uuid', 'Discrepancy found in grading, re-evaluation underway'),
('cmp-010-uuid', 'PENDING', 'PENDING', 'admin-005-uuid', 'Canteen menu planning in progress');

-- ═══════════════════════════════════════════════════════════════
-- Additional Assignments
-- ═══════════════════════════════════════════════════════════════

INSERT INTO assignments (assignment_id, complaint_id, dept_id, assigned_by, remarks) VALUES
('assign-002-uuid', 'cmp-004-uuid', 4, 'sa-001-superadmin-uuid', 'Urgent: Check bus schedule and driver availability'),
('assign-003-uuid', 'cmp-005-uuid', 2, 'sa-001-superadmin-uuid', 'Plan: Procure 5 additional desktop systems'),
('assign-004-uuid', 'cmp-007-uuid', 1, 'sa-001-superadmin-uuid', 'Check water tank capacity and pump functionality'),
('assign-005-uuid', 'cmp-008-uuid', 3, 'sa-001-superadmin-uuid', 'Server troubleshooting and network diagnostics'),
('assign-006-uuid', 'cmp-009-uuid', 2, 'sa-001-superadmin-uuid', 'Academic integrity review panel');

-- ═══════════════════════════════════════════════════════════════
-- Additional Notifications
-- ═══════════════════════════════════════════════════════════════

INSERT INTO notifications (notif_id, user_id, title, message, type, is_read, related_id) VALUES
(
    'notif-004-uuid',
    'student-002-uuid',
    'New Complaint Submitted',
    'Your complaint about bus service has been submitted successfully. You can track it using ID: cmp-004-uuid',
    'SYSTEM',
    TRUE,
    'cmp-004-uuid'
),
(
    'notif-005-uuid',
    'student-003-uuid',
    'Complaint Assigned',
    'Your complaint has been assigned to the Academic Department for review.',
    'ASSIGNMENT',
    FALSE,
    'cmp-005-uuid'
),
(
    'notif-006-uuid',
    'student-004-uuid',
    'Complaint Resolved',
    'Great news! Your complaint "Electrical Issues in Hostel Block A" has been resolved.',
    'STATUS_UPDATE',
    FALSE,
    'cmp-006-uuid'
),
(
    'notif-007-uuid',
    'student-005-uuid',
    'Update on Your Complaint',
    'Your complaint is being worked on. Expected resolution: within 48 hours.',
    'REMINDER',
    TRUE,
    'cmp-007-uuid'
);

-- ═══════════════════════════════════════════════════════════════
-- Sample Feedback Records
-- ═══════════════════════════════════════════════════════════════

INSERT INTO feedback (feedback_id, complaint_id, user_id, rating, comment) VALUES
(
    'feedback-001-uuid',
    'cmp-003-uuid',
    'student-001-uuid',
    5,
    'Excellent service! The AC was repaired quickly and works perfectly now. Very satisfied with the response.'
),
(
    'feedback-002-uuid',
    'cmp-006-uuid',
    'student-004-uuid',
    4,
    'Good resolution. The electrical work was done professionally. Could have been faster though.'
);

-- ═══════════════════════════════════════════════════════════════
-- Sample Audit Log Records
-- ═══════════════════════════════════════════════════════════════

INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_value, new_value, ip_address, user_agent) VALUES
(
    'student-001-uuid',
    'CREATE_COMPLAINT',
    'COMPLAINT',
    'cmp-001-uuid',
    NULL,
    'Hostel WiFi complaint created',
    '192.168.1.100',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'
),
(
    'admin-001-uuid',
    'UPDATE_STATUS',
    'COMPLAINT',
    'cmp-002-uuid',
    'PENDING',
    'IN_PROGRESS',
    '192.168.1.101',
    'Mozilla/5.0 (X11; Linux x86_64)'
),
(
    'sa-001-superadmin-uuid',
    'CREATE_USER',
    'USER',
    'admin-002-uuid',
    NULL,
    'Hostel Admin user created',
    '192.168.1.102',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)'
),
(
    'admin-002-uuid',
    'CREATE_ASSIGNMENT',
    'ASSIGNMENT',
    'assign-002-uuid',
    NULL,
    'Complaint assigned to Transport Dept',
    '192.168.1.103',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'
);

-- ═══════════════════════════════════════════════════════════════
-- END OF SEED DATA
-- Total: 5 students, 4 admins, 7 complaints, 9 assignments,
--        7 notifications, 2 feedback, 4 audit logs
-- ═══════════════════════════════════════════════════════════════
