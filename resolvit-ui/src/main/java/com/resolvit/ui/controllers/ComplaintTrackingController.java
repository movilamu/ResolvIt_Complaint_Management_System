package com.resolvit.ui.controllers;

import com.resolvit.core.entities.Complaint;
import com.resolvit.core.entities.StatusUpdate;
import com.resolvit.core.entities.User;
import com.resolvit.core.enums.Status;
import com.resolvit.service.factory.ServiceFactory;
import com.resolvit.service.interfaces.ComplaintService;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for complaint tracking screen.
 * Displays complaint status timeline and history.
 */
public class ComplaintTrackingController implements Initializable {

    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, String> idColumn;
    @FXML private TableColumn<Complaint, String> titleColumn;
    @FXML private TableColumn<Complaint, String> categoryColumn;
    @FXML private TableColumn<Complaint, String> statusColumn;
    @FXML private TableColumn<Complaint, String> dateColumn;
    @FXML private TableColumn<Complaint, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> categoryFilterCombo;

    @FXML private VBox timelineContainer;
    @FXML private Label selectedComplaintTitle;
    @FXML private Label selectedComplaintId;
    @FXML private Label currentStatusLabel;
    @FXML private ProgressBar resolutionProgress;
    @FXML private Label progressPercentLabel;

    @FXML private VBox detailsPane;
    @FXML private Label complaintDescription;
    @FXML private Label submittedDate;
    @FXML private Label lastUpdatedDate;
    @FXML private Label assignedTo;

    @FXML private StackPane rootPane;
    @FXML private ProgressIndicator loadingIndicator;

    private final ComplaintService complaintService;
    private final ObservableList<Complaint> complaintsList = FXCollections.observableArrayList();
    private FilteredList<Complaint> filteredComplaints;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private Complaint selectedComplaint;

    public ComplaintTrackingController() {
        this.complaintService = ServiceFactory.getInstance().getComplaintService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        setupSearch();
        loadComplaints();

        // Apply entrance animation
        FadeAnimation.fadeIn(rootPane, 300);
    }

