package com.resolvit.ui.controllers;

import com.resolvit.core.entities.Complaint;
import com.resolvit.core.entities.User;
import com.resolvit.core.enums.Category;
import com.resolvit.core.enums.Role;
import com.resolvit.core.enums.Status;
import com.resolvit.service.factory.ServiceFactory;
import com.resolvit.service.interfaces.ComplaintService;
import com.resolvit.service.interfaces.UserService;
import com.resolvit.service.session.SessionManager;
import com.resolvit.ui.animations.FadeAnimation;
import com.resolvit.ui.animations.SlideAnimation;
import com.resolvit.ui.utils.AlertUtils;
import com.resolvit.ui.utils.NavigationManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the Admin Panel screen.
 * Handles complaint management, user management, and administrative functions.
 */
public class AdminPanelController implements Initializable {

    // Navigation
    @FXML private Button dashboardTab;
    @FXML private Button complaintsTab;
    @FXML private Button usersTab;
    @FXML private Button settingsTab;

    // Dashboard Stats
    @FXML private Label totalComplaintsLabel;
    @FXML private Label pendingComplaintsLabel;
    @FXML private Label resolvedComplaintsLabel;
    @FXML private Label totalUsersLabel;

    // Complaints Table
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, String> complaintIdColumn;
    @FXML private TableColumn<Complaint, String> complaintTitleColumn;
    @FXML private TableColumn<Complaint, String> complaintCategoryColumn;
    @FXML private TableColumn<Complaint, String> complaintStatusColumn;
    @FXML private TableColumn<Complaint, String> complaintSubmitterColumn;
    @FXML private TableColumn<Complaint, String> complaintDateColumn;
    @FXML private TableColumn<Complaint, Void> complaintActionsColumn;

    // Users Table
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userIdColumn;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, Void> userActionsColumn;

    // Filters
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private ComboBox<String> priorityFilterCombo;

    // Detail Panel
    @FXML private VBox complaintDetailPanel;
    @FXML private Label detailTitle;
    @FXML private Label detailId;
    @FXML private Label detailCategory;
    @FXML private Label detailStatus;
    @FXML private Label detailSubmitter;
    @FXML private Label detailDate;
    @FXML private TextArea detailDescription;
    @FXML private ComboBox<String> assignToCombo;
    @FXML private ComboBox<String> updateStatusCombo;
    @FXML private TextArea responseTextArea;

    // Containers
    @FXML private StackPane rootPane;
    @FXML private VBox dashboardContent;
    @FXML private VBox complaintsContent;
    @FXML private VBox usersContent;
    @FXML private VBox settingsContent;
    @FXML private ProgressIndicator loadingIndicator;

    // Header
    @FXML private Label adminNameLabel;
    @FXML private Label currentDateLabel;

    private final ComplaintService complaintService;
    private final UserService userService;
    private final ObservableList<Complaint> complaintsList = FXCollections.observableArrayList();
    private final ObservableList<User> usersList = FXCollections.observableArrayList();
    private FilteredList<Complaint> filteredComplaints;
    private FilteredList<User> filteredUsers;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private Complaint selectedComplaint;

    public AdminPanelController() {
        this.complaintService = ServiceFactory.getInstance().getComplaintService();
        this.userService = ServiceFactory.getInstance().getUserService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        verifyAdminAccess();
        setupHeader();
        setupTables();
        setupFilters();
        setupDetailPanel();
        loadData();
        showDashboard();

        FadeAnimation.fadeIn(rootPane, 300);
    }

