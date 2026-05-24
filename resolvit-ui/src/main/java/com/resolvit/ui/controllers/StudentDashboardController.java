package com.resolvit.ui.controllers;

import com.resolvit.core.entities.Complaint;
import com.resolvit.core.entities.User;
import com.resolvit.core.enums.Status;
import com.resolvit.service.factory.ServiceFactory;
import com.resolvit.service.interfaces.ComplaintService;
import com.resolvit.service.interfaces.NotificationService;
import com.resolvit.service.session.SessionManager;
import com.resolvit.ui.App;
import com.resolvit.ui.utils.DateFormatter;
import com.resolvit.ui.utils.ThemeManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the Student Dashboard screen.
 * Displays complaint statistics and recent complaints table.
 *
 * @author ResolvIt Team
 * @version 1.0.0
 */
public class StudentDashboardController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(StudentDashboardController.class);

    // Top bar
    @FXML private Button themeToggleBtn;
    @FXML private Button notificationsBtn;
    @FXML private Label notificationBadge;
    @FXML private MenuButton userMenuBtn;

    // Sidebar
    @FXML private VBox sidebar;
    @FXML private Label welcomeLabel;
    @FXML private Label userInfoLabel;
    @FXML private HBox navHome;
    @FXML private HBox navNewComplaint;
    @FXML private HBox navMyComplaints;
    @FXML private HBox navNotifications;
    @FXML private HBox navProfile;

    // Main content
    @FXML private VBox mainContent;
    @FXML private Label dashboardTitle;

    // Stats cards
    @FXML private Label totalCount;
    @FXML private Label pendingCount;
    @FXML private Label inProgressCount;
    @FXML private Label resolvedCount;

    // Complaints table
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, String> idColumn;
    @FXML private TableColumn<Complaint, String> titleColumn;
    @FXML private TableColumn<Complaint, String> categoryColumn;
    @FXML private TableColumn<Complaint, String> priorityColumn;
    @FXML private TableColumn<Complaint, String> statusColumn;
    @FXML private TableColumn<Complaint, String> dateColumn;
    @FXML private TableColumn<Complaint, Void> actionsColumn;

    private final ComplaintService complaintService;
    private final NotificationService notificationService;
    private final SessionManager sessionManager;
    private final ExecutorService executor;
    private User currentUser;

    public StudentDashboardController() {
        this.complaintService = ServiceFactory.getInstance().getComplaintService();
        this.notificationService = ServiceFactory.getInstance().getNotificationService();
        this.sessionManager = SessionManager.getInstance();
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing StudentDashboardController");

        // Get current user
        Optional<User> userOpt = sessionManager.getCurrentUser();
        if (userOpt.isEmpty()) {
            log.warn("No user session found, redirecting to login");
            App.navigateTo("login");
            return;
        }

        currentUser = userOpt.get();
        setupUserInfo();
        setupTable();
        loadData();
        loadUnreadNotificationCount();

        log.debug("StudentDashboardController initialized for user: {}", currentUser.getEmail());
    }

    /**
     * Sets up user information display.
     */
    private void setupUserInfo() {
        welcomeLabel.setText(currentUser.getWelcomeMessage());
        userInfoLabel.setText(currentUser.getEmail());
        dashboardTitle.setText(currentUser.getDashboardTitle());
        userMenuBtn.setText(currentUser.getInitials());
    }

    /**
     * Sets up the complaints table columns.
     */
    private void setupTable() {
        idColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(formatId(data.getValue().getComplaintId())));
        
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        categoryColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getCategory().getDisplayName()));
        
        priorityColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPriority().getDisplayName()));
        
        // Status column with badge styling
        statusColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    Complaint complaint = getTableView().getItems().get(getIndex());
                    badge.getStyleClass().addAll("status-badge", complaint.getStatus().getCssClass());
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        
        dateColumn.setCellValueFactory(data ->
            new SimpleStringProperty(DateFormatter.formatShort(data.getValue().getCreatedAt())));

        // Actions column with buttons
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button trackBtn = new Button("Track");
            private final HBox container = new HBox(8, viewBtn, trackBtn);

            {
                viewBtn.getStyleClass().add("btn-secondary");
                viewBtn.setStyle("-fx-padding: 4 8; -fx-font-size: 12;");
                trackBtn.getStyleClass().add("btn-primary");
                trackBtn.setStyle("-fx-padding: 4 8; -fx-font-size: 12;");

                viewBtn.setOnAction(e -> {
                    Complaint complaint = getTableView().getItems().get(getIndex());
                    viewComplaint(complaint);
                });

                trackBtn.setOnAction(e -> {
                    Complaint complaint = getTableView().getItems().get(getIndex());
                    trackComplaint(complaint);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    /**
     * Loads dashboard data.
     */
    private void loadData() {
        Task<List<Complaint>> task = new Task<>() {
            @Override
            protected List<Complaint> call() {
                return complaintService.getComplaintsByStudent(currentUser.getId());
            }
        };

        task.setOnSucceeded(e -> {
            List<Complaint> complaints = task.getValue();
            Platform.runLater(() -> {
                updateStats(complaints);
                updateTable(complaints);
            });
        });

        task.setOnFailed(e -> {
            log.error("Failed to load complaints", task.getException());
            Platform.runLater(() -> {
                App.showError("Error", "Failed to load complaints. Please try again.");
            });
        });

        executor.submit(task);
    }

    /**
     * Updates the statistics cards.
     */
    private void updateStats(List<Complaint> complaints) {
        int total = complaints.size();
        long pending = complaints.stream().filter(c -> c.getStatus() == Status.PENDING).count();
        long inProgress = complaints.stream().filter(c -> c.getStatus() == Status.IN_PROGRESS).count();
        long resolved = complaints.stream().filter(c -> c.getStatus() == Status.RESOLVED).count();

        totalCount.setText(String.valueOf(total));
        pendingCount.setText(String.valueOf(pending));
        inProgressCount.setText(String.valueOf(inProgress));
        resolvedCount.setText(String.valueOf(resolved));
    }

    /**
     * Updates the complaints table.
     */
    private void updateTable(List<Complaint> complaints) {
        ObservableList<Complaint> observableList = FXCollections.observableArrayList(complaints);
        complaintsTable.setItems(observableList);
    }

    /**
     * Loads unread notification count.
     */
    private void loadUnreadNotificationCount() {
        Task<Long> task = new Task<>() {
            @Override
            protected Long call() {
                return notificationService.getUnreadCount(currentUser.getId());
            }
        };

        task.setOnSucceeded(e -> {
            long count = task.getValue();
            Platform.runLater(() -> {
                if (count > 0) {
                    notificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                    notificationBadge.setVisible(true);
                    notificationBadge.setManaged(true);
                } else {
                    notificationBadge.setVisible(false);
                    notificationBadge.setManaged(false);
                }
            });
        });

        executor.submit(task);
    }

    /**
     * Formats complaint ID for display.
     */
    private String formatId(String id) {
        if (id == null) return "";
        return id.length() > 8 ? "CMP-" + id.substring(0, 8).toUpperCase() : "CMP-" + id.toUpperCase();
    }

    /**
     * Views complaint details.
     */
    private void viewComplaint(Complaint complaint) {
        // Store complaint ID for the tracking controller
        sessionManager.updateLastActivity();
        // Navigate to tracking screen with complaint context
        App.navigateTo("complaint_tracking");
    }

    /**
     * Tracks complaint status.
     */
    private void trackComplaint(Complaint complaint) {
        sessionManager.updateLastActivity();
        App.navigateTo("complaint_tracking");
    }

    // Navigation handlers
    @FXML
    private void onNavHome() {
        setActiveNav(navHome);
        loadData();
    }

    @FXML
    private void onNavNewComplaint() {
        App.navigateTo("complaint_form");
    }

    @FXML
    private void onNavMyComplaints() {
        setActiveNav(navMyComplaints);
        loadData();
    }

    @FXML
    private void onNavNotifications() {
        App.navigateTo("notifications");
    }

    @FXML
    private void onNavProfile() {
        App.navigateTo("profile");
    }

    @FXML
    private void onNewComplaintClick() {
        App.navigateTo("complaint_form");
    }

    @FXML
    private void onToggleTheme() {
        ThemeManager.getInstance().toggleTheme(App.getMainScene());
    }

    @FXML
    private void onNotificationsClick() {
        App.navigateTo("notifications");
    }

    @FXML
    private void onProfileClick() {
        App.navigateTo("profile");
    }

    @FXML
    private void onLogout() {
        sessionManager.destroySession();
        App.navigateTo("login");
    }

    /**
     * Sets the active navigation item.
     */
    private void setActiveNav(HBox activeNav) {
        navHome.getStyleClass().remove("sidebar-item-active");
        navNewComplaint.getStyleClass().remove("sidebar-item-active");
        navMyComplaints.getStyleClass().remove("sidebar-item-active");
        navNotifications.getStyleClass().remove("sidebar-item-active");
        navProfile.getStyleClass().remove("sidebar-item-active");

        if (!activeNav.getStyleClass().contains("sidebar-item-active")) {
            activeNav.getStyleClass().add("sidebar-item-active");
        }
    }

    /**
     * Clean up resources.
     */
    public void cleanup() {
        executor.shutdown();
    }
}