    private void setupTable() {
        // Configure columns
        idColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getComplaintId()));

        titleColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getTitle()));

        categoryColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getCategory().getDisplayName()));

        statusColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));

        statusColumn.setCellFactory(column -> new TableCell<>() {
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

        dateColumn.setCellValueFactory(data -> {
            if (data.getValue().getSubmittedAt() != null) {
                return new SimpleStringProperty(data.getValue().getSubmittedAt().format(dateFormatter));
            }
            return new SimpleStringProperty("N/A");
        });

        // Actions column with view button
        actionsColumn.setCellFactory(createActionsCellFactory());

        // Setup filtered list
        filteredComplaints = new FilteredList<>(complaintsList, p -> true);
        SortedList<Complaint> sortedList = new SortedList<>(filteredComplaints);
        sortedList.comparatorProperty().bind(complaintsTable.comparatorProperty());
        complaintsTable.setItems(sortedList);

        // Selection listener
        complaintsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showComplaintDetails(newSelection);
                }
            }
        );
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

    private Callback<TableColumn<Complaint, Void>, TableCell<Complaint, Void>> createActionsCellFactory() {
        return param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button refreshBtn = new Button("Refresh");
            private final HBox container = new HBox(5, viewBtn, refreshBtn);

            {
                viewBtn.getStyleClass().add("btn-primary-small");
                refreshBtn.getStyleClass().add("btn-secondary-small");
                container.setAlignment(Pos.CENTER);

                viewBtn.setOnAction(event -> {
                    Complaint complaint = getTableView().getItems().get(getIndex());
                    showComplaintDetails(complaint);
                });

                refreshBtn.setOnAction(event -> {
                    Complaint complaint = getTableView().getItems().get(getIndex());
                    refreshComplaintStatus(complaint);
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
        // Status filter
        statusFilterCombo.setItems(FXCollections.observableArrayList(
            "All Statuses", "Submitted", "In Progress", "Under Review", "Resolved", "Rejected", "Closed"
        ));
        statusFilterCombo.setValue("All Statuses");
        statusFilterCombo.setOnAction(e -> applyFilters());

        // Category filter
        categoryFilterCombo.setItems(FXCollections.observableArrayList(
            "All Categories", "Academic", "Infrastructure", "Administrative", 
            "Financial", "Hostel", "Library", "IT Services", "Other"
        ));
        categoryFilterCombo.setValue("All Categories");
        categoryFilterCombo.setOnAction(e -> applyFilters());
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String statusFilter = statusFilterCombo.getValue();
        String categoryFilter = categoryFilterCombo.getValue();

        filteredComplaints.setPredicate(complaint -> {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                complaint.getTitle().toLowerCase().contains(searchText) ||
                complaint.getComplaintId().toLowerCase().contains(searchText) ||
                complaint.getDescription().toLowerCase().contains(searchText);

            // Status filter
            boolean matchesStatus = "All Statuses".equals(statusFilter) ||
                complaint.getStatus().getDisplayName().equalsIgnoreCase(statusFilter);

            // Category filter
            boolean matchesCategory = "All Categories".equals(categoryFilter) ||
                complaint.getCategory().getDisplayName().equalsIgnoreCase(categoryFilter);

            return matchesSearch && matchesStatus && matchesCategory;
        });
    }

    private void loadComplaints() {
        showLoading(true);

        Task<List<Complaint>> loadTask = new Task<>() {
            @Override
            protected List<Complaint> call() throws Exception {
                User currentUser = SessionManager.getInstance().getCurrentUser();
                if (currentUser != null) {
                    return complaintService.getComplaintsByUserId(currentUser.getUserId());
                }
                return List.of();
            }
        };

        loadTask.setOnSucceeded(event -> Platform.runLater(() -> {
            complaintsList.clear();
            complaintsList.addAll(loadTask.getValue());
            showLoading(false);

            if (!complaintsList.isEmpty()) {
                complaintsTable.getSelectionModel().selectFirst();
            }
        }));

        loadTask.setOnFailed(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showError("Error", "Failed to load complaints: " + 
                loadTask.getException().getMessage());
        }));

        executorService.submit(loadTask);
    }

    private void showComplaintDetails(Complaint complaint) {
        this.selectedComplaint = complaint;

        // Animate details pane
        SlideAnimation.slideInFromRight(detailsPane, 200);

        // Update header info
        selectedComplaintTitle.setText(complaint.getTitle());
        selectedComplaintId.setText("ID: " + complaint.getComplaintId());
        currentStatusLabel.setText(complaint.getStatus().getDisplayName());
        currentStatusLabel.getStyleClass().clear();
        currentStatusLabel.getStyleClass().addAll("status-badge", "status-large", 
            getStatusStyleClass(complaint.getStatus().getDisplayName()));

        // Update progress
        double progress = calculateProgress(complaint.getStatus());
        resolutionProgress.setProgress(progress);
        progressPercentLabel.setText(String.format("%.0f%%", progress * 100));

        // Update details
        complaintDescription.setText(complaint.getDescription());
        submittedDate.setText(complaint.getSubmittedAt() != null ? 
            complaint.getSubmittedAt().format(dateFormatter) : "N/A");
        lastUpdatedDate.setText(complaint.getUpdatedAt() != null ? 
            complaint.getUpdatedAt().format(dateFormatter) : "N/A");
        assignedTo.setText(complaint.getAssignedTo() != null ? 
            complaint.getAssignedTo() : "Not yet assigned");

        // Build timeline
        buildTimeline(complaint);
    }

    private double calculateProgress(Status status) {
        return switch (status) {
            case SUBMITTED -> 0.2;
            case IN_PROGRESS -> 0.5;
            case UNDER_REVIEW -> 0.7;
            case RESOLVED -> 1.0;
            case REJECTED, CLOSED -> 1.0;
            default -> 0.1;
        };
    }

    private void buildTimeline(Complaint complaint) {
        timelineContainer.getChildren().clear();

        List<StatusUpdate> history = complaint.getStatusHistory();
        if (history == null || history.isEmpty()) {
            // Show at least the current status
            addTimelineItem("Submitted", complaint.getSubmittedAt() != null ? 
                complaint.getSubmittedAt().format(dateFormatter) : "Unknown", 
                "Complaint was submitted", true, true);
            return;
        }

        for (int i = 0; i < history.size(); i++) {
            StatusUpdate update = history.get(i);
            boolean isFirst = (i == 0);
            boolean isLast = (i == history.size() - 1);
            boolean isCurrent = isLast;

            addTimelineItem(
                update.getStatus().getDisplayName(),
                update.getUpdatedAt().format(dateFormatter),
                update.getComment() != null ? update.getComment() : "",
                isCurrent,
                isFirst
            );
        }
    }

    private void addTimelineItem(String status, String date, String comment, 
                                  boolean isCurrent, boolean isFirst) {
        HBox itemContainer = new HBox(15);
        itemContainer.setAlignment(Pos.TOP_LEFT);
        itemContainer.getStyleClass().add("timeline-item");

        // Timeline indicator
        VBox indicatorBox = new VBox();
        indicatorBox.setAlignment(Pos.TOP_CENTER);
        indicatorBox.setPrefWidth(30);

        Circle circle = new Circle(8);
        circle.getStyleClass().add(isCurrent ? "timeline-circle-current" : "timeline-circle");
        if (isCurrent) {
            circle.setFill(Color.web("#2563eb"));
        } else {
            circle.setFill(Color.web("#94a3b8"));
        }

        Region line = new Region();
        line.setPrefWidth(2);
        line.setPrefHeight(40);
        line.getStyleClass().add("timeline-line");
        VBox.setVgrow(line, Priority.ALWAYS);

        indicatorBox.getChildren().add(circle);
        if (!isFirst) {
            // Add line above for non-first items
        }

        // Content
        VBox contentBox = new VBox(5);
        contentBox.getStyleClass().add("timeline-content");

        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add(isCurrent ? "timeline-status-current" : "timeline-status");

        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("timeline-date");

        contentBox.getChildren().addAll(statusLabel, dateLabel);

        if (comment != null && !comment.isEmpty()) {
            Label commentLabel = new Label(comment);
            commentLabel.getStyleClass().add("timeline-comment");
            commentLabel.setWrapText(true);
            contentBox.getChildren().add(commentLabel);
        }

        itemContainer.getChildren().addAll(indicatorBox, contentBox);
        timelineContainer.getChildren().add(itemContainer);

        // Add animation
        FadeAnimation.fadeIn(itemContainer, 200);
    }

    private void refreshComplaintStatus(Complaint complaint) {
        showLoading(true);

        Task<Complaint> refreshTask = new Task<>() {
            @Override
            protected Complaint call() throws Exception {
                return complaintService.getComplaintById(complaint.getComplaintId());
            }
        };

        refreshTask.setOnSucceeded(event -> Platform.runLater(() -> {
            Complaint updated = refreshTask.getValue();
            if (updated != null) {
                // Update in list
                int index = complaintsList.indexOf(complaint);
                if (index >= 0) {
                    complaintsList.set(index, updated);
                }
                // Refresh details if this is selected
                if (complaint.equals(selectedComplaint)) {
                    showComplaintDetails(updated);
                }
            }
            showLoading(false);
            AlertUtils.showInfo("Refreshed", "Complaint status has been refreshed.");
        }));

        refreshTask.setOnFailed(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showError("Error", "Failed to refresh status: " + 
                refreshTask.getException().getMessage());
        }));

        executorService.submit(refreshTask);
    }

    @FXML
    private void handleRefreshAll() {
        loadComplaints();
    }

    @FXML
    private void handleNewComplaint() {
        NavigationManager.getInstance().navigateTo("complaint_form");
    }

    @FXML
    private void handleBack() {
        NavigationManager.getInstance().navigateTo("student_dashboard");
    }

    @FXML
    private void handleExportHistory() {
        if (selectedComplaint == null) {
            AlertUtils.showWarning("No Selection", "Please select a complaint to export its history.");
            return;
        }

        // Export complaint history (would typically open file chooser)
        AlertUtils.showInfo("Export", "Export functionality will be available in a future update.");
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
    }

    /**
     * Cleanup resources when controller is destroyed.
     */
    public void cleanup() {
        executorService.shutdown();
    }
}
