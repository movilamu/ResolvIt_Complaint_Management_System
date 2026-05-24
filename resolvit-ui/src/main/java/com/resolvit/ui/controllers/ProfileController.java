package com.resolvit.ui.controllers;

import com.resolvit.core.entities.User;
import com.resolvit.core.entities.Student;
import com.resolvit.core.enums.Role;
import com.resolvit.service.factory.ServiceFactory;
import com.resolvit.service.interfaces.UserService;
import com.resolvit.service.session.SessionManager;
import com.resolvit.ui.animations.FadeAnimation;
import com.resolvit.ui.utils.AlertUtils;
import com.resolvit.ui.utils.NavigationManager;
import com.resolvit.ui.utils.ThemeManager;
import com.resolvit.ui.utils.ValidationUtils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the Profile screen.
 * Handles user profile viewing and editing.
 */
public class ProfileController implements Initializable {

    // Profile Display
    @FXML private ImageView profileImage;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label memberSinceLabel;
    @FXML private Circle profileImageClip;

    // Edit Form Fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    // Student-specific fields
    @FXML private VBox studentFieldsContainer;
    @FXML private TextField studentIdField;
    @FXML private TextField departmentField;
    @FXML private TextField batchField;

    // Password Change
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordStrengthLabel;
    @FXML private ProgressBar passwordStrengthBar;

    // Settings
    @FXML private ToggleButton darkModeToggle;
    @FXML private ToggleButton emailNotificationsToggle;
    @FXML private ToggleButton pushNotificationsToggle;
    @FXML private ComboBox<String> languageCombo;

    // Statistics
    @FXML private Label totalComplaintsLabel;
    @FXML private Label resolvedComplaintsLabel;
    @FXML private Label pendingComplaintsLabel;

    // Action Buttons
    @FXML private Button editProfileBtn;
    @FXML private Button saveProfileBtn;
    @FXML private Button cancelEditBtn;
    @FXML private Button changePasswordBtn;
    @FXML private Button changePhotoBtn;

    // Containers
    @FXML private StackPane rootPane;
    @FXML private VBox viewModeContainer;
    @FXML private VBox editModeContainer;
    @FXML private ProgressIndicator loadingIndicator;

    // Tabs
    @FXML private Button profileTab;
    @FXML private Button securityTab;
    @FXML private Button preferencesTab;
    @FXML private VBox profileContent;
    @FXML private VBox securityContent;
    @FXML private VBox preferencesContent;

    private final UserService userService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    private User currentUser;
    private boolean isEditing = false;
    private File selectedProfileImage;

    public ProfileController() {
        this.userService = ServiceFactory.getInstance().getUserService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCurrentUser();
        setupPasswordStrengthChecker();
        setupPreferences();
        showProfileTab();

        FadeAnimation.fadeIn(rootPane, 300);
    }

