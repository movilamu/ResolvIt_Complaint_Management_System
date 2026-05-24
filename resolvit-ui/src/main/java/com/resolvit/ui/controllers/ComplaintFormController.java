package com.resolvit.ui.controllers;

import com.resolvit.core.entities.Complaint;
import com.resolvit.core.entities.User;
import com.resolvit.core.enums.Category;
import com.resolvit.core.enums.Priority;
import com.resolvit.core.exceptions.DuplicateComplaintException;
import com.resolvit.service.factory.ServiceFactory;
import com.resolvit.service.interfaces.ComplaintService;
import com.resolvit.service.session.SessionManager;
import com.resolvit.ui.App;
import com.resolvit.ui.animations.ShakeAnimation;
import com.resolvit.ui.utils.AlertHelper;
import com.resolvit.ui.utils.FileUploadHelper;
import com.resolvit.ui.utils.ValidationHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the Complaint Form screen.
 * Handles new complaint submission with validation.
 *
 * @author ResolvIt Team
 * @version 1.0.0
 */
public class ComplaintFormController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(ComplaintFormController.class);
    private static final int TITLE_MIN_LENGTH = 10;
    private static final int TITLE_MAX_LENGTH = 200;
    private static final int DESC_MIN_LENGTH = 30;
    private static final int DESC_MAX_LENGTH = 1000;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // FXML fields
    @FXML private Button backBtn;
    @FXML private TextField titleField;
    @FXML private Label titleCharCount;
    @FXML private Label titleError;
    @FXML private TextArea descriptionField;
    @FXML private Label descCharCount;
    @FXML private Label descriptionError;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private Label categoryError;
    @FXML private ComboBox<Priority> priorityComboBox;
    @FXML private Label priorityError;
    @FXML private CheckBox anonymousCheckbox;
    @FXML private VBox dropZone;
    @FXML private HBox filePreview;
    @FXML private Label fileName;
    @FXML private Label fileSize;
    @FXML private Button removeFileBtn;
    @FXML private Label fileError;
    @FXML private HBox errorContainer;
    @FXML private Label errorLabel;
    @FXML private Button submitButton;
    @FXML private HBox loadingContainer;

    private final ComplaintService complaintService;
    private final SessionManager sessionManager;
    private final ExecutorService executor;
    private File selectedFile;
    private User currentUser;

    public ComplaintFormController() {
        this.complaintService = ServiceFactory.getInstance().getComplaintService();
        this.sessionManager = SessionManager.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing ComplaintFormController");

        // Check session
        Optional<User> userOpt = sessionManager.getCurrentUser();
        if (userOpt.isEmpty()) {
            App.navigateTo("login");
            return;
        }
        currentUser = userOpt.get();

        // Setup combo boxes
        setupComboBoxes();

        // Setup character counters
        setupCharacterCounters();

        // Setup validation listeners
        setupValidation();

        // Hide errors
        hideAllErrors();

        log.debug("ComplaintFormController initialized");
    }

    /**
     * Sets up category and priority combo boxes.
     */
    private void setupComboBoxes() {
        // Categories
        categoryComboBox.setItems(FXCollections.observableArrayList(Category.values()));
        categoryComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Category cat) {
                return cat != null ? cat.getDisplayWithIcon() : "";
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        });

        // Priorities
        priorityComboBox.setItems(FXCollections.observableArrayList(Priority.values()));
        priorityComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Priority pri) {
                return pri != null ? pri.getDisplayName() : "";
            }

            @Override
            public Priority fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Sets up character counters for text fields.
     */
    private void setupCharacterCounters() {
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            int len = newVal.length();
            titleCharCount.setText(len + "/" + TITLE_MAX_LENGTH);

            if (len > TITLE_MAX_LENGTH) {
                titleField.setText(oldVal);
                titleCharCount.getStyleClass().add("char-counter-error");
            } else if (len > TITLE_MAX_LENGTH - 20) {
                titleCharCount.getStyleClass().add("char-counter-warning");
            } else {
                titleCharCount.getStyleClass().removeAll("char-counter-warning", "char-counter-error");
            }

            validateForm();
        });

        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
            int len = newVal.length();
            descCharCount.setText(len + "/" + DESC_MAX_LENGTH);

            if (len > DESC_MAX_LENGTH) {
                descriptionField.setText(oldVal);
                descCharCount.getStyleClass().add("char-counter-error");
            } else if (len > DESC_MAX_LENGTH - 50) {
                descCharCount.getStyleClass().add("char-counter-warning");
            } else {
                descCharCount.getStyleClass().removeAll("char-counter-warning", "char-counter-error");
            }

            validateForm();
        });
    }

    /**
     * Sets up real-time validation.
     */
    private void setupValidation() {
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        priorityComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    /**
     * Validates the entire form and enables/disables submit button.
     */
    private void validateForm() {
        boolean valid = true;

        // Title validation
        String title = titleField.getText().trim();
        if (title.length() < TITLE_MIN_LENGTH) {
            valid = false;
        }

        // Description validation
        String desc = descriptionField.getText().trim();
        if (desc.length() < DESC_MIN_LENGTH) {
            valid = false;
        }

        // Category validation
        if (categoryComboBox.getValue() == null) {
            valid = false;
        }

        // Priority validation
        if (priorityComboBox.getValue() == null) {
            valid = false;
        }

        submitButton.setDisable(!valid);
    }

    /**
     * Validates fields on submission.
     */
    private boolean validateOnSubmit() {
        boolean valid = true;

        // Title
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showFieldError(titleError, "Title is required");
            valid = false;
        } else if (title.length() < TITLE_MIN_LENGTH) {
            showFieldError(titleError, "Title must be at least " + TITLE_MIN_LENGTH + " characters");
            valid = false;
        } else {
            hideError(titleError);
        }

        // Description
        String desc = descriptionField.getText().trim();
        if (desc.isEmpty()) {
            showFieldError(descriptionError, "Description is required");
            valid = false;
        } else if (desc.length() < DESC_MIN_LENGTH) {
            showFieldError(descriptionError, "Description must be at least " + DESC_MIN_LENGTH + " characters");
            valid = false;
        } else {
            hideError(descriptionError);
        }

        // Category
        if (categoryComboBox.getValue() == null) {
            showFieldError(categoryError, "Please select a category");
            valid = false;
        } else {
            hideError(categoryError);
        }

        // Priority
        if (priorityComboBox.getValue() == null) {
            showFieldError(priorityError, "Please select a priority");
            valid = false;
        } else {
            hideError(priorityError);
        }

        return valid;
    }

    /**
     * Handles form submission.
     */
    @FXML
    private void onSubmitClick() {
        hideGeneralError();

        if (!validateOnSubmit()) {
            ShakeAnimation.shake(submitButton.getParent());
            return;
        }

        setLoadingState(true);

        // Create complaint
        Complaint complaint = new Complaint();
        complaint.setTitle(titleField.getText().trim());
        complaint.setDescription(descriptionField.getText().trim());
        complaint.setCategory(categoryComboBox.getValue());
        complaint.setPriority(priorityComboBox.getValue());
        complaint.setAnonymous(anonymousCheckbox.isSelected());

        String attachmentPath = selectedFile != null ? selectedFile.getAbsolutePath() : null;
        if (attachmentPath != null) {
            complaint.setAttachmentPath(attachmentPath);
        }

        // Submit in background
        Task<Complaint> submitTask = new Task<>() {
            @Override
            protected Complaint call() throws Exception {
                return complaintService.createComplaint(complaint, currentUser.getId());
            }
        };

        submitTask.setOnSucceeded(e -> {
            Complaint created = submitTask.getValue();
            Platform.runLater(() -> {
                setLoadingState(false);
                AlertHelper.showInfo("Complaint Submitted",
                        "Your complaint has been submitted successfully.\n" +
                        "Tracking Number: " + created.getTrackingNumber());
                App.navigateTo("student_dashboard");
            });
        });

        submitTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoadingState(false);
                Throwable ex = submitTask.getException();

                if (ex instanceof DuplicateComplaintException) {
                    showGeneralError(ex.getMessage());
                } else {
                    showGeneralError("Failed to submit complaint. Please try again.");
                    log.error("Complaint submission failed", ex);
                }
            });
        });

        executor.submit(submitTask);
    }

    /**
     * Handles back button click.
     */
    @FXML
    private void onBackClick() {
        App.navigateTo("student_dashboard");
    }

    // File handling methods
    @FXML
    private void onDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
            dropZone.getStyleClass().add("drop-zone-active");
        }
        event.consume();
    }

    @FXML
    private void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            File file = db.getFiles().get(0);
            handleFileSelection(file);
            success = true;
        }

        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    private void onDragExited(DragEvent event) {
        dropZone.getStyleClass().remove("drop-zone-active");
    }

    @FXML
    private void onBrowseClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachment");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("All Supported", "*.jpg", "*.jpeg", "*.png", "*.pdf")
        );

        File file = fileChooser.showOpenDialog(App.getPrimaryStage());
        if (file != null) {
            handleFileSelection(file);
        }
    }

    private void handleFileSelection(File file) {
        hideError(fileError);

        // Validate file type
        String extension = FileUploadHelper.getFileExtension(file.getName()).toLowerCase();
        if (!Arrays.asList("jpg", "jpeg", "png", "pdf").contains(extension)) {
            showFieldError(fileError, "Only JPG, PNG, and PDF files are allowed");
            return;
        }

        // Validate file size
        if (file.length() > MAX_FILE_SIZE) {
            showFieldError(fileError, "File size must not exceed 5MB");
            return;
        }

        selectedFile = file;
        fileName.setText(file.getName());
        fileSize.setText(FileUploadHelper.formatFileSize(file.length()));

        dropZone.setVisible(false);
        dropZone.setManaged(false);
        filePreview.setVisible(true);
        filePreview.setManaged(true);
    }

    @FXML
    private void onRemoveFile() {
        selectedFile = null;
        filePreview.setVisible(false);
        filePreview.setManaged(false);
        dropZone.setVisible(true);
        dropZone.setManaged(true);
    }

    // Helper methods
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
        hideError(titleError);
        hideError(descriptionError);
        hideError(categoryError);
        hideError(priorityError);
        hideError(fileError);
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
        submitButton.setDisable(loading);
        submitButton.setVisible(!loading);
        submitButton.setManaged(!loading);
        loadingContainer.setVisible(loading);
        loadingContainer.setManaged(loading);
    }

    public void cleanup() {
        executor.shutdown();
    }
}
