# ResolvIt Architecture Documentation

## System Architecture Overview

ResolvIt follows a **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                   UI Layer (JavaFX)                      │
│     Controllers, Views (FXML), Animations, Theming      │
│                  (resolvit-ui module)                    │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│            Service Layer (Business Logic)               │
│  Interfaces + Implementations for all business rules   │
│      Validation, Transaction Management, Caching      │
│              (resolvit-service module)                  │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│           DAO Layer (Data Access Objects)               │
│  Database abstraction, Query execution, Transactions   │
│    Interfaces + Implementations for persistence        │
│                (resolvit-dao module)                    │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│       Core Layer (Domain Objects & Enums)              │
│   Entities, Value Objects, Business Rules, Exceptions  │
│                (resolvit-core module)                   │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│      Database Layer (MySQL with Connection Pool)       │
│    JDBC, Connection Management, Query Execution        │
└─────────────────────────────────────────────────────────┘
```

## Design Patterns Implemented

### 1. **MVC (Model-View-Controller)**
- **Model**: Entity classes in resolvit-core
- **View**: FXML files for UI
- **Controller**: Controllers in resolvit-ui handling user interactions

### 2. **Singleton Pattern**
Used for:
- `DatabaseConnection` - Single database connection pool
- `SessionManager` - Centralized session management
- `ServiceFactory` - Single factory for service creation

```java
public class DatabaseConnection {
    private static DatabaseConnection instance;
    
    private DatabaseConnection() { }
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}
```

### 3. **Builder Pattern**
Used for constructing complex objects:

```java
Complaint complaint = new Complaint.Builder()
    .withTitle("WiFi Not Working")
    .withDescription("No internet in Block B")
    .withCategory(Category.HOSTEL)
    .withPriority(Priority.HIGH)
    .withUserId(userId)
    .build();
```

### 4. **Factory Pattern**
Centralized service creation:

```java
public class ServiceFactory {
    public static UserService getUserService() {
        return new UserServiceImpl();
    }
    
    public static ComplaintService getComplaintService() {
        return new ComplaintServiceImpl();
    }
}
```

### 5. **DAO Pattern**
Abstract data access logic:

```java
public interface UserDAO {
    User findById(String userId);
    List<User> findAll();
    void save(User user);
}
```

### 6. **Observer Pattern**
For notification system:

```java
public interface NotificationObserver {
    void update(Notification notification);
}
```

### 7. **Strategy Pattern**
For different complaint categorization strategies

## Module Organization

### resolvit-core
**Responsibility**: Core domain objects and business rules

```
com.resolvit.core/
├── entities/          (10 entity classes)
│   ├── User.java (abstract base)
│   ├── Student.java (extends User)
│   ├── Admin.java (extends User)
│   ├── SuperAdmin.java (extends Admin)
│   ├── Complaint.java (with Builder)
│   ├── Department.java
│   ├── Assignment.java
│   ├── Notification.java
│   ├── AuditLog.java
│   ├── StatusHistory.java
│   └── Feedback.java
├── enums/             (4 enum types)
│   ├── Role.java (STUDENT, ADMIN, SUPERADMIN)
│   ├── Status.java (PENDING, IN_PROGRESS, RESOLVED, REJECTED, REOPENED)
│   ├── Priority.java (LOW, MEDIUM, HIGH, CRITICAL)
│   ├── Category.java (HOSTEL, ACADEMICS, IT, TRANSPORT, CANTEEN, OTHER)
│   └── NotificationType.java
└── exceptions/        (7 custom exceptions)
    ├── ComplaintNotFoundException
    ├── UnauthorizedAccessException
    ├── DuplicateComplaintException
    ├── InvalidStatusTransitionException
    ├── SessionExpiredException
    ├── DuplicateEntryException
    └── AuthenticationException
```

### resolvit-dao
**Responsibility**: Database access and persistence

```
com.resolvit.dao/
├── interfaces/        (6 DAO interfaces)
│   ├── UserDAO
│   ├── ComplaintDAO
│   ├── DepartmentDAO
│   ├── AssignmentDAO
│   ├── NotificationDAO
│   ├── AuditLogDAO
│   └── StatusHistoryDAO
├── impl/              (6 DAO implementations)
│   ├── UserDAOImpl
│   ├── ComplaintDAOImpl
│   ├── DepartmentDAOImpl
│   ├── AssignmentDAOImpl
│   ├── NotificationDAOImpl
│   └── AuditLogDAOImpl
└── connection/
    ├── DatabaseConnection (Singleton)
    ├── ConnectionPool (10 connections)
    └── DatabaseInitializer (Auto-schema creation)
```

### resolvit-service
**Responsibility**: Business logic and service operations

```
com.resolvit.service/
├── interfaces/        (6 service interfaces)
│   ├── UserService
│   ├── ComplaintService
│   ├── NotificationService
│   ├── AuthenticationService
│   ├── AuditService
│   └── DepartmentService
├── impl/              (6 service implementations)
│   ├── UserServiceImpl
│   ├── ComplaintServiceImpl
│   ├── NotificationServiceImpl
│   ├── AuthenticationServiceImpl
│   ├── AuditServiceImpl
│   └── DepartmentServiceImpl
├── validators/
│   ├── ComplaintValidator
│   ├── UserValidator
│   └── FileValidator
├── factory/
│   └── ServiceFactory
└── session/
    └── SessionManager