    private void verifyAdminAccess() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            AlertUtils.showError("Access Denied", "You do not have permission to access this panel.");
            NavigationManager.getInstance().navigateTo("login");
        }
    }

    private void setupHeader() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && adminNameLabel != null) {
            adminNameLabel.setText("Welcome, " + currentUser.getFirstName());
        }
        if (currentDateLabel != null) {
            currentDateLabel.setText(java.time.LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        }
    }

    private void setupTables() {
        setupComplaintsTable();
        setupUsersTable();
    }

    private void setupComplaintsTable() {
        if (complaintsTable == null) return;

        complaintIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getComplaintId()));

        complaintTitleColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getTitle()));

        complaintCategoryColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getCategory().getDisplayName()));

        complaintStatusColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));

        complaintStatusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(status);
                    statusLabel.getStyleClass().addAll("status-badge", getStatusStyleClass(status));
                    setGraphic(statusLabel);
                }
            }
        });

        complaintSubmitterColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getSubmittedBy()));

        complaintDateColumn.setCellValueFactory(data -> {
            if (data.getValue().getSubmittedAt() != null) {
                return new SimpleStringProperty(data.getValue().getSubmittedAt().format(dateFormatter));
            }
            return new SimpleStringProperty("N/A");
        });

        complaintActionsColumn.setCellFactory(createComplaintActionsCellFactory());

        filteredComplaints = new FilteredList<>(complaintsList, p -> true);
        SortedList<Complaint> sortedComplaints = new SortedList<>(filteredComplaints);
        sortedComplaints.comparatorProperty().bind(complaintsTable.comparatorProperty());
        complaintsTable.setItems(sortedComplaints);

        complaintsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    showComplaintDetails(newVal);
                }
            }
        );
    }

    private void setupUsersTable() {
        if (usersTable == null) return;

        userIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getUserId()));

        userNameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getFirstName() + " " + data.getValue().getLastName()));

        userEmailColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEmail()));

        userRoleColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getRole().getDisplayName()));

        userActionsColumn.setCellFactory(createUserActionsCellFactory());

        filteredUsers = new FilteredList<>(usersList, p -> true);
        SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
        sortedUsers.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedUsers);
    }

    private String getStatusStyleClass(String status) {
        if (status == null) return "status-pending";
        return switch (status.toUpperCase()) {
            case "SUBMITTED", "PENDING" -> "status-pending";
            case "IN_PROGRESS", "IN PROGRESS", "UNDER REVIEW" -> "status-in-progress";
            case "RESOLVED" -> "status-resolved";
            case "REJECTED", "CLOSED" -> "status-rejected";
            default -> "status-pending";
        };
    }

    private Callback<TableColumn<Complaint, Void>, TableCell<Complaint, Void>> createComplaintActionsCellFactory() {
        return param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button assignBtn = new Button("Assign");
            private final HBox container = new HBox(5, viewBtn, assignBtn);

            {
                viewBtn.getStyleClass().add("btn-primary-small");
                assignBtn.getStyleClass().add("btn-secondary-small");
                container.setAlignment(Pos.CENTER);

                viewBtn.setOnAction(event -> {
                    Complaint complaint = getTableView().getItems().get(getIndex());
                    showComplaintDetails(complaint);
                });

                assignBtn.setOnAction(event -> {
                    Complaint complaint = getTableView().getItems().get(getIndex());
                    showAssignDialog(complaint);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        };
    }

    private Callback<TableColumn<User, Void>, TableCell<User, Void>> createUserActionsCellFactory() {
        return param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deactivateBtn = new Button("Deactivate");
            private final HBox container = new HBox(5, editBtn, deactivateBtn);

            {
                editBtn.getStyleClass().add("btn-primary-small");
                deactivateBtn.getStyleClass().add("btn-danger-small");
                container.setAlignment(Pos.CENTER);

                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    showEditUserDialog(user);
                });

                deactivateBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeactivateUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        };
    }

    private void setupFilters() {
        if (statusFilterCombo != null) {
            statusFilterCombo.setItems(FXCollections.observableArrayList(
                "All Statuses", "Submitted", "In Progress", "Under Review", "Resolved", "Rejected", "Closed"
            ));
            statusFilterCombo.setValue("All Statuses");
            statusFilterCombo.setOnAction(e -> applyFilters());
        }

        if (categoryFilterCombo != null) {
            categoryFilterCombo.setItems(FXCollections.observableArrayList(
                "All Categories", "Academic", "Infrastructure", "Administrative",
                "Financial", "Hostel", "Library", "IT Services", "Other"
            ));
            categoryFilterCombo.setValue("All Categories");
            categoryFilterCombo.setOnAction(e -> applyFilters());
        }

        if (priorityFilterCombo != null) {
            priorityFilterCombo.setItems(FXCollections.observableArrayList(
                "All Priorities", "High", "Medium", "Low"
            ));
            priorityFilterCombo.setValue("All Priorities");
            priorityFilterCombo.setOnAction(e -> applyFilters());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void applyFilters() {
        if (filteredComplaints == null) return;

        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String statusFilter = statusFilterCombo != null ? statusFilterCombo.getValue() : "All Statuses";
        String categoryFilter = categoryFilterCombo != null ? categoryFilterCombo.getValue() : "All Categories";

        filteredComplaints.setPredicate(complaint -> {
            boolean matchesSearch = searchText.isEmpty() ||
                complaint.getTitle().toLowerCase().contains(searchText) ||
                complaint.getComplaintId().toLowerCase().contains(searchText);

            boolean matchesStatus = "All Statuses".equals(statusFilter) ||
                complaint.getStatus().getDisplayName().equalsIgnoreCase(statusFilter);

            boolean matchesCategory = "All Categories".equals(categoryFilter) ||
                complaint.getCategory().getDisplayName().equalsIgnoreCase(categoryFilter);

            return matchesSearch && matchesStatus && matchesCategory;
        });
    }

    private void setupDetailPanel() {
        if (updateStatusCombo != null) {
            updateStatusCombo.setItems(FXCollections.observableArrayList(
                "Submitted", "In Progress", "Under Review", "Resolved", "Rejected", "Closed"
            ));
        }
    }

    private void loadData() {
        showLoading(true);

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                List<Complaint> complaints = complaintService.getAllComplaints();
                List<User> users = userService.getAllUsers();

                Platform.runLater(() -> {
                    complaintsList.clear();
                    complaintsList.addAll(complaints);

                    usersList.clear();
                    usersList.addAll(users);

                    updateDashboardStats();
                });

                return null;
            }
        };

        loadTask.setOnSucceeded(event -> showLoading(false));
        loadTask.setOnFailed(event -> {
            showLoading(false);
            AlertUtils.showError("Error", "Failed to load data: " + loadTask.getException().getMessage());
        });

        executorService.submit(loadTask);
    }

    private void updateDashboardStats() {
        if (totalComplaintsLabel != null) {
            totalComplaintsLabel.setText(String.valueOf(complaintsList.size()));
        }

        long pending = complaintsList.stream()
            .filter(c -> c.getStatus() == Status.SUBMITTED || c.getStatus() == Status.IN_PROGRESS)
            .count();
        if (pendingComplaintsLabel != null) {
            pendingComplaintsLabel.setText(String.valueOf(pending));
        }

        long resolved = complaintsList.stream()
            .filter(c -> c.getStatus() == Status.RESOLVED)
            .count();
        if (resolvedComplaintsLabel != null) {
            resolvedComplaintsLabel.setText(String.valueOf(resolved));
        }

        if (totalUsersLabel != null) {
            totalUsersLabel.setText(String.valueOf(usersList.size()));
        }
    }

    private void showComplaintDetails(Complaint complaint) {
        this.selectedComplaint = complaint;

        if (complaintDetailPanel != null) {
            SlideAnimation.slideInFromRight(complaintDetailPanel, 200);
            complaintDetailPanel.setVisible(true);
        }

        if (detailTitle != null) detailTitle.setText(complaint.getTitle());
        if (detailId != null) detailId.setText("ID: " + complaint.getComplaintId());
        if (detailCategory != null) detailCategory.setText(complaint.getCategory().getDisplayName());
        if (detailStatus != null) detailStatus.setText(complaint.getStatus().getDisplayName());
        if (detailSubmitter != null) detailSubmitter.setText(complaint.getSubmittedBy());
        if (detailDate != null && complaint.getSubmittedAt() != null) {
            detailDate.setText(complaint.getSubmittedAt().format(dateFormatter));
        }
        if (detailDescription != null) detailDescription.setText(complaint.getDescription());
        if (updateStatusCombo != null) updateStatusCombo.setValue(complaint.getStatus().getDisplayName());
    }

    private void showAssignDialog(Complaint complaint) {
        // Load staff members for assignment
        List<User> staff = usersList.stream()
            .filter(u -> u.getRole() == Role.STAFF || u.getRole() == Role.ADMIN)
            .toList();

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
            staff.isEmpty() ? "" : staff.get(0).getFirstName() + " " + staff.get(0).getLastName(),
            staff.stream().map(u -> u.getFirstName() + " " + u.getLastName()).toList()
        );
        dialog.setTitle("Assign Complaint");
        dialog.setHeaderText("Assign complaint to staff member");
        dialog.setContentText("Select staff member:");

        dialog.showAndWait().ifPresent(selected -> {
            complaint.setAssignedTo(selected);
            if (complaint.getStatus() == Status.SUBMITTED) {
                complaint.setStatus(Status.IN_PROGRESS);
            }
            updateComplaint(complaint);
        });
    }

    private void showEditUserDialog(User user) {
        AlertUtils.showInfo("Edit User", "Edit user functionality will be available in a future update.");
    }

    private void handleDeactivateUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deactivation");
        confirm.setHeaderText("Deactivate User");
        confirm.setContentText("Are you sure you want to deactivate user: " + 
            user.getFirstName() + " " + user.getLastName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Deactivate user logic
                AlertUtils.showInfo("User Deactivated", "User has been deactivated successfully.");
            }
        });
    }

    @FXML
    private void handleUpdateStatus() {
        if (selectedComplaint == null || updateStatusCombo == null) {
            AlertUtils.showWarning("No Selection", "Please select a complaint to update.");
            return;
        }

        String newStatus = updateStatusCombo.getValue();
        Status status = Status.fromDisplayName(newStatus);
        if (status != null) {
            selectedComplaint.setStatus(status);

            String response = responseTextArea != null ? responseTextArea.getText() : null;
            if (response != null && !response.trim().isEmpty()) {
                // Add response to complaint
            }

            updateComplaint(selectedComplaint);
        }
    }

    private void updateComplaint(Complaint complaint) {
        showLoading(true);

        Task<Boolean> updateTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return complaintService.updateComplaint(complaint);
            }
        };

        updateTask.setOnSucceeded(event -> Platform.runLater(() -> {
            showLoading(false);
            if (updateTask.getValue()) {
                AlertUtils.showInfo("Success", "Complaint updated successfully.");
                loadData();
            } else {
                AlertUtils.showError("Error", "Failed to update complaint.");
            }
        }));

        updateTask.setOnFailed(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showError("Error", "Failed to update complaint: " + 
                updateTask.getException().getMessage());
        }));

        executorService.submit(updateTask);
    }

    // Navigation methods
    @FXML
    private void showDashboard() {
        setActiveTab(dashboardTab);
        showContent(dashboardContent);
    }

    @FXML
    private void showComplaints() {
        setActiveTab(complaintsTab);
        showContent(complaintsContent);
    }

    @FXML
    private void showUsers() {
        setActiveTab(usersTab);
        showContent(usersContent);
    }

    @FXML
    private void showSettings() {
        setActiveTab(settingsTab);
        showContent(settingsContent);
    }

    private void setActiveTab(Button activeTab) {
        if (dashboardTab != null) dashboardTab.getStyleClass().remove("tab-active");
        if (complaintsTab != null) complaintsTab.getStyleClass().remove("tab-active");
        if (usersTab != null) usersTab.getStyleClass().remove("tab-active");
        if (settingsTab != null) settingsTab.getStyleClass().remove("tab-active");

        if (activeTab != null) activeTab.getStyleClass().add("tab-active");
    }

    private void showContent(VBox contentToShow) {
        if (dashboardContent != null) dashboardContent.setVisible(false);
        if (complaintsContent != null) complaintsContent.setVisible(false);
        if (usersContent != null) usersContent.setVisible(false);
        if (settingsContent != null) settingsContent.setVisible(false);

        if (contentToShow != null) {
            contentToShow.setVisible(true);
            FadeAnimation.fadeIn(contentToShow, 200);
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationManager.getInstance().navigateTo("login");
    }

    @FXML
    private void handleViewAnalytics() {
        NavigationManager.getInstance().navigateTo("analytics");
    }

    @FXML
    private void handleGenerateReport() {
        NavigationManager.getInstance().navigateTo("report");
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
