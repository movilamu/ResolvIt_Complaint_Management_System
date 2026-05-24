-- ═══════════════════════════════════════════════════════════════
-- Migration V2: Add Audit Logging Extensions
-- Version: 1.0.1
-- Description: Adds additional audit logging and compliance features
-- ═══════════════════════════════════════════════════════════════

USE resolvit_db;

-- ═══════════════════════════════════════════════════════════════
-- Add compliance-specific columns to audit_log
-- ═══════════════════════════════════════════════════════════════

ALTER TABLE audit_log
ADD COLUMN IF NOT EXISTS source_system VARCHAR(100) DEFAULT 'WEB' AFTER user_agent,
ADD COLUMN IF NOT EXISTS is_sensitive BOOLEAN DEFAULT FALSE AFTER source_system,
ADD COLUMN IF NOT EXISTS retention_days INT DEFAULT 2555 AFTER is_sensitive;

-- ═══════════════════════════════════════════════════════════════
-- Add status_change_reason to complaints table for detailed tracking
-- ═══════════════════════════════════════════════════════════════

ALTER TABLE complaints
ADD COLUMN IF NOT EXISTS status_change_reason TEXT AFTER resolved_at,
ADD COLUMN IF NOT EXISTS rejected_reason TEXT AFTER status_change_reason,
ADD COLUMN IF NOT EXISTS internal_notes TEXT AFTER rejected_reason;

-- ═══════════════════════════════════════════════════════════════
-- Create index on audit_log for compliance queries
-- ═══════════════════════════════════════════════════════════════

CREATE INDEX idx_audit_sensitive ON audit_log(is_sensitive, timestamp);
CREATE INDEX idx_audit_retention ON audit_log(retention_days, timestamp);

-- ═══════════════════════════════════════════════════════════════
-- Create audit trail view for compliance reporting
-- ═══════════════════════════════════════════════════════════════

CREATE OR REPLACE VIEW vw_compliance_audit AS
SELECT 
    log_id,
    user_id,
    COALESCE(u.name, 'System') AS performed_by,
    action,
    entity_type,
    entity_id,
    old_value,
    new_value,
    ip_address,
    source_system,
    is_sensitive,
    timestamp,
    DATEDIFF(CURDATE(), DATE(timestamp)) AS days_since_action
FROM audit_log al
LEFT JOIN users u ON al.user_id = u.user_id
ORDER BY al.timestamp DESC;

-- ═══════════════════════════════════════════════════════════════
-- Create resolution time tracking view
-- ═══════════════════════════════════════════════════════════════

CREATE OR REPLACE VIEW vw_resolution_metrics AS
SELECT 
    c.complaint_id,
    c.category,
    c.priority,
    c.status,
    DATEDIFF(COALESCE(c.resolved_at, NOW()), c.created_at) AS resolution_days,
    HOUR(TIMEDIFF(COALESCE(c.resolved_at, NOW()), c.created_at)) AS resolution_hours,
    CASE 
        WHEN DATEDIFF(COALESCE(c.resolved_at, NOW()), c.created_at) <= 1 THEN 'Under 24 hours'
        WHEN DATEDIFF(COALESCE(c.resolved_at, NOW()), c.created_at) <= 7 THEN 'Under 1 week'
        WHEN DATEDIFF(COALESCE(c.resolved_at, NOW()), c.created_at) <= 30 THEN 'Under 1 month'
        ELSE 'Over 1 month'
    END AS resolution_category,
    c.created_at,
    c.resolved_at
FROM complaints c
WHERE c.status IN ('RESOLVED', 'REJECTED');

-- ═══════════════════════════════════════════════════════════════
-- Migration completed successfully
-- ═══════════════════════════════════════════════════════════════
