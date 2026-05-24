package com.resolvit.ui.controllers;

import com.resolvit.core.entities.Notification;
import com.resolvit.core.entities.User;
import com.resolvit.core.enums.NotificationType;
import com.resolvit.service.factory.ServiceFactory;
import com.resolvit.service.interfaces.NotificationService;
import com.resolvit.service.session.SessionManager;
import com.resolvit.ui.animations.FadeAnimation;
import com.resolvit.ui.animations.SlideAnimation;
import com.resolvit.ui.utils.AlertUtils;
import com.resolvit.ui.utils.NavigationManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the Notifications screen.
 * Displays and manages user notifications.
 */
public class NotificationController implements Initializable {

    @FXML private ListView<Notification> notificationListView;
    @FXML private VBox notificationContainer;

    @FXML private ComboBox<String> filterCombo;
    @FXML private Button markAllReadBtn;
    @FXML private Button clearAllBtn;

    @FXML private Label totalCountLabel;
    @FXML private Label unreadCountLabel;

    @FXML private VBox detailPanel;
    @FXML private Label detailTitle;
    @FXML private Label detailDate;
    @FXML private Label detailType;
    @FXML private TextArea detailMessage;
    @FXML private Button viewRelatedBtn;

    @FXML private StackPane rootPane;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label emptyStateLabel;

    private final NotificationService notificationService;
    private final ObservableList<Notification> notificationsList = FXCollections.observableArrayList();
    private FilteredList<Notification> filteredNotifications;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private Notification selectedNotification;

    public NotificationController() {
        this.notificationService = ServiceFactory.getInstance().getNotificationService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilter();
        setupListView();
        loadNotifications();

        FadeAnimation.fadeIn(rootPane, 300);
    }

    private void setupFilter() {
        if (filterCombo != null) {
            filterCombo.setItems(FXCollections.observableArrayList(
                "All Notifications", "Unread Only", "Status Updates", "System Messages", "Reminders"
            ));
            filterCombo.setValue("All Notifications");
            filterCombo.setOnAction(e -> applyFilter());
        }
    }