    private void loadCurrentUser() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertUtils.showError("Error", "No user logged in.");
            NavigationManager.getInstance().navigateTo("login");
            return;
        }

        populateUserData();
        loadUserStatistics();
    }

    private void populateUserData() {
        if (currentUser == null) return;

        // Display mode
        if (fullNameLabel != null) {
            fullNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        }
        if (emailLabel != null) emailLabel.setText(currentUser.getEmail());
        if (roleLabel != null) roleLabel.setText(currentUser.getRole().getDisplayName());
        if (memberSinceLabel != null && currentUser.getCreatedAt() != null) {
            memberSinceLabel.setText("Member since " + currentUser.getCreatedAt().format(dateFormatter));
        }

        // Edit form
        if (firstNameField != null) firstNameField.setText(currentUser.getFirstName());
        if (lastNameField != null) lastNameField.setText(currentUser.getLastName());
        if (emailField != null) emailField.setText(currentUser.getEmail());
        if (phoneField != null) phoneField.setText(currentUser.getPhone());

        // Student-specific fields
        boolean isStudent = currentUser.getRole() == Role.STUDENT;
        if (studentFieldsContainer != null) {
            studentFieldsContainer.setVisible(isStudent);
            studentFieldsContainer.setManaged(isStudent);
        }

        if (isStudent && currentUser instanceof Student student) {
            if (studentIdField != null) studentIdField.setText(student.getStudentId());
            if (departmentField != null) departmentField.setText(student.getDepartment());
            if (batchField != null) batchField.setText(student.getBatch());
        }

        // Profile image
        loadProfileImage();
    }

    private void loadProfileImage() {
        if (profileImage == null) return;

        String imageUrl = currentUser.getProfileImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Image image = new Image(imageUrl, true);
                profileImage.setImage(image);
            } catch (Exception e) {
                setDefaultProfileImage();
            }
        } else {
            setDefaultProfileImage();
        }

        // Apply circular clip
        if (profileImageClip != null) {
            profileImage.setClip(profileImageClip);
        }
    }

    private void setDefaultProfileImage() {
        // Set default avatar
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/assets/default-avatar.png"));
            if (profileImage != null) {
                profileImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            // Ignore if default image not found
        }
    }

    private void loadUserStatistics() {
        Task<int[]> statsTask = new Task<>() {
            @Override
            protected int[] call() throws Exception {
                // Get complaint statistics for user
                var complaintService = ServiceFactory.getInstance().getComplaintService();
                var complaints = complaintService.getComplaintsByUserId(currentUser.getUserId());

                int total = complaints.size();
                int resolved = (int) complaints.stream()
                    .filter(c -> c.getStatus() == com.resolvit.core.enums.Status.RESOLVED)
                    .count();
                int pending = total - resolved;

                return new int[]{total, resolved, pending};
            }
        };

        statsTask.setOnSucceeded(event -> Platform.runLater(() -> {
            int[] stats = statsTask.getValue();
            if (totalComplaintsLabel != null) totalComplaintsLabel.setText(String.valueOf(stats[0]));
            if (resolvedComplaintsLabel != null) resolvedComplaintsLabel.setText(String.valueOf(stats[1]));
            if (pendingComplaintsLabel != null) pendingComplaintsLabel.setText(String.valueOf(stats[2]));
        }));

        executorService.submit(statsTask);
    }

    private void setupPasswordStrengthChecker() {
        if (newPasswordField == null) return;

        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
        });
    }

    private void updatePasswordStrength(String password) {
        if (passwordStrengthLabel == null || passwordStrengthBar == null) return;

        if (password == null || password.isEmpty()) {
            passwordStrengthLabel.setText("");
            passwordStrengthBar.setProgress(0);
            return;
        }

        int strength = calculatePasswordStrength(password);
        double progress = strength / 4.0;
        passwordStrengthBar.setProgress(progress);

        String strengthText;
        String styleClass;

        if (strength <= 1) {
            strengthText = "Weak";
            styleClass = "strength-weak";
        } else if (strength == 2) {
            strengthText = "Fair";
            styleClass = "strength-fair";
        } else if (strength == 3) {
            strengthText = "Good";
            styleClass = "strength-good";
        } else {
            strengthText = "Strong";
            styleClass = "strength-strong";
        }

        passwordStrengthLabel.setText(strengthText);
        passwordStrengthBar.getStyleClass().removeAll("strength-weak", "strength-fair", "strength-good", "strength-strong");
        passwordStrengthBar.getStyleClass().add(styleClass);
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength++;
        return Math.min(strength, 4);
    }

    private void setupPreferences() {
        // Language combo
        if (languageCombo != null) {
            languageCombo.getItems().addAll("English", "Spanish", "French", "German", "Chinese");
            languageCombo.setValue("English");
        }

        // Dark mode toggle
        if (darkModeToggle != null) {
            darkModeToggle.setSelected(ThemeManager.getInstance().isDarkMode());
            darkModeToggle.setOnAction(e -> {
                boolean isDark = darkModeToggle.isSelected();
                ThemeManager.getInstance().setDarkMode(isDark);
            });
        }

        // Notification toggles (default to true)
        if (emailNotificationsToggle != null) emailNotificationsToggle.setSelected(true);
        if (pushNotificationsToggle != null) pushNotificationsToggle.setSelected(true);
    }

    // Tab Navigation
    @FXML
    private void showProfileTab() {
        setActiveTab(profileTab);
        showContent(profileContent);
    }

    @FXML
    private void showSecurityTab() {
        setActiveTab(securityTab);
        showContent(securityContent);
    }

    @FXML
    private void showPreferencesTab() {
        setActiveTab(preferencesTab);
        showContent(preferencesContent);
    }

    private void setActiveTab(Button activeTab) {
        if (profileTab != null) profileTab.getStyleClass().remove("tab-active");
        if (securityTab != null) securityTab.getStyleClass().remove("tab-active");
        if (preferencesTab != null) preferencesTab.getStyleClass().remove("tab-active");

        if (activeTab != null) activeTab.getStyleClass().add("tab-active");
    }

    private void showContent(VBox contentToShow) {
        if (profileContent != null) profileContent.setVisible(false);
        if (securityContent != null) securityContent.setVisible(false);
        if (preferencesContent != null) preferencesContent.setVisible(false);

        if (contentToShow != null) {
            contentToShow.setVisible(true);
            FadeAnimation.fadeIn(contentToShow, 200);
        }
    }

    // Profile Edit Actions
    @FXML
    private void handleEditProfile() {
        isEditing = true;
        toggleEditMode(true);
    }

    @FXML
    private void handleCancelEdit() {
        isEditing = false;
        populateUserData(); // Reset fields
        toggleEditMode(false);
    }

    @FXML
    private void handleSaveProfile() {
        if (!validateProfileForm()) {
            return;
        }

        showLoading(true);

        Task<Boolean> saveTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // Update user object
                currentUser.setFirstName(firstNameField.getText().trim());
                currentUser.setLastName(lastNameField.getText().trim());
                currentUser.setEmail(emailField.getText().trim());
                currentUser.setPhone(phoneField.getText().trim());

                if (currentUser instanceof Student student) {
                    student.setDepartment(departmentField.getText().trim());
                    student.setBatch(batchField.getText().trim());
                }

                return userService.updateUser(currentUser);
            }
        };

        saveTask.setOnSucceeded(event -> Platform.runLater(() -> {
            showLoading(false);
            if (saveTask.getValue()) {
                isEditing = false;
                toggleEditMode(false);
                populateUserData();
                AlertUtils.showInfo("Success", "Profile updated successfully.");
            } else {
                AlertUtils.showError("Error", "Failed to update profile.");
            }
        }));

        saveTask.setOnFailed(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showError("Error", "Failed to update profile: " + 
                saveTask.getException().getMessage());
        }));

        executorService.submit(saveTask);
    }

    private boolean validateProfileForm() {
        StringBuilder errors = new StringBuilder();

        if (firstNameField == null || firstNameField.getText().trim().isEmpty()) {
            errors.append("First name is required.\n");
        }
        if (lastNameField == null || lastNameField.getText().trim().isEmpty()) {
            errors.append("Last name is required.\n");
        }
        if (emailField == null || !ValidationUtils.isValidEmail(emailField.getText())) {
            errors.append("Valid email is required.\n");
        }

        if (errors.length() > 0) {
            AlertUtils.showError("Validation Error", errors.toString());
            return false;
        }
        return true;
    }

    private void toggleEditMode(boolean editing) {
        if (viewModeContainer != null) viewModeContainer.setVisible(!editing);
        if (editModeContainer != null) editModeContainer.setVisible(editing);

        if (editProfileBtn != null) editProfileBtn.setVisible(!editing);
        if (saveProfileBtn != null) saveProfileBtn.setVisible(editing);
        if (cancelEditBtn != null) cancelEditBtn.setVisible(editing);

        // Disable/enable form fields
        if (firstNameField != null) firstNameField.setEditable(editing);
        if (lastNameField != null) lastNameField.setEditable(editing);
        if (emailField != null) emailField.setEditable(editing);
        if (phoneField != null) phoneField.setEditable(editing);
        if (departmentField != null) departmentField.setEditable(editing);
        if (batchField != null) batchField.setEditable(editing);
    }

    // Photo Change
    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (selectedFile != null) {
            selectedProfileImage = selectedFile;
            try {
                Image image = new Image(selectedFile.toURI().toString());
                profileImage.setImage(image);
                uploadProfileImage(selectedFile);
            } catch (Exception e) {
                AlertUtils.showError("Error", "Failed to load image: " + e.getMessage());
            }
        }
    }

    private void uploadProfileImage(File imageFile) {
        showLoading(true);

        Task<String> uploadTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                // In a real app, this would upload to a server and return the URL
                return imageFile.toURI().toString();
            }
        };

        uploadTask.setOnSucceeded(event -> Platform.runLater(() -> {
            showLoading(false);
            String imageUrl = uploadTask.getValue();
            currentUser.setProfileImageUrl(imageUrl);
            AlertUtils.showInfo("Success", "Profile picture updated.");
        }));

        uploadTask.setOnFailed(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showError("Error", "Failed to upload image.");
        }));

        executorService.submit(uploadTask);
    }

    // Password Change
    @FXML
    private void handleChangePassword() {
        String currentPassword = currentPasswordField != null ? currentPasswordField.getText() : "";
        String newPassword = newPasswordField != null ? newPasswordField.getText() : "";
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        // Validation
        StringBuilder errors = new StringBuilder();

        if (currentPassword.isEmpty()) {
            errors.append("Current password is required.\n");
        }
        if (newPassword.isEmpty()) {
            errors.append("New password is required.\n");
        } else if (newPassword.length() < 8) {
            errors.append("Password must be at least 8 characters.\n");
        }
        if (!newPassword.equals(confirmPassword)) {
            errors.append("Passwords do not match.\n");
        }

        if (errors.length() > 0) {
            AlertUtils.showError("Validation Error", errors.toString());
            return;
        }

        showLoading(true);

        Task<Boolean> passwordTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return userService.changePassword(currentUser.getUserId(), currentPassword, newPassword);
            }
        };

        passwordTask.setOnSucceeded(event -> Platform.runLater(() -> {
            showLoading(false);
            if (passwordTask.getValue()) {
                clearPasswordFields();
                AlertUtils.showInfo("Success", "Password changed successfully.");
            } else {
                AlertUtils.showError("Error", "Current password is incorrect.");
            }
        }));

        passwordTask.setOnFailed(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showError("Error", "Failed to change password: " + 
                passwordTask.getException().getMessage());
        }));

        executorService.submit(passwordTask);
    }

    private void clearPasswordFields() {
        if (currentPasswordField != null) currentPasswordField.clear();
        if (newPasswordField != null) newPasswordField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
    }

    // Preferences
    @FXML
    private void handleSavePreferences() {
        // Save preferences
        AlertUtils.showInfo("Success", "Preferences saved successfully.");
    }

    @FXML
    private void handleResetPreferences() {
        if (darkModeToggle != null) darkModeToggle.setSelected(false);
        if (emailNotificationsToggle != null) emailNotificationsToggle.setSelected(true);
        if (pushNotificationsToggle != null) pushNotificationsToggle.setSelected(true);
        if (languageCombo != null) languageCombo.setValue("English");

        ThemeManager.getInstance().setDarkMode(false);
        AlertUtils.showInfo("Reset", "Preferences reset to defaults.");
    }

    @FXML
    private void handleBack() {
        NavigationManager.getInstance().goBack();
    }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will need to login again to access your account.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionManager.getInstance().logout();
                NavigationManager.getInstance().navigateTo("login");
            }
        });
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
    }

    public void cleanup() {
        executorService.shutdown();
    }
}
