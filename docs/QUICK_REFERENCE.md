# ResolvIt - Quick Reference Guide

## Quick Start (5 Minutes)

### 1. Prerequisites
```bash
# Check Java installation
java -version          # Should be 17 or higher

# Check Maven installation
mvn -version           # Should be 3.8.0 or higher

# Check MySQL status
mysql --version        # Should be 8.0 or higher
```

### 2. Database Setup
```bash
# Ensure MySQL is running
# Windows: net start MySQL80
# macOS: mysql.server start
# Linux: sudo service mysql start

# Create database
mysql -u root -p < database/schema.sql
# Enter MySQL root password when prompted
```

### 3. Build & Run
```bash
# Build entire project
mvn clean install -DskipTests

# Run the application
cd resolvit-ui
mvn javafx:run
```

### 4. Login with Test Credentials
- **Email**: superadmin@resolvit.com
- **Password**: Admin@123

---

## Key Files Location

| What | Where |
|------|-------|
| Main Application | `resolvit-ui/src/main/java/com/resolvit/ui/App.java` |
| Database Schema | `database/schema.sql` |
| Sample Data | `database/seed_data.sql` |
| UI Layouts | `resolvit-ui/src/main/resources/fxml/*.fxml` |
| Stylesheets | `resolvit-ui/src/main/resources/css/*.css` |
| Services | `resolvit-service/src/main/java/.../service/impl/` |
| DAOs | `resolvit-dao/src/main/java/.../dao/impl/` |
| Entities | `resolvit-core/src/main/java/.../core/entities/` |

---

## Common Commands

```bash
# Build project
mvn clean install

# Skip tests (faster)
mvn clean install -DskipTests

# Run tests only
mvn test

# Run application
cd resolvit-ui && mvn javafx:run

# Generate documentation
mvn javadoc:javadoc

# Check dependencies
mvn dependency:tree

# Build specific module
mvn clean install -pl resolvit-ui

# Run with production profile
mvn clean install -Pprod
```

---

## Module Overview

```
┌─────────────────────────┐
│   resolvit-ui           │  ← Start here (UI)
│   (JavaFX, Controllers) │
└─────────────────────────┘
          ↓
┌─────────────────────────┐
│  resolvit-service       │  ← Business logic
│  (Services, Validators) │
└─────────────────────────┘
          ↓
┌─────────────────────────┐
│   resolvit-dao          │  ← Database access
│   (DAOs, Connection)    │
└─────────────────────────┘
          ↓
┌─────────────────────────┐
│  resolvit-core          │  ← Domain objects
│  (Entities, Enums)      │
└─────────────────────────┘
```

---

## User Roles & Permissions

### Student
- Submit complaints (anonymous or public)
- Track own complaints
- Leave feedback

### Admin
- View assigned complaints
- Update complaint status
- Assign to departments
- Generate reports

### SuperAdmin
- View all complaints
- Manage all users & departments
- System analytics
- Full audit logs

---

## Database Connection Details

**Default Configuration**:
- **Host**: localhost
- **Port**: 3306
- **Database**: resolvit_db
- **User**: root
- **Password**: root (change in production)

**Location**: `resolvit-dao/src/main/java/com/resolvit/dao/connection/DatabaseConnection.java`

---

## Feature Checklist

✅ Multi-role authentication (Student, Admin, SuperAdmin)
✅ Complaint management (CRUD operations)
✅ Status tracking with history
✅ Department-based assignment
✅ Real-time notifications
✅ PDF report generation
✅ Dark/Light theme toggle
✅ Audit logging (MNC-grade)
✅ Anonymous complaint submission
✅ File attachment support
✅ Search & filtering
✅ Analytics dashboard
✅ Feedback collection
✅ Email notifications (ready for integration)

---

## Architecture Pattern

**MVC + Layered + DAO**

```
UI (FXML) → Controller → Service → DAO → Database
```

**Design Patterns**:
- Singleton (Database, Session)
- Builder (Complaint creation)
- Factory (Service creation)
- Observer (Notifications)
- Strategy (Categorization)

---

## Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=ComplaintServiceTest

