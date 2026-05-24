// In-memory database for demo purposes
const users = [
  {
    id: 1,
    email: 'student@resolvit.com',
    password: 'password123',
    name: 'John Student',
    role: 'student',
    department: 'Academic',
    phone: '+1-800-000-0003',
  },
  {
    id: 2,
    email: 'admin@resolvit.com',
    password: 'password123',
    name: 'Admin User',
    role: 'admin',
    department: 'Support',
    phone: '+1-800-000-0002',
  },
  {
    id: 3,
    email: 'superadmin@resolvit.com',
    password: 'password123',
    name: 'Super Admin',
    role: 'superadmin',
    department: 'Administration',
    phone: '+1-800-000-0001',
  },
]

const complaints = [
  {
    id: 1,
    complaint_id: 'CMP-001',
    user_id: 1,
    title: 'Canteen Quality Issues',
    description: 'The food quality in the canteen has been deteriorating. Need immediate action.',
    category: 'Canteen Services',
    priority: 'high',
    status: 'in_progress',
    assigned_to: 2,
    department: 'Food Services',
    created_at: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000),
    updated_at: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
  },
  {
    id: 2,
    complaint_id: 'CMP-002',
    user_id: 1,
    title: 'Library WiFi Connectivity',
    description: 'WiFi disconnection issues in the library, very frustrating during exam prep.',
    category: 'Library Services',
    priority: 'medium',
    status: 'open',
    assigned_to: null,
    department: 'IT',
    created_at: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
    updated_at: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
  },
  {
    id: 3,
    complaint_id: 'CMP-003',
    user_id: 1,
    title: 'Hostel Maintenance',
    description: 'Broken water tap in hostel room 305. Needs urgent repair.',
    category: 'Hostel Facilities',
    priority: 'critical',
    status: 'resolved',
    assigned_to: 2,
    department: 'Facilities',
    created_at: new Date(Date.now() - 14 * 24 * 60 * 60 * 1000),
    updated_at: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
  },
]

export function getUserByEmail(email: string) {
  return users.find((u) => u.email === email)
}

export function getAllComplaints() {
  return complaints
}

export function getComplaintsByUserId(userId: number) {
  return complaints.filter((c) => c.user_id === userId)
}

export function createComplaint(complaintData: any) {
  const newComplaint = {
    id: complaints.length + 1,
    complaint_id: `CMP-${String(complaints.length + 1).padStart(3, '0')}`,
    created_at: new Date(),
    updated_at: new Date(),
    assigned_to: null,
    ...complaintData,
  }
  complaints.push(newComplaint)
  return newComplaint
}


export function initializeDb() {
  const db = getDb()

  // Create tables
  db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY,
      email TEXT UNIQUE NOT NULL,
      password TEXT NOT NULL,
      name TEXT NOT NULL,
      role TEXT NOT NULL CHECK(role IN ('student', 'admin', 'superadmin')),
      department TEXT,
      phone TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS complaints (
      id INTEGER PRIMARY KEY,
      complaint_id TEXT UNIQUE NOT NULL,
      user_id INTEGER NOT NULL,
      title TEXT NOT NULL,
      description TEXT NOT NULL,
      category TEXT NOT NULL,
      priority TEXT NOT NULL CHECK(priority IN ('low', 'medium', 'high', 'critical')),
      status TEXT NOT NULL CHECK(status IN ('open', 'in_progress', 'resolved', 'rejected', 'closed')),
      assigned_to INTEGER,
      department TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY(user_id) REFERENCES users(id),
      FOREIGN KEY(assigned_to) REFERENCES users(id)
    );

    CREATE TABLE IF NOT EXISTS comments (
      id INTEGER PRIMARY KEY,
      complaint_id INTEGER NOT NULL,
      user_id INTEGER NOT NULL,
      comment TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY(complaint_id) REFERENCES complaints(id),
      FOREIGN KEY(user_id) REFERENCES users(id)
    );

    CREATE TABLE IF NOT EXISTS status_history (
      id INTEGER PRIMARY KEY,
      complaint_id INTEGER NOT NULL,
      old_status TEXT,
      new_status TEXT NOT NULL,
      changed_by INTEGER,
      reason TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY(complaint_id) REFERENCES complaints(id),
      FOREIGN KEY(changed_by) REFERENCES users(id)
    );

    CREATE TABLE IF NOT EXISTS audit_logs (
      id INTEGER PRIMARY KEY,
      user_id INTEGER,
      action TEXT NOT NULL,
      entity_type TEXT,
      entity_id INTEGER,
      details TEXT,
      ip_address TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY(user_id) REFERENCES users(id)
    );

    CREATE TABLE IF NOT EXISTS notifications (
      id INTEGER PRIMARY KEY,
      user_id INTEGER NOT NULL,
      title TEXT NOT NULL,
      message TEXT,
      type TEXT,
      related_complaint_id INTEGER,
      read BOOLEAN DEFAULT 0,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY(user_id) REFERENCES users(id),
      FOREIGN KEY(related_complaint_id) REFERENCES complaints(id)
    );

    CREATE TABLE IF NOT EXISTS feedbacks (
      id INTEGER PRIMARY KEY,
      complaint_id INTEGER NOT NULL,
      rating INTEGER CHECK(rating >= 1 AND rating <= 5),
      feedback TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY(complaint_id) REFERENCES complaints(id)
    );

    CREATE INDEX IF NOT EXISTS idx_complaints_user_id ON complaints(user_id);
    CREATE INDEX IF NOT EXISTS idx_complaints_status ON complaints(status);
    CREATE INDEX IF NOT EXISTS idx_complaints_created_at ON complaints(created_at DESC);
    CREATE INDEX IF NOT EXISTS idx_comments_complaint_id ON comments(complaint_id);
    CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
    CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
  `)

  // Seed initial data
  const stmt = db.prepare('SELECT COUNT(*) as count FROM users')
  const result = stmt.get() as { count: number }

  if (result.count === 0) {
    const insertUser = db.prepare(`
      INSERT INTO users (email, password, name, role, department, phone)
      VALUES (?, ?, ?, ?, ?, ?)
    `)

    // Default users with bcrypt hashed passwords
    insertUser.run('superadmin@resolvit.com', '$2a$10$7gkN8kZIkQbzVXJg5QkUQeZVZZzZZZZZZZZZZZZZZZZZZZZZZZZZZZ', 'Super Admin', 'superadmin', 'Administration', '+1-800-000-0001')
    insertUser.run('admin@resolvit.com', '$2a$10$7gkN8kZIkQbzVXJg5QkUQeZVZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ', 'Admin User', 'admin', 'Support', '+1-800-000-0002')
    insertUser.run('student@resolvit.com', '$2a$10$7gkN8kZIkQbzVXJg5QkUQeZVZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ', 'John Student', 'student', 'Academic', '+1-800-000-0003')
  }

  db.close()
}
