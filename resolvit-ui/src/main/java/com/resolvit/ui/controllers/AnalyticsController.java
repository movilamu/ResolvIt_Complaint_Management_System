package com.resolvit.ui.controllers;

import com.resolvit.core.entities.Complaint;
import com.resolvit.core.enums.Category;
import com.resolvit.core.enums.Status;
import com.resolvit.service.factory.ServiceFactory;
import com.resolvit.service.interfaces.ComplaintService;
import com.resolvit.service.interfaces.UserService;
import com.resolvit.ui.animations.FadeAnimation;
import com.resolvit.ui.utils.AlertUtils;
import com.resolvit.ui.utils.NavigationManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Controller for the Analytics screen.
 * Displays charts and statistics about complaints.
 */
public class AnalyticsController implements Initializable {

    // Charts
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> categoryBarChart;
    @FXML private LineChart<String, Number> trendsLineChart;
    @FXML private BarChart<String, Number> resolutionTimeChart;

    @FXML private CategoryAxis categoryXAxis;
    @FXML private NumberAxis categoryYAxis;
    @FXML private CategoryAxis trendsXAxis;
    @FXML private NumberAxis trendsYAxis;
    @FXML private CategoryAxis resolutionXAxis;
    @FXML private NumberAxis resolutionYAxis;

    // Statistics Labels
    @FXML private Label totalComplaintsLabel;
    @FXML private Label resolvedComplaintsLabel;
    @FXML private Label pendingComplaintsLabel;
    @FXML private Label avgResolutionTimeLabel;
    @FXML private Label resolutionRateLabel;
    @FXML private Label thisMonthLabel;
    @FXML private Label lastMonthLabel;
    @FXML private Label growthRateLabel;

    // Category Stats
    @FXML private Label topCategoryLabel;
    @FXML private Label topCategoryCountLabel;
    @FXML private VBox categoryBreakdownContainer;

    // Filters
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> periodCombo;
    @FXML private ComboBox<String> categoryFilterCombo;

    // Container
    @FXML private StackPane rootPane;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ScrollPane mainScrollPane;

    private final ComplaintService complaintService;
    private final UserService userService;
    private List<Complaint> allComplaints = new ArrayList<>();
    private List<Complaint> filteredComplaints = new ArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd");
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

    public AnalyticsController() {
        this.complaintService = ServiceFactory.getInstance().getComplaintService();
        this.userService = ServiceFactory.getInstance().getUserService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        setupCharts();
        loadData();

        FadeAnimation.fadeIn(rootPane, 300);
    }

    private void setupFilters() {
        // Period combo
        if (periodCombo != null) {
            periodCombo.setItems(FXCollections.observableArrayList(
                "Last 7 Days", "Last 30 Days", "Last 90 Days", "This Year", "All Time"
            ));
            periodCombo.setValue("Last 30 Days");
            periodCombo.setOnAction(e -> {
                updateDateRange();
                applyFiltersAndRefresh();
            });
        }

        // Date pickers
        LocalDate now = LocalDate.now();
        if (startDatePicker != null) {
            startDatePicker.setValue(now.minusDays(30));
            startDatePicker.setOnAction(e -> applyFiltersAndRefresh());
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(now);
            endDatePicker.setOnAction(e -> applyFiltersAndRefresh());
        }

        // Category filter
        if (categoryFilterCombo != null) {
            categoryFilterCombo.setItems(FXCollections.observableArrayList(
                "All Categories", "Academic", "Infrastructure", "Administrative",
                "Financial", "Hostel", "Library", "IT Services", "Other"
            ));
            categoryFilterCombo.setValue("All Categories");
            categoryFilterCombo.setOnAction(e -> applyFiltersAndRefresh());
        }
    }