# Run with coverage
mvn test jacoco:report

# View report: target/site/jacoco/index.html
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| MySQL connection failed | Start MySQL service |
| Cannot find symbol | Run `mvn clean install` |
| Port 3306 in use | Kill process or change port |
| JavaFX not found | Use `mvn javafx:run` |
| Tests failing | Check database connection |
| IDE not recognizing modules | Reload Maven projects |

---

## Important Classes

### Service Layer
- `UserService` - User management
- `ComplaintService` - Complaint operations
- `NotificationService` - Notifications
- `AuthenticationService` - Login/logout
- `AuditService` - Logging

### DAO Layer
- `UserDAO` - User persistence
- `ComplaintDAO` - Complaint persistence
- `NotificationDAO` - Notification persistence
- `AuditLogDAO` - Audit log persistence

### Entities
- `User` (abstract) → `Student`, `Admin`, `SuperAdmin`
- `Complaint` (with Builder)
- `Department`, `Assignment`, `Notification`
- `AuditLog`, `StatusHistory`, `Feedback`

### Enums
- `Role` - User roles
- `Status` - Complaint status
- `Priority` - Priority levels
- `Category` - Complaint categories
- `NotificationType` - Notification types

---

## Configuration Files

### pom.xml
- Maven build configuration
- Dependency management
- Plugin configuration
- Profiles (dev, prod)

### Database
- `schema.sql` - Complete schema
- `seed_data.sql` - Sample data
- `migrations/` - Version control

### UI Resources
- `fxml/` - FXML layouts
- `css/` - Stylesheets
- `assets/` - Images & icons

---

## Security Notes

✅ BCrypt password hashing (cost factor: 12)
✅ Session-based authentication
✅ SQL injection prevention
✅ Input validation & sanitization
✅ Role-based access control
✅ Audit logging for all actions
✅ Connection pooling (prevent exhaustion)

---

## Performance

- **Connection Pool**: 10 reusable connections
- **Caching**: Session data, user profiles
- **Database Indexes**: On frequently queried columns
- **Prepared Statements**: Prevent SQL injection & optimize queries

---

## Default Accounts (After Schema Import)

| Role | Email | Password |
|------|-------|----------|
| SuperAdmin | superadmin@resolvit.com | Admin@123 |
| Admin | itadmin@resolvit.com | Admin@123 |
| Student | john.doe@college.edu | Student@123 |

⚠️ Change credentials immediately in production!

---

## Directory Structure Summary

```
resolvit/
├── README.md                          ← Start here!
├── pom.xml                           ← Maven config
├── database/
│   ├── schema.sql                    ← Database schema
│   ├── seed_data.sql                 ← Sample data
│   └── migrations/
├── resolvit-core/                    ← Entities & Enums
├── resolvit-dao/                     ← Database access
├── resolvit-service/                 ← Business logic
├── resolvit-ui/                      ← User interface
│   └── src/main/resources/
│       ├── fxml/                     ← Layouts
│       └── css/                      ← Styles
├── resolvit-tests/                   ← Tests
├── docs/
│   ├── ARCHITECTURE.md               ← Design details
│   ├── DEVELOPER_SETUP.md            ← Setup guide
│   └── QUICK_REFERENCE.md            ← This file
└── .github/
    └── workflows/
        └── ci.yml                    ← CI/CD pipeline
```

---

## Getting Help

1. **Documentation**: Check `docs/` folder
2. **Architecture**: Read `docs/ARCHITECTURE.md`
3. **Setup Issues**: See `docs/DEVELOPER_SETUP.md`
4. **FAQ**: Common issues section above
5. **Contact**: Reach out to development team

---

## Next Steps

1. ✅ Install prerequisites
2. ✅ Setup database
3. ✅ Build project
4. ✅ Run application
5. ✅ Login with test account
6. ✅ Explore features
7. ✅ Review code structure
8. ✅ Read architecture documentation
9. ✅ Start development

---

**Version**: 1.0.0 | **Status**: Production Ready ✅
**Last Updated**: May 2024

For detailed information, refer to the complete README.md
