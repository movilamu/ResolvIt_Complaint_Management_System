package com.resolvit.ui.controllers;

import com.resolvit.core.entities.Department;
import com.resolvit.core.entities.Student;
import com.resolvit.core.exceptions.DuplicateEntryException;
import com.resolvit.service.factory.ServiceFactory;
import com.resolvit.service.interfaces.DepartmentService;
import com.resolvit.service.interfaces.UserService;
import com.resolvit.ui.App;
import com.resolvit.ui.animations.ShakeAnimation;
import com.resolvit.ui.utils.AlertHelper;
import com.resolvit.ui.utils.ValidationHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the Registration screen.
 * Handles new student registration with validation.
 *
 * @author ResolvIt Team
 * @version 1.0.0
 */
public class RegisterController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);

    // FXML injected fields
    @FXML private VBox registerCard;
    @FXML private TextField nameField;
    @FXML private Label nameError;
    @FXML private TextField emailField;
    @FXML private Label emailError;
    @FXML private TextField rollNumberField;
    @FXML private TextField classNameField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<Department> departmentComboBox;
    @FXML private PasswordField passwordField;
    @FXML private VBox strengthMeterContainer;
    @FXML private Region strengthBar1;
    @FXML private Region strengthBar2;
    @FXML private Region strengthBar3;
    @FXML private Region strengthBar4;
    @FXML private Label strengthLabel;
    @FXML private Label passwordError;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label confirmPasswordError;
    @FXML private HBox errorContainer;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;
    @FXML private HBox loadingContainer;

    private final UserService userService;
    private final DepartmentService departmentService;
    private final ExecutorService executor;

    /**
     * Default constructor initializes services.
     */
    public RegisterController() {
        this.userService = ServiceFactory.getInstance().getUserService();
        this.departmentService = ServiceFactory.getInstance().getDepartmentService();
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing RegisterController");

        // Load departments
        loadDepartments();

        // Setup password strength meter
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));

        // Setup confirm password validation
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswordMatch());

        // Setup real-time validation
        setupFieldValidation();

        // Hide errors initially
        hideAllErrors();

        log.debug("RegisterController initialized");
    }

    /**
     * Loads departments into the combo box.
     */
    private void loadDepartments() {
        Task<List<Department>> task = new Task<>() {
            @Override
            protected List<Department> call() {
                return departmentService.getAllDepartments();
            }
        };

        task.setOnSucceeded(event -> {
            List<Department> departments = task.getValue();
            Platform.runLater(() -> {
                departmentComboBox.setItems(FXCollections.observableArrayList(departments));
                departmentComboBox.setConverter(new javafx.util.StringConverter<>() {
                    @Override
                    public String toString(Department dept) {
                        return dept != null ? dept.getDeptName() : "";
                    }

                    @Override
                    public Department fromString(String string) {
                        return null;
                    }
                });
            });
        });

        executor.submit(task);
    }

    /**
     * Updates the password strength meter.
     */
    private void updatePasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);

        // Reset all bars
        String emptyClass = "password-strength-bar-empty";
        strengthBar1.getStyleClass().removeAll("password-strength-weak", "password-strength-medium", "password-strength-strong");
        strengthBar2.getStyleClass().removeAll("password-strength-weak", "password-strength-medium", "password-strength-strong");
        strengthBar3.getStyleClass().removeAll("password-strength-weak", "password-strength-medium", "password-strength-strong");
        strengthBar4.getStyleClass().removeAll("password-strength-weak", "password-strength-medium", "password-strength-strong");

        if (!strengthBar1.getStyleClass().contains(emptyClass)) strengthBar1.getStyleClass().add(emptyClass);
        if (!strengthBar2.getStyleClass().contains(emptyClass)) strengthBar2.getStyleClass().add(emptyClass);
        if (!strengthBar3.getStyleClass().contains(emptyClass)) strengthBar3.getStyleClass().add(emptyClass);
        if (!strengthBar4.getStyleClass().contains(emptyClass)) strengthBar4.getStyleClass().add(emptyClass);

        if (password.isEmpty()) {
            strengthLabel.setText("");
            return;
        }

        String strengthClass;
        String label;

        if (strength == 1) {
            strengthClass = "password-strength-weak";
            label = "Weak - Add uppercase, numbers, and special characters";
            strengthBar1.getStyleClass().remove(emptyClass);
            strengthBar1.getStyleClass().add(strengthClass);
        } else if (strength == 2) {
            strengthClass = "password-strength-medium";
            label = "Medium - Add special characters for stronger password";
            strengthBar1.getStyleClass().remove(emptyClass);
            strengthBar2.getStyleClass().remove(emptyClass);
            strengthBar1.getStyleClass().add(strengthClass);
            strengthBar2.getStyleClass().add(strengthClass);
        } else if (strength == 3) {
            strengthClass = "password-strength-medium";
            label = "Good - Almost there!";
            strengthBar1.getStyleClass().remove(emptyClass);
            strengthBar2.getStyleClass().remove(emptyClass);
            strengthBar3.getStyleClass().remove(emptyClass);
            strengthBar1.getStyleClass().add(strengthClass);
            strengthBar2.getStyleClass().add(strengthClass);
            strengthBar3.getStyleClass().add(strengthClass);
        } else {
            strengthClass = "password-strength-strong";
            label = "Strong - Great password!";
            strengthBar1.getStyleClass().remove(emptyClass);
            strengthBar2.getStyleClass().remove(emptyClass);
            strengthBar3.getStyleClass().remove(emptyClass);
            strengthBar4.getStyleClass().remove(emptyClass);
            strengthBar1.getStyleClass().add(strengthClass);
            strengthBar2.getStyleClass().add(strengthClass);
            strengthBar3.getStyleClass().add(strengthClass);
            strengthBar4.getStyleClass().add(strengthClass);
        }

        strengthLabel.setText(label);
    }

    /**
     * Calculates password strength (0-4).
     */
    private int calculatePasswordStrength(String password) {
        if (password == null || password.length() < 8) return 1;

        int strength = 1;
        if (password.matches(".*[A-Z].*") && password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength++;

        return strength;
    }

    /**
     * Validates that passwords match.
     */
    private void validatePasswordMatch() {
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (confirm.isEmpty()) {
            hideError(confirmPasswordError);
            return;
        }

        if (!password.equals(confirm)) {
            showFieldError(confirmPasswordError, "Passwords do not match");
        } else {
            hideError(confirmPasswordError);
        }
    }

    /**
     * Sets up real-time field validation.
     */
    private void setupFieldValidation() {
        nameField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) validateName();
        });

        emailField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) validateEmail();
        });
    }

    /**
     * Validates the name field.
     */
    private boolean validateName() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showFieldError(nameError, "Name is required");
            return false;
        }
        if (name.length() < 2) {
            showFieldError(nameError, "Name must be at least 2 characters");
            return false;
        }
        hideError(nameError);
        return true;
    }

    /**
     * Validates the email field.
     */
    private boolean validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showFieldError(emailError, "Email is required");
            return false;
        }
        if (!ValidationHelper.isValidEmail(email)) {
            showFieldError(emailError, "Please enter a valid email address");
            return false;
        }
        hideError(emailError);
        return true;
    }

    /**
     * Validates all fields before registration.
     */
    private boolean validateAll() {
        boolean valid = true;

        if (!validateName()) valid = false;
        if (!validateEmail()) valid = false;

        // Validate password
        String password = passwordField.getText();
        if (password.isEmpty()) {
            showFieldError(passwordError, "Password is required");
            valid = false;
        } else if (password.length() < 8) {
            showFieldError(passwordError, "Password must be at least 8 characters");
            valid = false;
        } else if (calculatePasswordStrength(password) < 2) {
            showFieldError(passwordError, "Password is too weak");
            valid = false;
        } else {
            hideError(passwordError);
        }

        // Validate confirm password
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showFieldError(confirmPasswordError, "Passwords do not match");
            valid = false;
        }

        return valid;
    }

    /**
     * Handles register button click.
     */
    @FXML
    private void onRegisterClick() {
        hideGeneralError();

        if (!validateAll()) {
            ShakeAnimation.shake(registerCard);
            return;
        }

        setLoadingState(true);

        // Create student object
        Student student = new Student();
        student.setName(nameField.getText().trim());
        student.setEmail(emailField.getText().trim().toLowerCase());
        student.setRollNumber(rollNumberField.getText().trim());
        student.setClassName(classNameField.getText().trim());
        student.setPhone(phoneField.getText().trim());
        student.setDepartment(departmentComboBox.getValue());

        String password = passwordField.getText();

        // Execute registration in background
        Task<Student> registerTask = new Task<>() {
            @Override
            protected Student call() throws Exception {
                return userService.createStudent(student, password);
            }
        };

        registerTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                setLoadingState(false);
                AlertHelper.showInfo("Registration Successful",
                        "Your account has been created! Please login with your credentials.");
                App.navigateTo("login");
            });
        });

        registerTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                setLoadingState(false);
                Throwable ex = registerTask.getException();

                if (ex instanceof DuplicateEntryException) {
                    showGeneralError(ex.getMessage());
                } else {
                    showGeneralError("Registration failed. Please try again.");
                    log.error("Registration failed", ex);
                }

                ShakeAnimation.shake(registerCard);
            });
        });

        executor.submit(registerTask);
    }

    /**
     * Handles back to login click.
     */
    @FXML
    private void onBackToLogin() {
        App.navigateTo("login");
    }

    // Helper methods for error display
    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void hideAllErrors() {
        hideError(nameError);
        hideError(emailError);
        hideError(passwordError);
        hideError(confirmPasswordError);
        hideGeneralError();
    }

    private void showGeneralError(String message) {
        errorLabel.setText(message);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
    }

    private void hideGeneralError() {
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
    }

    private void setLoadingState(boolean loading) {
        registerButton.setDisable(loading);
        registerButton.setVisible(!loading);
        registerButton.setManaged(!loading);
        loadingContainer.setVisible(loading);
        loadingContainer.setManaged(loading);
    }

    /**
     * Clean up resources.
     */
    public void cleanup() {
        executor.shutdown();
    }
}
