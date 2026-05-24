package com.resolvit.ui.controllers;

import com.resolvit.core.entities.User;
import com.resolvit.core.exceptions.AuthenticationException;
import com.resolvit.service.factory.ServiceFactory;
import com.resolvit.service.interfaces.AuthenticationService;
import com.resolvit.service.session.SessionManager;
import com.resolvit.ui.App;
import com.resolvit.ui.animations.ShakeAnimation;
import com.resolvit.ui.utils.AlertHelper;
import com.resolvit.ui.utils.ValidationHelper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

/**
 * Controller for the Login screen.
 * Handles user authentication and navigation.
 *
 * @author ResolvIt Team
 * @version 1.0.0
 */
public class LoginController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    private static final String PREF_REMEMBERED_EMAIL = "rememberedEmail";

    // FXML injected fields
    @FXML private VBox loginCard;
    @FXML private ImageView logoImage;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private HBox errorContainer;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private HBox loadingContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Hyperlink registerLink;

    private final AuthenticationService authService;
    private final Preferences preferences;
    private final ExecutorService executor;
    private boolean passwordVisible = false;

    /**
     * Default constructor initializes services.
     */
    public LoginController() {
        this.authService = ServiceFactory.getInstance().getAuthenticationService();
        this.preferences = Preferences.userNodeForPackage(LoginController.class);
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing LoginController");

        // Load remembered email if exists
        loadRememberedEmail();

        // Setup enter key handler on password field
        passwordField.setOnAction(event -> onLoginClick());
        passwordTextField.setOnAction(event -> onLoginClick());
        emailField.setOnAction(event -> passwordField.requestFocus());

        // Sync password fields
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

        // Hide error initially
        hideError();

        log.debug("LoginController initialized");
    }

    /**
     * Handles login button click.
     */
    @FXML
    private void onLoginClick() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Show loading state
        setLoadingState(true);
        hideError();

        // Execute login in background thread
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return authService.login(email, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            User user = loginTask.getValue();
            handleLoginSuccess(user);
        });

        loginTask.setOnFailed(event -> {
            Throwable exception = loginTask.getException();
            handleLoginFailure(exception);
        });

        executor.submit(loginTask);
    }

    /**
     * Validates login inputs.
     */
    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            showError("Please enter your email address.");
            emailField.requestFocus();
            return false;
        }

        if (!ValidationHelper.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            emailField.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showError("Please enter your password.");
            passwordField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Handles successful login.
     */
    private void handleLoginSuccess(User user) {
        Platform.runLater(() -> {
            log.info("Login successful for user: {}", user.getEmail());

            // Save email if remember me is checked
            if (rememberMeCheckbox.isSelected()) {
                preferences.put(PREF_REMEMBERED_EMAIL, user.getEmail());
            } else {
                preferences.remove(PREF_REMEMBERED_EMAIL);
            }

            setLoadingState(false);

            // Navigate to appropriate dashboard based on role
            String homeScreen = user.getHomeScreen();
            App.navigateTo(homeScreen.replace(".fxml", ""));
        });
    }

    /**
     * Handles login failure.
     */
    private void handleLoginFailure(Throwable exception) {
        Platform.runLater(() -> {
            setLoadingState(false);

            String message;
            if (exception instanceof AuthenticationException) {
                message = exception.getMessage();
            } else {
                message = "An unexpected error occurred. Please try again.";
                log.error("Login failed with unexpected error", exception);
            }

            showError(message);

            // Shake animation on error
            ShakeAnimation.shake(loginCard);
        });
    }

    /**
     * Toggles password visibility.
     */
    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("Hide");
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            togglePasswordBtn.setText("Show");
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    /**
     * Handles forgot password link click.
     */
    @FXML
    private void onForgotPassword() {
        String email = emailField.getText().trim();

        TextInputDialog dialog = new TextInputDialog(email);
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Forgot your password?");
        dialog.setContentText("Enter your email to receive a password reset link:");

        dialog.showAndWait().ifPresent(inputEmail -> {
            if (ValidationHelper.isValidEmail(inputEmail)) {
                // In a real app, this would send a reset email
                AlertHelper.showInfo("Password Reset",
                        "If an account exists with this email, you will receive a password reset link.");
                log.info("Password reset requested for: {}", inputEmail);
            } else {
                AlertHelper.showError("Invalid Email", "Please enter a valid email address.");
            }
        });
    }

    /**
     * Handles register link click - navigates to registration page.
     */
    @FXML
    private void onRegisterClick() {
        log.debug("Navigating to registration page");
        App.navigateTo("register");
    }

    /**
     * Shows error message with animation.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
    }

    /**
     * Hides error message.
     */
    private void hideError() {
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
    }

    /**
     * Sets loading state for the login form.
     */
    private void setLoadingState(boolean loading) {
        loginButton.setDisable(loading);
        loginButton.setVisible(!loading);
        loginButton.setManaged(!loading);
        loadingContainer.setVisible(loading);
        loadingContainer.setManaged(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        passwordTextField.setDisable(loading);
    }

    /**
     * Loads remembered email from preferences.
     */
    private void loadRememberedEmail() {
        String rememberedEmail = preferences.get(PREF_REMEMBERED_EMAIL, null);
        if (rememberedEmail != null && !rememberedEmail.isEmpty()) {
            emailField.setText(rememberedEmail);
            rememberMeCheckbox.setSelected(true);
            passwordField.requestFocus();
        }
    }

    /**
     * Clean up resources when controller is destroyed.
     */
    public void cleanup() {
        executor.shutdown();
    }
}
