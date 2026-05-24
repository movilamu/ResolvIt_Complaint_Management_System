# ResolvIt - Complaint Management System

> **Every complaint heard. Every issue resolved.**

A production-grade, enterprise-ready complaint management system built with **Java 17**, **JavaFX 21**, **MySQL 8.0**, and **Maven**. Designed for educational institutions to streamline complaint handling and resolution tracking.

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Setup Instructions](#setup-instructions)
- [Project Structure](#project-structure)
- [Features](#features)
- [Database Schema](#database-schema)
- [User Roles](#user-roles)
- [API & Service Layer](#api--service-layer)
- [Security](#security)
- [Testing](#testing)
- [Deployment](#deployment)
- [Credits](#credits)

---

## 🎯 Project Overview

**ResolvIt** is a comprehensive complaint management system that bridges the gap between students and administration. It enables:

- **Students** to submit complaints anonymously or publicly
- **Admins** to track, assign, and resolve complaints efficiently
- **SuperAdmins** to monitor system-wide metrics and analytics
- **Real-time notifications** for status updates
- **Audit logging** for compliance and MNC-grade accountability
- **PDF report generation** for analytics and trends

**Version**: 1.0.0  
**Java Version**: 17 (LTS)  
**Build Tool**: Maven (Multi-module)  
**UI Framework**: JavaFX 21 with FXML  
**Database**: MySQL 8.0

---

## 🏗️ Architecture

ResolvIt follows **MVC (Model-View-Controller)** with additional layers:

```
┌─────────────────────────────────────────────────────────┐
│                  JavaFX UI Layer                         │
│         (Controllers, FXML, Animations, Themes)         │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│              Service Layer (Business Logic)             │
│  (UserService, ComplaintService, NotificationService)  │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│           DAO Layer (Data Access Objects)               │
│  (UserDAO, ComplaintDAO, NotificationDAO, etc.)        │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│       Database Layer (MySQL with Connection Pool)       │
│              (Singleton Pattern, 10 Connections)        │
└─────────────────────────────────────────────────────────┘
```

**Design Patterns Used**:
- **MVC** - Separation of concerns
- **Singleton** - DatabaseConnection, SessionManager
- **Builder** - Complaint entity construction
- **Factory** - ServiceFactory for service instantiation
- **Observer** - Notification system
- **Strategy** - Complaint categorization and priority handling
- **DAO** - Database abstraction

---

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Java** | OpenJDK/Oracle JDK | 17 LTS |
| **UI Framework** | JavaFX | 21 |
| **Build Tool** | Maven | 3.8+ |
| **Database** | MySQL | 8.0.33+ |
| **Security** | BCrypt | 0.10.2 |
| **PDF Generation** | iText | 5.5.13.3 |
| **Logging** | SLF4J + Logback | 2.0.9 / 1.4.11 |
| **Testing** | JUnit 5 + Mockito | 5.10.0 / 5.5.0 |

---

## 🚀 Setup Instructions

### Prerequisites

- **Java 17+** installed and in PATH
- **Maven 3.8+** installed
- **MySQL 8.0+** server running
- **Git** (optional, for version control)

### Step 1: Clone/Extract the Project

```bash
git clone https://github.com/your-org/resolvit.git
cd resolvit
```

Or extract the provided ZIP file.

### Step 2: Setup MySQL Database

1. **Start MySQL Server**:
   ```bash
   # Linux/Mac
   mysql.server start
   
   # Windows
   mysqld
   ```

2. **Create Database and Run Schema**:
   ```bash
   mysql -u root -p < database/schema.sql
   ```

   When prompted, enter your MySQL root password.

   **Note**: If you want to use a different user/password, edit `resolvit-dao/src/main/java/com/resolvit/dao/connection/DatabaseConnection.java` and update the credentials.

### Step 3: Build the Project

```bash
# Full build with tests
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl resolvit-ui
```

### Step 4: Run the Application

**Option A: From Maven**
```bash
cd resolvit-ui
mvn javafx:run
```

**Option B: Using JAR**
```bash
java -jar resolvit-ui/target/resolvit-ui-1.0.0.jar
```

**Option C: IDE (IntelliJ IDEA)**
1. Open project in IntelliJ
2. Set Project SDK to Java 17
3. Right-click on `App.java` → Run

---

## 📁 Project Structure

```
resolvit/                          (Root)
│
├── pom.xml                        (Parent POM - Multi-module)
│
├── resolvit-core/                 (Domain & Entities)
│   ├── pom.xml
│   └── src/main/java/com/resolvit/core/
│       ├── entities/              (User, Complaint, Department, etc.)
│       ├── enums/                 (Role, Status, Priority, Category)
│       └── exceptions/            (Custom exceptions)
│
├── resolvit-dao/                  (Data Access Layer)
│   ├── pom.xml
│   └── src/main/java/com/resolvit/dao/
│       ├── interfaces/            (DAO interfaces)
│       ├── impl/                  (DAO implementations)
│       └── connection/            (Database connection & pool)
│
├── resolvit-service/              (Business Logic Layer)
│   ├── pom.xml
│   └── src/main/java/com/resolvit/service/
│       ├── interfaces/            (Service interfaces)
│       ├── impl/                  (Service implementations)
│       ├── validators/            (Input validation)
│       ├── factory/               (ServiceFactory)
│       └── session/               (SessionManager)
│
├── resolvit-ui/                   (JavaFX UI Layer)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/resolvit/ui/
│       │   ├── App.java           (Entry point)
│       │   ├── controllers/       (FXML controllers)
│       │   ├── utils/             (Helpers: PDFGenerator, FileUploadHelper, etc.)
│       │   └── animations/        (Transition & animation effects)
│       └── resources/
│           ├── fxml/              (FXML layout files)
│           ├── css/               (Stylesheets: light, dark, components)
│           └── assets/            (Images, icons, logos)
│
├── resolvit-tests/                (Unit & Integration Tests)
│   ├── pom.xml
│   └── src/test/java/com/resolvit/
│       ├── unit/
│       └── integration/
│
├── database/                      (Database files)
│   ├── schema.sql                 (Complete schema with seed data)
│   ├── migrations/
│   │   ├── V1__initial_schema.sql
│   │   └── V2__add_audit_log.sql
│   └── seed_data.sql
│
├── docs/                          (Documentation)
│   ├── architecture_diagram.png
│   ├── er_diagram.png
│   ├── class_diagram.png
│   └── user_manual.pdf
│
├── .github/
│   └── workflows/
│       └── ci.yml                 (GitHub Actions CI/CD)
│
└── README.md
```

---

## ✨ Features

### User Management
- ✅ Multi-role authentication (Student, Admin, SuperAdmin)
- ✅ Secure password hashing with BCrypt
- ✅ Session management with timeout
- ✅ User profile management
- ✅ Department-based access control

### Complaint Management
- ✅ Create, read, update, delete complaints
- ✅ 6 categories: Hostel, Academics, IT, Transport, Canteen, Other
- ✅ 4 priority levels: Low, Medium, High, Critical
- ✅ 5 status types: Pending, In Progress, Resolved, Rejected, Reopened
- ✅ Anonymous complaint submission
- ✅ File attachment support
- ✅ View count tracking
- ✅ Full-text search capability

### Tracking & Analytics
- ✅ Real-time complaint status tracking
- ✅ Dashboard analytics for SuperAdmin
- ✅ Complaint statistics by category, priority, status
- ✅ Monthly trend analysis
- ✅ Department-wise performance metrics
- ✅ Average resolution time calculation

### Notifications
- ✅ Real-time status update notifications
- ✅ Assignment notifications
- ✅ System reminders
- ✅ Read/unread status tracking
- ✅ In-app notification center

### Audit & Compliance
- ✅ Complete audit log for all actions
- ✅ IP address & user agent tracking
- ✅ Status change history with remarks
- ✅ MNC-grade compliance logging
- ✅ Report generation (PDF)

### UI/UX
- ✅ Dark/Light theme toggle
- ✅ Material Design inspired components
- ✅ Smooth animations and transitions
- ✅ Responsive layouts
- ✅ Intuitive navigation
- ✅ Professional styling

---

## 🗄️ Database Schema

### Core Tables

| Table | Purpose |
|-------|---------|
| `users` | All system users (Students, Admins, SuperAdmins) |
| `complaints` | Main complaint records |
| `departments` | Department information for routing |
| `assignments` | Complaint assignments to departments |
| `status_history` | Audit trail of status changes |
| `notifications` | User notifications |
| `audit_log` | System-wide audit logging |
| `feedback` | User feedback on resolved complaints |

### Key Views

- `vw_complaints_summary` - Complaint details with user and assignment info
- `vw_complaint_stats` - Complaint statistics by status
- `vw_category_stats` - Statistics grouped by category
- `vw_monthly_trend` - Monthly complaint trends

See `database/schema.sql` for complete details.

---

## 👥 User Roles

### Student (Role: STUDENT)
- Submit complaints (anonymous or public)
- Track complaint status
- View personal complaint history
- Leave feedback on resolved complaints
- Receive notifications
- Upload attachments

### Admin (Role: ADMIN)
- View assigned complaints
- Update complaint status
- Assign complaints to departments
- Add remarks/comments
- Generate reports
- Manage users in their department

### SuperAdmin (Role: SUPERADMIN)
- Full system access
- View all complaints and users
- Create/manage departments
- System-wide analytics and dashboards
- Generate comprehensive reports
- Configure system settings
- Access audit logs

---

## 🔌 API & Service Layer

### Authentication Service
```java
AuthenticationService authService = ServiceFactory.getAuthenticationService();
User user = authService.login(email, password);
authService.logout(userId);
boolean valid = authService.validateToken(token);
```

### Complaint Service
```java
ComplaintService complaintService = ServiceFactory.getComplaintService();
Complaint complaint = complaintService.createComplaint(complaint);
List<Complaint> complaints = complaintService.getAllComplaints();
complaintService.updateStatus(complaintId, newStatus, remarks);
List<Complaint> filtered = complaintService.filterComplaints(category, priority, status);
```

### Notification Service
```java
NotificationService notificationService = ServiceFactory.getNotificationService();
List<Notification> unread = notificationService.getUnreadNotifications(userId);
notificationService.markAsRead(notificationId);
notificationService.sendNotification(notification);
```

### User Service
```java
UserService userService = ServiceFactory.getUserService();
User user = userService.getUserById(userId);
List<User> users = userService.getAllUsers();
userService.updateUserProfile(user);
```

### Audit Service
```java
AuditService auditService = ServiceFactory.getAuditService();
auditService.logAction(userId, action, entityType, entityId, oldValue, newValue);
List<AuditLog> logs = auditService.getAuditLogs(filters);
```

---

## 🔒 Security

### Authentication
- ✅ BCrypt password hashing (cost factor: 12)
- ✅ Session-based authentication
- ✅ Automatic session timeout (configurable)
- ✅ Secure password validation

### Authorization
- ✅ Role-based access control (RBAC)
- ✅ Department-based access filtering
- ✅ Method-level security checks

### Data Protection
- ✅ SQL injection prevention (prepared statements)
- ✅ Input validation and sanitization
- ✅ UTF-8 encoding throughout
- ✅ Secure file upload handling

### Audit & Compliance
- ✅ Complete audit logging
- ✅ IP address tracking
- ✅ User agent logging
- ✅ Immutable audit records

---

## 🧪 Testing

### Unit Tests
Located in `resolvit-tests/src/test/java/com/resolvit/unit/`

```bash
mvn test -Dtest=ComplaintServiceTest
mvn test -Dtest=UserValidatorTest
mvn test -Dtest=StatusTransitionTest
```

### Integration Tests
Located in `resolvit-tests/src/test/java/com/resolvit/integration/`

```bash
mvn test -Dtest=ComplaintDAOTest
mvn test -Dtest=UserDAOTest
```

### Run All Tests
```bash
mvn test
```

### Generate Test Report
```bash
mvn test site:site
# Open target/site/index.html in browser
```

---

## 📦 Deployment

### Building for Production

1. **Set production profile**:
   ```bash
   mvn clean install -Pprod
   ```

2. **Create standalone JAR**:
   ```bash
   mvn clean install -DskipTests
   cd resolvit-ui
   mvn package -DskipTests
   ```

3. **Run the JAR**:
   ```bash
   java -Xmx1G -jar resolvit-ui-1.0.0.jar
   ```

### Docker Deployment (Optional)

Create a `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
COPY resolvit-ui/target/resolvit-ui-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t resolvit:1.0.0 .
docker run -p 8080:8080 resolvit:1.0.0
```

### Database Deployment

1. Backup current database:
   ```bash
   mysqldump -u root -p resolvit_db > backup_$(date +%Y%m%d).sql
   ```

2. Deploy schema and migrations:
   ```bash
   mysql -u root -p resolvit_db < database/schema.sql
   mysql -u root -p resolvit_db < database/migrations/V2__add_audit_log.sql
   ```

---

## 📋 License

Proprietary - All Rights Reserved

---

## 👤 Credits

**Development Team**:
- Architecture & Design
- Full-stack Development
- Database Design
- UI/UX Implementation
- Testing & QA

**Technologies Used**:
- Java 17 (OpenJDK)
- JavaFX 21 - UI Framework
- MySQL 8.0 - Database
- Maven - Build Tool
- SLF4J + Logback - Logging
- BCrypt - Password Hashing
- iText - PDF Generation
- JUnit 5 + Mockito - Testing

**Inspired By**:
- MVC Architecture Best Practices
- Enterprise Java Patterns
- Material Design Guidelines
- OWASP Security Standards

---

## 📞 Support

For issues, questions, or contributions:

1. Check the `docs/` directory for detailed documentation
2. Review existing issue reports
3. Contact the development team

---

**Last Updated**: May 2024  
**Version**: 1.0.0  
**Status**: Production Ready ✅