    private void setupListView() {
        if (notificationListView != null) {
            filteredNotifications = new FilteredList<>(notificationsList, p -> true);
            notificationListView.setItems(filteredNotifications);

            notificationListView.setCellFactory(listView -> new NotificationCell());

            notificationListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        showNotificationDetails(newVal);
                    }
                }
            );
        }
    }

    private void applyFilter() {
        if (filteredNotifications == null || filterCombo == null) return;

        String filter = filterCombo.getValue();
        filteredNotifications.setPredicate(notification -> {
            if ("All Notifications".equals(filter)) return true;
            if ("Unread Only".equals(filter)) return !notification.isRead();
            if ("Status Updates".equals(filter)) return notification.getType() == NotificationType.STATUS_UPDATE;
            if ("System Messages".equals(filter)) return notification.getType() == NotificationType.SYSTEM;
            if ("Reminders".equals(filter)) return notification.getType() == NotificationType.REMINDER;
            return true;
        });

        updateEmptyState();
    }

    private void loadNotifications() {
        showLoading(true);

        Task<List<Notification>> loadTask = new Task<>() {
            @Override
            protected List<Notification> call() throws Exception {
                User currentUser = SessionManager.getInstance().getCurrentUser();
                if (currentUser != null) {
                    return notificationService.getNotificationsByUserId(currentUser.getUserId());
                }
                return List.of();
            }
        };

        loadTask.setOnSucceeded(event -> Platform.runLater(() -> {
            notificationsList.clear();
            notificationsList.addAll(loadTask.getValue());
            updateCounts();
            updateEmptyState();
            showLoading(false);
        }));

        loadTask.setOnFailed(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showError("Error", "Failed to load notifications: " + 
                loadTask.getException().getMessage());
        }));

        executorService.submit(loadTask);
    }

    private void updateCounts() {
        int total = notificationsList.size();
        long unread = notificationsList.stream().filter(n -> !n.isRead()).count();

        if (totalCountLabel != null) totalCountLabel.setText(String.valueOf(total));
        if (unreadCountLabel != null) unreadCountLabel.setText(String.valueOf(unread));
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredNotifications == null || filteredNotifications.isEmpty();
        if (emptyStateLabel != null) {
            emptyStateLabel.setVisible(isEmpty);
            emptyStateLabel.setManaged(isEmpty);
        }
        if (notificationListView != null) {
            notificationListView.setVisible(!isEmpty);
        }
    }

    private void showNotificationDetails(Notification notification) {
        this.selectedNotification = notification;

        // Mark as read if unread
        if (!notification.isRead()) {
            markAsRead(notification);
        }

        if (detailPanel != null) {
            SlideAnimation.slideInFromRight(detailPanel, 200);
            detailPanel.setVisible(true);
        }

        if (detailTitle != null) detailTitle.setText(notification.getTitle());
        if (detailDate != null && notification.getCreatedAt() != null) {
            detailDate.setText(notification.getCreatedAt().format(dateFormatter));
        }
        if (detailType != null) detailType.setText(notification.getType().getDisplayName());
        if (detailMessage != null) detailMessage.setText(notification.getMessage());

        // Show/hide related button based on notification type
        if (viewRelatedBtn != null) {
            boolean hasRelated = notification.getRelatedComplaintId() != null;
            viewRelatedBtn.setVisible(hasRelated);
            viewRelatedBtn.setManaged(hasRelated);
        }
    }

    private void markAsRead(Notification notification) {
        Task<Boolean> markReadTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return notificationService.markAsRead(notification.getNotificationId());
            }
        };

        markReadTask.setOnSucceeded(event -> Platform.runLater(() -> {
            if (markReadTask.getValue()) {
                notification.setRead(true);
                notificationListView.refresh();
                updateCounts();
            }
        }));

        executorService.submit(markReadTask);
    }

    @FXML
    private void handleMarkAllRead() {
        List<Notification> unreadNotifications = notificationsList.stream()
            .filter(n -> !n.isRead())
            .toList();

        if (unreadNotifications.isEmpty()) {
            AlertUtils.showInfo("Info", "All notifications are already read.");
            return;
        }

        Task<Boolean> markAllTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                for (Notification n : unreadNotifications) {
                    notificationService.markAsRead(n.getNotificationId());
                }
                return true;
            }
        };

        markAllTask.setOnSucceeded(event -> Platform.runLater(() -> {
            unreadNotifications.forEach(n -> n.setRead(true));
            notificationListView.refresh();
            updateCounts();
            AlertUtils.showInfo("Success", "All notifications marked as read.");
        }));

        markAllTask.setOnFailed(event -> Platform.runLater(() -> 
            AlertUtils.showError("Error", "Failed to mark notifications as read.")));

        executorService.submit(markAllTask);
    }

    @FXML
    private void handleClearAll() {
        if (notificationsList.isEmpty()) {
            AlertUtils.showInfo("Info", "No notifications to clear.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All Notifications");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will permanently delete all your notifications.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearAllNotifications();
            }
        });
    }

    private void clearAllNotifications() {
        showLoading(true);

        Task<Boolean> clearTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                User currentUser = SessionManager.getInstance().getCurrentUser();
                if (currentUser != null) {
                    return notificationService.clearAllNotifications(currentUser.getUserId());
                }
                return false;
            }
        };

        clearTask.setOnSucceeded(event -> Platform.runLater(() -> {
            showLoading(false);
            if (clearTask.getValue()) {
                notificationsList.clear();
                updateCounts();
                updateEmptyState();
                hideDetailPanel();
                AlertUtils.showInfo("Success", "All notifications cleared.");
            }
        }));

        clearTask.setOnFailed(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showError("Error", "Failed to clear notifications.");
        }));

        executorService.submit(clearTask);
    }

    @FXML
    private void handleViewRelated() {
        if (selectedNotification == null || selectedNotification.getRelatedComplaintId() == null) {
            return;
        }

        // Navigate to complaint tracking with the related complaint
        NavigationManager.getInstance().navigateTo("complaint_tracking", 
            selectedNotification.getRelatedComplaintId());
    }

    @FXML
    private void handleDeleteSelected() {
        if (selectedNotification == null) {
            AlertUtils.showWarning("No Selection", "Please select a notification to delete.");
            return;
        }

        Task<Boolean> deleteTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return notificationService.deleteNotification(selectedNotification.getNotificationId());
            }
        };

        deleteTask.setOnSucceeded(event -> Platform.runLater(() -> {
            if (deleteTask.getValue()) {
                notificationsList.remove(selectedNotification);
                selectedNotification = null;
                hideDetailPanel();
                updateCounts();
                updateEmptyState();
            }
        }));

        deleteTask.setOnFailed(event -> Platform.runLater(() -> 
            AlertUtils.showError("Error", "Failed to delete notification.")));

        executorService.submit(deleteTask);
    }

    private void hideDetailPanel() {
        if (detailPanel != null) {
            detailPanel.setVisible(false);
        }
    }

    @FXML
    private void handleRefresh() {
        loadNotifications();
    }

    @FXML
    private void handleBack() {
        NavigationManager.getInstance().goBack();
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
    }

    public void cleanup() {
        executorService.shutdown();
    }

    /**
     * Custom ListCell for displaying notifications.
     */
    private class NotificationCell extends ListCell<Notification> {
        private final HBox container;
        private final VBox contentBox;
        private final Circle indicator;
        private final Label titleLabel;
        private final Label messageLabel;
        private final Label dateLabel;
        private final Label typeLabel;

        public NotificationCell() {
            container = new HBox(12);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(12, 16, 12, 16));
            container.getStyleClass().add("notification-cell");

            indicator = new Circle(6);
            indicator.getStyleClass().add("notification-indicator");

            contentBox = new VBox(4);
            HBox.setHgrow(contentBox, Priority.ALWAYS);

            HBox headerBox = new HBox(8);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            titleLabel = new Label();
            titleLabel.getStyleClass().add("notification-title");
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            typeLabel = new Label();
            typeLabel.getStyleClass().add("notification-type-badge");

            headerBox.getChildren().addAll(titleLabel, typeLabel);

            messageLabel = new Label();
            messageLabel.getStyleClass().add("notification-message");
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(400);

            dateLabel = new Label();
            dateLabel.getStyleClass().add("notification-date");

            contentBox.getChildren().addAll(headerBox, messageLabel, dateLabel);
            container.getChildren().addAll(indicator, contentBox);
        }

        @Override
        protected void updateItem(Notification notification, boolean empty) {
            super.updateItem(notification, empty);

            if (empty || notification == null) {
                setGraphic(null);
            } else {
                titleLabel.setText(notification.getTitle());
                messageLabel.setText(truncateMessage(notification.getMessage(), 100));
                dateLabel.setText(formatRelativeTime(notification.getCreatedAt()));
                typeLabel.setText(notification.getType().getDisplayName());

                // Update indicator based on read status
                if (notification.isRead()) {
                    indicator.setFill(Color.TRANSPARENT);
                    indicator.setStroke(Color.web("#94a3b8"));
                    container.getStyleClass().remove("notification-unread");
                } else {
                    indicator.setFill(Color.web("#3b82f6"));
                    indicator.setStroke(Color.TRANSPARENT);
                    if (!container.getStyleClass().contains("notification-unread")) {
                        container.getStyleClass().add("notification-unread");
                    }
                }

                // Type badge color
                typeLabel.getStyleClass().removeAll("type-status", "type-system", "type-reminder", "type-alert");
                switch (notification.getType()) {
                    case STATUS_UPDATE -> typeLabel.getStyleClass().add("type-status");
                    case SYSTEM -> typeLabel.getStyleClass().add("type-system");
                    case REMINDER -> typeLabel.getStyleClass().add("type-reminder");
                    case ALERT -> typeLabel.getStyleClass().add("type-alert");
                }

                setGraphic(container);
            }
        }

        private String truncateMessage(String message, int maxLength) {
            if (message == null) return "";
            if (message.length() <= maxLength) return message;
            return message.substring(0, maxLength - 3) + "...";
        }

        private String formatRelativeTime(LocalDateTime dateTime) {
            if (dateTime == null) return "";

            LocalDateTime now = LocalDateTime.now();
            long minutes = ChronoUnit.MINUTES.between(dateTime, now);

            if (minutes < 1) return "Just now";
            if (minutes < 60) return minutes + " min ago";

            long hours = ChronoUnit.HOURS.between(dateTime, now);
            if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";

            long days = ChronoUnit.DAYS.between(dateTime, now);
            if (days < 7) return days + " day" + (days > 1 ? "s" : "") + " ago";

            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        }
    }
}