```

### resolvit-ui
**Responsibility**: JavaFX presentation layer

```
com.resolvit.ui/
├── App.java           (Main entry point)
├── controllers/       (10 controllers)
│   ├── LoginController
│   ├── RegisterController
│   ├── StudentDashboardController
│   ├── ComplaintFormController
│   ├── ComplaintTrackingController
│   ├── AdminPanelController
│   ├── AnalyticsController
│   ├── NotificationController
│   ├── ProfileController
│   └── ReportController
├── utils/             (8 helper utilities)
│   ├── SessionManager
│   ├── AlertHelper
│   ├── PDFGenerator
│   ├── ChartBuilder
│   ├── FileUploadHelper
│   ├── ValidationHelper
│   ├── DateFormatter
│   └── ThemeManager
├── animations/
│   ├── FadeTransition
│   ├── SlideTransition
│   └── ShakeAnimation
└── resources/
    ├── fxml/          (9 FXML layout files)
    ├── css/           (4 stylesheets)
    └── assets/        (Images & icons)
```

## Data Flow Diagrams

### Complaint Creation Flow

```
┌──────────────┐
│  User Input  │
│  (FXML UI)   │
└──────┬───────┘
       │
       ▼
┌────────────────────────────┐
│ ComplaintFormController    │
│ - Validate input           │
│ - Call service method      │
└──────┬─────────────────────┘
       │
       ▼
┌────────────────────────────┐
│ ComplaintService           │
│ - Process business logic   │
│ - Generate complaint ID    │
│ - Call DAO method          │
└──────┬─────────────────────┘
       │
       ▼
┌────────────────────────────┐
│ ComplaintDAO               │
│ - Execute SQL INSERT       │
│ - Handle transactions      │
└──────┬─────────────────────┘
       │
       ▼
┌────────────────────────────┐
│ MySQL Database             │
│ - Store complaint          │
│ - Update audit log         │
└────────────────────────────┘
```

### Status Update with Notifications

```
┌──────────────┐
│  Admin View  │
│ Status Form  │
└──────┬───────┘
       │
       ▼
┌─────────────────────────────┐
│ ComplaintService            │
│ - Validate status change    │
│ - Update complaint status   │
│ - Create status history     │
└──────┬──────────────────────┘
       │
       ├─────────────────────────────────┐
       │                                 │
       ▼                                 ▼
┌──────────────────┐        ┌──────────────────────┐
│ ComplaintDAO     │        │ NotificationService  │
│ - Update status  │        │ - Create notification│
│ - Update audit   │        │ - Send to user       │
└──────┬───────────┘        └──────────┬───────────┘
       │                              │
       │                              ▼
       │                    ┌──────────────────┐
       │                    │ NotificationDAO  │
       │                    │ - Store notif    │
       │                    └──────┬───────────┘
       │                           │
       └──────────────┬────────────┘
                      │
                      ▼
          ┌───────────────────────┐
          │   MySQL Database      │
          │   - Store all changes │
          │   - Full audit trail  │
          └───────────────────────┘
```

## Database Schema Highlights

### Entity Relationships

```
users (1) ──── (*) complaints
  │                 │
  │                 ├──── (1) status_history (*)
  │                 ├──── (1) assignments (*) ──── (1) departments
  │                 ├──── (1) notifications (*)
  │                 └──── (1) feedback (*)
  │
  ├──── (1) departments
  │
  └──── (*) audit_log
```

### Key Indexes for Performance

- `idx_complaint_status` - Fast status filtering
- `idx_complaint_category` - Fast category queries
- `idx_complaint_created` - Time-range queries
- `idx_audit_timestamp` - Audit trail queries
- `idx_notif_user_read` - Unread notifications
- Composite indexes for common query patterns

## Security Architecture

### Authentication Flow

```
User Credentials
      │
      ▼
┌─────────────────────┐
│ AuthenticationService│
├─────────────────────┤
│ 1. Find user by ID  │
│ 2. Verify password  │
│    (BCrypt)         │
│ 3. Generate session │
│ 4. Store in Manager │
└─────────────────────┘
      │
      ▼
Session Token (Valid for 30 min)
```

### Authorization Levels

1. **STUDENT** - Can only access own complaints
2. **ADMIN** - Can access assigned department complaints
3. **SUPERADMIN** - Full system access

## Performance Optimizations

### Connection Pooling
- 10 reusable database connections
- Reduces connection overhead
- Prevents connection exhaustion

### Caching Strategy
- Session data cached in SessionManager
- User profile cached after login
- Department list cached

### Database Optimizations
- Proper indexing on frequently queried columns
- Prepared statements (prevent SQL injection)
- Query optimization with appropriate JOINs

## Error Handling

### Custom Exceptions Hierarchy

```
Exception (Java built-in)
├── ResolvItException (custom base)
│   ├── ComplaintNotFoundException
│   ├── UnauthorizedAccessException
│   ├── DuplicateComplaintException
│   ├── InvalidStatusTransitionException
│   ├── SessionExpiredException
│   └── AuthenticationException
```

### Logging Strategy
- SLF4J facade with Logback implementation
- Different log levels:
  - **DEBUG**: Detailed flow information
  - **INFO**: General application events
  - **WARN**: Potentially problematic situations
  - **ERROR**: Error conditions
  - **TRACE**: Very detailed debugging

## Extensibility Points

1. **New Service Layer**: Add new ServiceImpl and interface
2. **New DAO Operations**: Implement new methods in DAOImpl
3. **New UI Screens**: Create FXML + Controller + CSS
4. **New Business Rules**: Extend validators
5. **New Notification Types**: Add to NotificationType enum

## Testing Architecture

### Unit Tests
- Service layer tests (mocked DAOs)
- Validator tests
- Utility tests

### Integration Tests
- DAO tests (real database)
- End-to-end workflow tests
- Database transaction tests

### Test Data
- Separate test database or fixtures
- Sample data in seed_data.sql
- Factory methods for test objects

---

**Last Updated**: May 2024  
**Version**: 1.0.0  
**Status**: Production Ready