    private void updateDateRange() {
        if (periodCombo == null || startDatePicker == null || endDatePicker == null) return;

        String period = periodCombo.getValue();
        LocalDate now = LocalDate.now();

        switch (period) {
            case "Last 7 Days" -> {
                startDatePicker.setValue(now.minusDays(7));
                endDatePicker.setValue(now);
            }
            case "Last 30 Days" -> {
                startDatePicker.setValue(now.minusDays(30));
                endDatePicker.setValue(now);
            }
            case "Last 90 Days" -> {
                startDatePicker.setValue(now.minusDays(90));
                endDatePicker.setValue(now);
            }
            case "This Year" -> {
                startDatePicker.setValue(now.withDayOfYear(1));
                endDatePicker.setValue(now);
            }
            case "All Time" -> {
                startDatePicker.setValue(now.minusYears(5));
                endDatePicker.setValue(now);
            }
        }
    }

    private void setupCharts() {
        // Status Pie Chart
        if (statusPieChart != null) {
            statusPieChart.setTitle("Complaints by Status");
            statusPieChart.setLegendSide(Side.BOTTOM);
            statusPieChart.setLabelsVisible(true);
        }

        // Category Bar Chart
        if (categoryBarChart != null) {
            categoryBarChart.setTitle("Complaints by Category");
            categoryBarChart.setLegendVisible(false);
            if (categoryXAxis != null) categoryXAxis.setLabel("Category");
            if (categoryYAxis != null) categoryYAxis.setLabel("Count");
        }

        // Trends Line Chart
        if (trendsLineChart != null) {
            trendsLineChart.setTitle("Complaint Trends");
            trendsLineChart.setCreateSymbols(true);
            if (trendsXAxis != null) trendsXAxis.setLabel("Date");
            if (trendsYAxis != null) trendsYAxis.setLabel("Complaints");
        }

        // Resolution Time Bar Chart
        if (resolutionTimeChart != null) {
            resolutionTimeChart.setTitle("Average Resolution Time by Category");
            resolutionTimeChart.setLegendVisible(false);
            if (resolutionXAxis != null) resolutionXAxis.setLabel("Category");
            if (resolutionYAxis != null) resolutionYAxis.setLabel("Days");
        }
    }

