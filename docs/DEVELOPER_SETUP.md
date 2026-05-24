# ResolvIt Developer Setup Guide

## Prerequisites Installation

### 1. Java 17 Installation

#### Windows
1. Download from [Oracle JDK 17](https://www.oracle.com/java/technologies/downloads/#java17) or [OpenJDK 17](https://adoptium.net/)
2. Run the installer
3. Verify installation:
   ```cmd
   java -version
   javac -version
   ```

#### macOS
```bash
# Using Homebrew
brew install openjdk@17

# Verify
java -version
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# Verify
java -version
```

### 2. Maven Installation

#### Windows
1. Download from [Maven Official Site](https://maven.apache.org/download.cgi)
2. Extract to `C:\Program Files\Apache\maven`
3. Add to PATH: `C:\Program Files\Apache\maven\bin`
4. Verify:
   ```cmd
   mvn -version
   ```

#### macOS
```bash
brew install maven
mvn -version
```

#### Linux
```bash
sudo apt-get install maven
mvn -version
```

### 3. MySQL 8.0 Installation

#### Windows
1. Download from [MySQL Official](https://dev.mysql.com/downloads/mysql/)
2. Run MSI installer
3. Configure MySQL Server (port 3306)
4. Verify:
   ```cmd
   mysql --version
   ```

#### macOS
```bash
brew install mysql
mysql.server start
mysql --version
```

#### Linux
```bash
sudo apt-get install mysql-server
sudo service mysql start
mysql --version
```

### 4. IDE Setup

#### Option A: IntelliJ IDEA (Recommended)
1. Download [IntelliJ IDEA Community](https://www.jetbrains.com/idea/)
2. Install and open
3. File → Open → Select resolvit folder
4. Configure JDK:
   - File → Project Structure → Project
   - Set SDK to Java 17
5. Enable Maven:
   - File → Settings → Build, Execution → Build Tools → Maven

#### Option B: Eclipse IDE
1. Download [Eclipse IDE for Java Developers](https://www.eclipse.org/downloads/)
2. Install and extract
3. File → Import → Maven → Existing Maven Projects
4. Select resolvit folder

#### Option C: VS Code
1. Install [Visual Studio Code](https://code.visualstudio.com/)
2. Install extensions:
   - Extension Pack for Java (Microsoft)
   - Maven for Java (Microsoft)
   - JavaFX Support (gluonhq)
3. Open resolvit folder in VS Code

## Database Setup

### Step 1: Start MySQL Server

**Windows**:
```cmd
net start MySQL80
```

**macOS**:
```bash
mysql.server start
```

**Linux**:
```bash
sudo service mysql start
```

### Step 2: Create Database

```bash
# Connect to MySQL
mysql -u root -p

# Enter password (default: root or your chosen password)
```

### Step 3: Run Schema

```bash
# Exit MySQL prompt
EXIT;

# Run schema script
mysql -u root -p < database/schema.sql

# Enter password when prompted
```

### Step 4: Verify Database

```bash
mysql -u root -p
USE resolvit_db;
SHOW TABLES;
EXIT;
```

Expected output: 8 tables (users, complaints, departments, assignments, status_history, notifications, audit_log, feedback)

## Build & Run

### Build Project

```bash
cd resolvit

# Clean and install all modules
mvn clean install

# Build without tests (faster)
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl resolvit-ui
```

### Run Application

#### Option 1: Maven
```bash
cd resolvit-ui
mvn javafx:run
```

#### Option 2: IDE
- Right-click on `App.java` → Run
- (IntelliJ IDEA / Eclipse)

#### Option 3: JAR File
```bash
java -jar resolvit-ui/target/resolvit-ui-1.0.0.jar
```

## Default Credentials

After schema import, use these to login:

### SuperAdmin Account
- **Email**: superadmin@resolvit.com
- **Password**: Admin@123

### Admin Account
- **Email**: itadmin@resolvit.com
- **Password**: Admin@123

### Student Account
- **Email**: john.doe@college.edu
- **Password**: Student@123

## Project Structure in IDE

```
resolvit/
├── resolvit-core/              (Core module)
│   └── src/main/java/com/resolvit/core/
│       ├── entities/
│       ├── enums/
│       └── exceptions/
├── resolvit-dao/               (DAO module)
├── resolvit-service/           (Service module)
├── resolvit-ui/                (UI module)
│   └── src/main/resources/
│       ├── fxml/               (FXML layouts)
│       └── css/                (Stylesheets)
└── resolvit-tests/             (Tests module)
```

## Development Workflow

### 1. Create Feature Branch
```bash
git checkout -b feature/new-feature
```

### 2. Make Changes
- Edit files in appropriate modules
- Follow existing patterns
- Add tests for new features

### 3. Run Tests
```bash
mvn test
```

### 4. Build Project
```bash
mvn clean install
```

### 5. Run Application
```bash
cd resolvit-ui
mvn javafx:run
```

### 6. Commit Changes
```bash
git add .
git commit -m "Add new feature"
```

### 7. Create Pull Request
- Push to remote
- Create PR with description
- Wait for CI/CD checks

## Common Issues & Solutions

### Issue: "Cannot find symbol" in IDE

**Solution**:
1. Right-click project → Maven → Reload Projects
2. Or rebuild: `mvn clean install`
3. IntelliJ: File → Invalidate Caches → Restart

### Issue: MySQL Connection Refused

**Solution**:
1. Check if MySQL is running: `mysql -u root -p`
2. If not running:
   - Windows: `net start MySQL80`
   - macOS: `mysql.server start`
   - Linux: `sudo service mysql start`

### Issue: "No module named resolvit-core"

**Solution**:
1. Run `mvn install` in root directory
2. Refresh Maven in IDE
3. Clean rebuild: `mvn clean install`

### Issue: JavaFX not found

**Solution**:
1. Ensure Java 17 is being used
2. Run: `mvn javafx:run` instead of running directly
3. Check pom.xml has JavaFX dependencies

### Issue: Port 3306 Already in Use

**Solution**:
```bash
# Find process using port 3306
lsof -i :3306

# Kill the process
kill -9 <PID>
```

## Code Style Guidelines

### Java Code Style

```java
// Class naming: PascalCase
public class ComplaintService {
    
    // Method naming: camelCase
    public Complaint createComplaint(String title) {
        // Implementation
    }
    
    // Constants: UPPER_SNAKE_CASE
    private static final int MAX_TITLE_LENGTH = 200;
    
    // Variables: camelCase
    private String complaintId;
}
```

### Naming Conventions

| Component | Convention | Example |
|-----------|-----------|---------|
| Classes | PascalCase | `UserService` |
| Methods | camelCase | `getUserById()` |
| Variables | camelCase | `userId` |
| Constants | UPPER_SNAKE_CASE | `MAX_LENGTH` |
| Packages | lowercase | `com.resolvit.service` |

### Formatting

- Indentation: 4 spaces (not tabs)
- Line length: Max 120 characters
- Braces: Opening on same line
- Imports: Organized, one per line

## Testing Guidelines

### Unit Test Template

```java
@DisplayName("ComplaintService Tests")
class ComplaintServiceTest {
    
    private ComplaintService complaintService;
    private ComplaintDAO complaintDAOMock;
    
    @BeforeEach
    void setUp() {
        complaintDAOMock = mock(ComplaintDAO.class);
        complaintService = new ComplaintServiceImpl(complaintDAOMock);
    }
    
    @Test
    @DisplayName("Should create complaint successfully")
    void testCreateComplaint() {
        // Arrange
        Complaint complaint = new Complaint("title", "desc");
        
        // Act
        Complaint result = complaintService.createComplaint(complaint);
        
        // Assert
        assertNotNull(result);
        verify(complaintDAOMock).save(complaint);
    }
}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ComplaintServiceTest

# Run with code coverage
mvn test jacoco:report

# View coverage report
# Open target/site/jacoco/index.html
```

## Performance Profiling

### Monitor Application

```bash
# Run with JVM arguments for monitoring
java -Xmx1G -Xms512M -XX:+UseG1GC -jar resolvit-ui-1.0.0.jar
```

### Database Query Performance

```bash
# Enable MySQL query log
mysql -u root -p
SET GLOBAL general_log = 'ON';
SET GLOBAL log_output = 'TABLE';

# View slow queries
SELECT * FROM mysql.general_log WHERE query_time > 0.1;

# Disable when done
SET GLOBAL general_log = 'OFF';
```

## Debugging Tips

### Enable Debug Logging

In `logback.xml` (or configure in code):
```xml
<logger name="com.resolvit" level="DEBUG"/>
```

### Remote Debugging

```bash
# Run with debug flags
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar resolvit-ui-1.0.0.jar

# Connect from IDE:
# Run → Debug → Select "Remote" configuration
```

### SQL Debugging

```java
// In DAO methods
logger.debug("Executing query: " + sql);
logger.debug("Parameters: " + Arrays.toString(params));
```

## Useful Commands

```bash
# Clean project
mvn clean

# Compile only
mvn compile

# Run tests only
mvn test

# Create package
mvn package

# Generate documentation
mvn javadoc:javadoc

# Check for updates
mvn versions:display-updates

# Generate project site
mvn site

# Run specific profile
mvn clean install -Pprod

# Show dependency tree
mvn dependency:tree

# Check for security vulnerabilities
mvn org.owasp:dependency-check-maven:check
```

## Documentation

- Architecture: See `docs/ARCHITECTURE.md`
- API Documentation: `mvn javadoc:javadoc` → target/site/apidocs/index.html
- Database Schema: See `database/schema.sql`
- User Manual: `docs/user_manual.pdf`

## Getting Help

1. **Check documentation**: `docs/` folder
2. **Review existing issues**: GitHub Issues
3. **Ask questions**: Create a GitHub Discussion
4. **Report bugs**: GitHub Issues with reproduction steps

## IDE Keyboard Shortcuts

### IntelliJ IDEA
- `Ctrl+Space` - Code completion
- `Ctrl+/` - Toggle comment
- `Ctrl+Shift+F` - Format code
- `Ctrl+Alt+L` - Reformat code
- `Shift+F6` - Rename
- `Ctrl+F` - Find
- `Ctrl+H` - Find and replace

### Eclipse
- `Ctrl+Space` - Content assist
- `Ctrl+/` - Toggle comment
- `Ctrl+Shift+F` - Format
- `Alt+Shift+R` - Rename
- `Ctrl+F` - Find
- `Ctrl+H` - Find and replace

---

**Happy Coding!** 🚀

For questions or issues, please refer to the main README.md or contact the development team.