    private void loadData() {
        showLoading(true);

        Task<List<Complaint>> loadTask = new Task<>() {
            @Override
            protected List<Complaint> call() throws Exception {
                return complaintService.getAllComplaints();
            }
        };

        loadTask.setOnSucceeded(event -> Platform.runLater(() -> {
            allComplaints = loadTask.getValue();
            applyFiltersAndRefresh();
            showLoading(false);
        }));

        loadTask.setOnFailed(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showError("Error", "Failed to load analytics data: " + 
                loadTask.getException().getMessage());
        }));

        executorService.submit(loadTask);
    }

    private void applyFiltersAndRefresh() {
        filteredComplaints = filterComplaints();
        updateStatistics();
        updateCharts();
    }

    private List<Complaint> filterComplaints() {
        LocalDate startDate = startDatePicker != null ? startDatePicker.getValue() : LocalDate.now().minusDays(30);
        LocalDate endDate = endDatePicker != null ? endDatePicker.getValue() : LocalDate.now();
        String categoryFilter = categoryFilterCombo != null ? categoryFilterCombo.getValue() : "All Categories";

        return allComplaints.stream()
            .filter(c -> {
                if (c.getSubmittedAt() == null) return false;
                LocalDate complaintDate = c.getSubmittedAt().toLocalDate();
                return !complaintDate.isBefore(startDate) && !complaintDate.isAfter(endDate);
            })
            .filter(c -> "All Categories".equals(categoryFilter) || 
                c.getCategory().getDisplayName().equalsIgnoreCase(categoryFilter))
            .collect(Collectors.toList());
    }

    private void updateStatistics() {
        int total = filteredComplaints.size();
        long resolved = filteredComplaints.stream()
            .filter(c -> c.getStatus() == Status.RESOLVED)
            .count();
        long pending = filteredComplaints.stream()
            .filter(c -> c.getStatus() == Status.SUBMITTED || 
                        c.getStatus() == Status.IN_PROGRESS ||
                        c.getStatus() == Status.UNDER_REVIEW)
            .count();

        if (totalComplaintsLabel != null) totalComplaintsLabel.setText(String.valueOf(total));
        if (resolvedComplaintsLabel != null) resolvedComplaintsLabel.setText(String.valueOf(resolved));
        if (pendingComplaintsLabel != null) pendingComplaintsLabel.setText(String.valueOf(pending));

        // Resolution rate
        double resolutionRate = total > 0 ? (double) resolved / total * 100 : 0;
        if (resolutionRateLabel != null) resolutionRateLabel.setText(String.format("%.1f%%", resolutionRate));

        // Average resolution time
        double avgResolutionTime = calculateAverageResolutionTime();
        if (avgResolutionTimeLabel != null) avgResolutionTimeLabel.setText(String.format("%.1f days", avgResolutionTime));

        // Monthly comparison
        LocalDate now = LocalDate.now();
        long thisMonth = filteredComplaints.stream()
            .filter(c -> c.getSubmittedAt() != null && 
                        c.getSubmittedAt().getMonth() == now.getMonth() &&
                        c.getSubmittedAt().getYear() == now.getYear())
            .count();
        long lastMonth = filteredComplaints.stream()
            .filter(c -> c.getSubmittedAt() != null && 
                        c.getSubmittedAt().getMonth() == now.minusMonths(1).getMonth() &&
                        c.getSubmittedAt().getYear() == now.minusMonths(1).getYear())
            .count();

        if (thisMonthLabel != null) thisMonthLabel.setText(String.valueOf(thisMonth));
        if (lastMonthLabel != null) lastMonthLabel.setText(String.valueOf(lastMonth));

        double growthRate = lastMonth > 0 ? ((double)(thisMonth - lastMonth) / lastMonth * 100) : 0;
        if (growthRateLabel != null) {
            growthRateLabel.setText(String.format("%+.1f%%", growthRate));
            growthRateLabel.getStyleClass().removeAll("text-success", "text-danger");
            growthRateLabel.getStyleClass().add(growthRate >= 0 ? "text-danger" : "text-success");
        }

        // Top category
        updateTopCategory();
    }

    private double calculateAverageResolutionTime() {
        List<Long> resolutionTimes = filteredComplaints.stream()
            .filter(c -> c.getStatus() == Status.RESOLVED && 
                        c.getSubmittedAt() != null && 
                        c.getResolvedAt() != null)
            .map(c -> ChronoUnit.DAYS.between(c.getSubmittedAt(), c.getResolvedAt()))
            .collect(Collectors.toList());

        if (resolutionTimes.isEmpty()) return 0;
        return resolutionTimes.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private void updateTopCategory() {
        Map<Category, Long> categoryCount = filteredComplaints.stream()
            .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));

        Optional<Map.Entry<Category, Long>> topCategory = categoryCount.entrySet().stream()
            .max(Map.Entry.comparingByValue());

        if (topCategory.isPresent()) {
            if (topCategoryLabel != null) topCategoryLabel.setText(topCategory.get().getKey().getDisplayName());
            if (topCategoryCountLabel != null) topCategoryCountLabel.setText(topCategory.get().getValue() + " complaints");
        }

        // Update category breakdown
        updateCategoryBreakdown(categoryCount);
    }

    private void updateCategoryBreakdown(Map<Category, Long> categoryCount) {
        if (categoryBreakdownContainer == null) return;

        categoryBreakdownContainer.getChildren().clear();

        int total = filteredComplaints.size();
        categoryCount.entrySet().stream()
            .sorted(Map.Entry.<Category, Long>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> {
                HBox row = new HBox(10);
                row.getStyleClass().add("category-row");

                Label nameLabel = new Label(entry.getKey().getDisplayName());
                nameLabel.getStyleClass().add("category-name");
                nameLabel.setPrefWidth(120);

                ProgressBar progressBar = new ProgressBar();
                double percentage = total > 0 ? (double) entry.getValue() / total : 0;
                progressBar.setProgress(percentage);
                progressBar.setPrefWidth(150);
                progressBar.getStyleClass().add("category-progress");
                HBox.setHgrow(progressBar, Priority.ALWAYS);

                Label countLabel = new Label(String.format("%d (%.0f%%)", entry.getValue(), percentage * 100));
                countLabel.getStyleClass().add("category-count");

                row.getChildren().addAll(nameLabel, progressBar, countLabel);
                categoryBreakdownContainer.getChildren().add(row);
            });
    }

    private void updateCharts() {
        updateStatusPieChart();
        updateCategoryBarChart();
        updateTrendsLineChart();
        updateResolutionTimeChart();
    }

    private void updateStatusPieChart() {
        if (statusPieChart == null) return;

        Map<Status, Long> statusCount = filteredComplaints.stream()
            .collect(Collectors.groupingBy(Complaint::getStatus, Collectors.counting()));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        statusCount.forEach((status, count) -> 
            pieData.add(new PieChart.Data(status.getDisplayName() + " (" + count + ")", count)));

        statusPieChart.setData(pieData);

        // Apply colors to pie slices
        int colorIndex = 0;
        String[] colors = {"#3b82f6", "#f59e0b", "#10b981", "#ef4444", "#6b7280"};
        for (PieChart.Data data : statusPieChart.getData()) {
            data.getNode().setStyle("-fx-pie-color: " + colors[colorIndex % colors.length] + ";");
            colorIndex++;
        }
    }

    private void updateCategoryBarChart() {
        if (categoryBarChart == null) return;

        categoryBarChart.getData().clear();

        Map<Category, Long> categoryCount = filteredComplaints.stream()
            .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Complaints");

        categoryCount.entrySet().stream()
            .sorted(Map.Entry.<Category, Long>comparingByValue().reversed())
            .forEach(entry -> 
                series.getData().add(new XYChart.Data<>(entry.getKey().getDisplayName(), entry.getValue())));

        categoryBarChart.getData().add(series);
    }

    private void updateTrendsLineChart() {
        if (trendsLineChart == null) return;

        trendsLineChart.getData().clear();

        // Group by date
        Map<LocalDate, Long> dailyCount = filteredComplaints.stream()
            .filter(c -> c.getSubmittedAt() != null)
            .collect(Collectors.groupingBy(
                c -> c.getSubmittedAt().toLocalDate(),
                TreeMap::new,
                Collectors.counting()
            ));

        XYChart.Series<String, Number> submittedSeries = new XYChart.Series<>();
        submittedSeries.setName("Submitted");

        dailyCount.forEach((date, count) -> 
            submittedSeries.getData().add(new XYChart.Data<>(date.format(dateFormatter), count)));

        // Resolved complaints trend
        Map<LocalDate, Long> resolvedDaily = filteredComplaints.stream()
            .filter(c -> c.getStatus() == Status.RESOLVED && c.getResolvedAt() != null)
            .collect(Collectors.groupingBy(
                c -> c.getResolvedAt().toLocalDate(),
                TreeMap::new,
                Collectors.counting()
            ));

        XYChart.Series<String, Number> resolvedSeries = new XYChart.Series<>();
        resolvedSeries.setName("Resolved");

        resolvedDaily.forEach((date, count) -> 
            resolvedSeries.getData().add(new XYChart.Data<>(date.format(dateFormatter), count)));

        trendsLineChart.getData().addAll(submittedSeries, resolvedSeries);
    }

    private void updateResolutionTimeChart() {
        if (resolutionTimeChart == null) return;

        resolutionTimeChart.getData().clear();

        Map<Category, Double> avgResolutionByCategory = filteredComplaints.stream()
            .filter(c -> c.getStatus() == Status.RESOLVED && 
                        c.getSubmittedAt() != null && 
                        c.getResolvedAt() != null)
            .collect(Collectors.groupingBy(
                Complaint::getCategory,
                Collectors.averagingLong(c -> 
                    ChronoUnit.DAYS.between(c.getSubmittedAt(), c.getResolvedAt()))
            ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Avg Days");

        avgResolutionByCategory.forEach((category, avgDays) -> 
            series.getData().add(new XYChart.Data<>(category.getDisplayName(), avgDays)));

        resolutionTimeChart.getData().add(series);
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleExportReport() {
        AlertUtils.showInfo("Export", "Report export functionality will be available in a future update.");
    }

    @FXML
    private void handleBack() {
        NavigationManager.getInstance().navigateTo("admin_panel");
    }

    @FXML
    private void handlePrint() {
        AlertUtils.showInfo("Print", "Print functionality will be available in a future update.");
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
