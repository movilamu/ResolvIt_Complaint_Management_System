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
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Controller for the Report generation screen.
 * Generates and exports reports on complaint data.
 */
public class ReportController implements Initializable {

    // Report Type Selection
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> formatCombo;

    // Report Preview
    @FXML private VBox reportPreviewContainer;
    @FXML private Label reportTitleLabel;
    @FXML private Label reportDateLabel;
    @FXML private Label reportPeriodLabel;

    // Summary Section
    @FXML private Label summaryTotalLabel;
    @FXML private Label summaryResolvedLabel;
    @FXML private Label summaryPendingLabel;
    @FXML private Label summaryAvgTimeLabel;

    // Data Table
    @FXML private TableView<Complaint> reportTable;
    @FXML private TableColumn<Complaint, String> tableIdColumn;
    @FXML private TableColumn<Complaint, String> tableTitleColumn;
    @FXML private TableColumn<Complaint, String> tableCategoryColumn;
    @FXML private TableColumn<Complaint, String> tableStatusColumn;
    @FXML private TableColumn<Complaint, String> tableDateColumn;

    // Category Breakdown
    @FXML private VBox categoryBreakdownContainer;

    // Status Breakdown
    @FXML private VBox statusBreakdownContainer;

    // Action Buttons
    @FXML private Button generateBtn;
    @FXML private Button exportBtn;
    @FXML private Button printBtn;
    @FXML private Button scheduleBtn;

    // Container
    @FXML private StackPane rootPane;
    @FXML private ScrollPane reportScrollPane;
    @FXML private ProgressIndicator loadingIndicator;

    private final ComplaintService complaintService;
    private final UserService userService;
    private List<Complaint> reportData = new ArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public ReportController() {
        this.complaintService = ServiceFactory.getInstance().getComplaintService();
        this.userService = ServiceFactory.getInstance().getUserService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupDatePickers();
        setupTable();
        setDefaultValues();

        FadeAnimation.fadeIn(rootPane, 300);
    }

    private void setupComboBoxes() {
        // Report Type
        if (reportTypeCombo != null) {
            reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Summary Report",
                "Detailed Report",
                "Category Analysis",
                "Status Analysis",
                "Resolution Time Report",
                "Trend Analysis"
            ));
            reportTypeCombo.setValue("Summary Report");
        }

        // Category Filter
        if (categoryFilterCombo != null) {
            categoryFilterCombo.setItems(FXCollections.observableArrayList(
                "All Categories", "Academic", "Infrastructure", "Administrative",
                "Financial", "Hostel", "Library", "IT Services", "Other"
            ));
            categoryFilterCombo.setValue("All Categories");
        }

        // Status Filter
        if (statusFilterCombo != null) {
            statusFilterCombo.setItems(FXCollections.observableArrayList(
                "All Statuses", "Submitted", "In Progress", "Under Review", 
                "Resolved", "Rejected", "Closed"
            ));
            statusFilterCombo.setValue("All Statuses");
        }

        // Export Format
        if (formatCombo != null) {
            formatCombo.setItems(FXCollections.observableArrayList(
                "PDF", "CSV", "Excel", "HTML"
            ));
            formatCombo.setValue("PDF");
        }
    }

    private void setupDatePickers() {
        LocalDate now = LocalDate.now();
        if (startDatePicker != null) {
            startDatePicker.setValue(now.minusMonths(1));
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(now);
        }
    }

    private void setupTable() {
        if (reportTable == null) return;

        tableIdColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getComplaintId()));

        tableTitleColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));

        tableCategoryColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory().getDisplayName()));

        tableStatusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().getDisplayName()));

        tableDateColumn.setCellValueFactory(data -> {
            if (data.getValue().getSubmittedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    data.getValue().getSubmittedAt().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
    }

    private void setDefaultValues() {
        // Initialize with empty state
        if (reportPreviewContainer != null) {
            reportPreviewContainer.setVisible(false);
        }
    }

    @FXML
    private void handleGenerateReport() {
        if (!validateReportParams()) {
            return;
        }

        showLoading(true);

        Task<List<Complaint>> generateTask = new Task<>() {
            @Override
            protected List<Complaint> call() throws Exception {
                return fetchReportData();
            }
        };

        generateTask.setOnSucceeded(event -> Platform.runLater(() -> {
            reportData = generateTask.getValue();
            displayReport();
            showLoading(false);

            if (reportData.isEmpty()) {
                AlertUtils.showInfo("No Data", "No complaints found for the selected criteria.");
            }
        }));

        generateTask.setOnFailed(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showError("Error", "Failed to generate report: " + 
                generateTask.getException().getMessage());
        }));

        executorService.submit(generateTask);
    }

    private boolean validateReportParams() {
        LocalDate startDate = startDatePicker != null ? startDatePicker.getValue() : null;
        LocalDate endDate = endDatePicker != null ? endDatePicker.getValue() : null;

        if (startDate == null || endDate == null) {
            AlertUtils.showError("Validation Error", "Please select start and end dates.");
            return false;
        }

        if (startDate.isAfter(endDate)) {
            AlertUtils.showError("Validation Error", "Start date cannot be after end date.");
            return false;
        }

        return true;
    }

    private List<Complaint> fetchReportData() throws Exception {
        List<Complaint> allComplaints = complaintService.getAllComplaints();

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String categoryFilter = categoryFilterCombo != null ? categoryFilterCombo.getValue() : "All Categories";
        String statusFilter = statusFilterCombo != null ? statusFilterCombo.getValue() : "All Statuses";

        return allComplaints.stream()
            .filter(c -> {
                if (c.getSubmittedAt() == null) return false;
                LocalDate complaintDate = c.getSubmittedAt().toLocalDate();
                return !complaintDate.isBefore(startDate) && !complaintDate.isAfter(endDate);
            })
            .filter(c -> "All Categories".equals(categoryFilter) || 
                c.getCategory().getDisplayName().equalsIgnoreCase(categoryFilter))
            .filter(c -> "All Statuses".equals(statusFilter) || 
                c.getStatus().getDisplayName().equalsIgnoreCase(statusFilter))
            .collect(Collectors.toList());
    }

    private void displayReport() {
        if (reportPreviewContainer != null) {
            reportPreviewContainer.setVisible(true);
            FadeAnimation.fadeIn(reportPreviewContainer, 300);
        }

        // Update header
        String reportType = reportTypeCombo != null ? reportTypeCombo.getValue() : "Report";
        if (reportTitleLabel != null) reportTitleLabel.setText(reportType);
        if (reportDateLabel != null) {
            reportDateLabel.setText("Generated: " + LocalDateTime.now().format(dateTimeFormatter));
        }
        if (reportPeriodLabel != null) {
            reportPeriodLabel.setText("Period: " + 
                startDatePicker.getValue().format(dateFormatter) + " - " + 
                endDatePicker.getValue().format(dateFormatter));
        }

        // Update summary
        updateSummary();

        // Update table
        if (reportTable != null) {
            reportTable.setItems(FXCollections.observableArrayList(reportData));
        }

        // Update breakdowns
        updateCategoryBreakdown();
        updateStatusBreakdown();

        // Enable export buttons
        if (exportBtn != null) exportBtn.setDisable(false);
        if (printBtn != null) printBtn.setDisable(false);
    }

    private void updateSummary() {
        int total = reportData.size();
        long resolved = reportData.stream()
            .filter(c -> c.getStatus() == Status.RESOLVED)
            .count();
        long pending = reportData.stream()
            .filter(c -> c.getStatus() != Status.RESOLVED && 
                        c.getStatus() != Status.REJECTED && 
                        c.getStatus() != Status.CLOSED)
            .count();

        if (summaryTotalLabel != null) summaryTotalLabel.setText(String.valueOf(total));
        if (summaryResolvedLabel != null) summaryResolvedLabel.setText(String.valueOf(resolved));
        if (summaryPendingLabel != null) summaryPendingLabel.setText(String.valueOf(pending));

        // Calculate average resolution time
        double avgTime = reportData.stream()
            .filter(c -> c.getStatus() == Status.RESOLVED && 
                        c.getSubmittedAt() != null && 
                        c.getResolvedAt() != null)
            .mapToLong(c -> java.time.temporal.ChronoUnit.DAYS.between(
                c.getSubmittedAt(), c.getResolvedAt()))
            .average()
            .orElse(0);

        if (summaryAvgTimeLabel != null) {
            summaryAvgTimeLabel.setText(String.format("%.1f days", avgTime));
        }
    }

    private void updateCategoryBreakdown() {
        if (categoryBreakdownContainer == null) return;

        categoryBreakdownContainer.getChildren().clear();

        Map<Category, Long> categoryCount = reportData.stream()
            .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));

        int total = reportData.size();

        categoryCount.entrySet().stream()
            .sorted(Map.Entry.<Category, Long>comparingByValue().reversed())
            .forEach(entry -> {
                HBox row = createBreakdownRow(
                    entry.getKey().getDisplayName(),
                    entry.getValue(),
                    total
                );
                categoryBreakdownContainer.getChildren().add(row);
            });
    }

    private void updateStatusBreakdown() {
        if (statusBreakdownContainer == null) return;

        statusBreakdownContainer.getChildren().clear();

        Map<Status, Long> statusCount = reportData.stream()
            .collect(Collectors.groupingBy(Complaint::getStatus, Collectors.counting()));

        int total = reportData.size();

        statusCount.entrySet().stream()
            .sorted(Map.Entry.<Status, Long>comparingByValue().reversed())
            .forEach(entry -> {
                HBox row = createBreakdownRow(
                    entry.getKey().getDisplayName(),
                    entry.getValue(),
                    total
                );
                statusBreakdownContainer.getChildren().add(row);
            });
    }

    private HBox createBreakdownRow(String label, long count, int total) {
        HBox row = new HBox(10);
        row.getStyleClass().add("breakdown-row");

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("breakdown-name");
        nameLabel.setPrefWidth(120);

        ProgressBar progressBar = new ProgressBar();
        double percentage = total > 0 ? (double) count / total : 0;
        progressBar.setProgress(percentage);
        progressBar.setPrefWidth(150);
        progressBar.getStyleClass().add("breakdown-progress");
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        Label countLabel = new Label(String.format("%d (%.0f%%)", count, percentage * 100));
        countLabel.getStyleClass().add("breakdown-count");

        row.getChildren().addAll(nameLabel, progressBar, countLabel);
        return row;
    }

    @FXML
    private void handleExportReport() {
        if (reportData.isEmpty()) {
            AlertUtils.showWarning("No Data", "Please generate a report first.");
            return;
        }

        String format = formatCombo != null ? formatCombo.getValue() : "CSV";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName("complaint_report_" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        switch (format) {
            case "CSV" -> {
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                fileChooser.setInitialFileName(fileChooser.getInitialFileName() + ".csv");
            }
            case "PDF" -> {
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                fileChooser.setInitialFileName(fileChooser.getInitialFileName() + ".pdf");
            }
            case "Excel" -> {
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
                fileChooser.setInitialFileName(fileChooser.getInitialFileName() + ".xlsx");
            }
            case "HTML" -> {
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("HTML Files", "*.html"));
                fileChooser.setInitialFileName(fileChooser.getInitialFileName() + ".html");
            }
        }

        File selectedFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (selectedFile != null) {
            exportToFile(selectedFile, format);
        }
    }

    private void exportToFile(File file, String format) {
        showLoading(true);

        Task<Boolean> exportTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                switch (format) {
                    case "CSV" -> exportToCsv(file);
                    case "HTML" -> exportToHtml(file);
                    default -> {
                        // For PDF and Excel, show info that these require additional libraries
                        Platform.runLater(() -> 
                            AlertUtils.showInfo("Export", 
                                format + " export requires additional libraries. Exporting as CSV instead."));
                        exportToCsv(new File(file.getPath().replace(".pdf", ".csv")
                            .replace(".xlsx", ".csv")));
                    }
                }
                return true;
            }
        };

        exportTask.setOnSucceeded(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showInfo("Export Complete", "Report exported successfully to:\n" + file.getAbsolutePath());
        }));

        exportTask.setOnFailed(event -> Platform.runLater(() -> {
            showLoading(false);
            AlertUtils.showError("Export Failed", "Failed to export report: " + 
                exportTask.getException().getMessage());
        }));

        executorService.submit(exportTask);
    }

    private void exportToCsv(File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Header
            writer.println("ID,Title,Category,Status,Submitted By,Date Submitted,Date Resolved,Description");

            // Data
            for (Complaint c : reportData) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    escapeCSV(c.getComplaintId()),
                    escapeCSV(c.getTitle()),
                    escapeCSV(c.getCategory().getDisplayName()),
                    escapeCSV(c.getStatus().getDisplayName()),
                    escapeCSV(c.getSubmittedBy()),
                    c.getSubmittedAt() != null ? c.getSubmittedAt().format(dateFormatter) : "",
                    c.getResolvedAt() != null ? c.getResolvedAt().format(dateFormatter) : "",
                    escapeCSV(c.getDescription())
                );
            }
        }
    }

    private void exportToHtml(File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html><head><title>Complaint Report</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial, sans-serif; margin: 40px; }");
            writer.println("h1 { color: #2563eb; }");
            writer.println("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
            writer.println("th { background-color: #2563eb; color: white; }");
            writer.println("tr:nth-child(even) { background-color: #f9fafb; }");
            writer.println(".summary { display: flex; gap: 20px; margin: 20px 0; }");
            writer.println(".stat { background: #f3f4f6; padding: 20px; border-radius: 8px; }");
            writer.println(".stat-value { font-size: 24px; font-weight: bold; color: #2563eb; }");
            writer.println("</style></head><body>");

            writer.println("<h1>" + (reportTypeCombo != null ? reportTypeCombo.getValue() : "Report") + "</h1>");
            writer.println("<p>Generated: " + LocalDateTime.now().format(dateTimeFormatter) + "</p>");
            writer.println("<p>Period: " + startDatePicker.getValue().format(dateFormatter) + 
                " - " + endDatePicker.getValue().format(dateFormatter) + "</p>");

            // Summary
            long resolved = reportData.stream().filter(c -> c.getStatus() == Status.RESOLVED).count();
            writer.println("<div class='summary'>");
            writer.println("<div class='stat'><div>Total Complaints</div><div class='stat-value'>" + 
                reportData.size() + "</div></div>");
            writer.println("<div class='stat'><div>Resolved</div><div class='stat-value'>" + 
                resolved + "</div></div>");
            writer.println("<div class='stat'><div>Pending</div><div class='stat-value'>" + 
                (reportData.size() - resolved) + "</div></div>");
            writer.println("</div>");

            // Table
            writer.println("<table>");
            writer.println("<tr><th>ID</th><th>Title</th><th>Category</th><th>Status</th><th>Date</th></tr>");

            for (Complaint c : reportData) {
                writer.printf("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>%n",
                    escapeHtml(c.getComplaintId()),
                    escapeHtml(c.getTitle()),
                    escapeHtml(c.getCategory().getDisplayName()),
                    escapeHtml(c.getStatus().getDisplayName()),
                    c.getSubmittedAt() != null ? c.getSubmittedAt().format(dateFormatter) : ""
                );
            }

            writer.println("</table></body></html>");
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    @FXML
    private void handlePrintReport() {
        if (reportPreviewContainer == null || reportData.isEmpty()) {
            AlertUtils.showWarning("No Data", "Please generate a report first.");
            return;
        }

        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null) {
            boolean proceed = printerJob.showPrintDialog(rootPane.getScene().getWindow());
            if (proceed) {
                // Scale content to fit page
                PageLayout pageLayout = printerJob.getJobSettings().getPageLayout();
                double scaleX = pageLayout.getPrintableWidth() / reportPreviewContainer.getBoundsInParent().getWidth();
                double scaleY = pageLayout.getPrintableHeight() / reportPreviewContainer.getBoundsInParent().getHeight();
                double scale = Math.min(scaleX, scaleY);

                Scale printScale = new Scale(scale, scale);
                reportPreviewContainer.getTransforms().add(printScale);

                boolean printed = printerJob.printPage(reportPreviewContainer);
                
                reportPreviewContainer.getTransforms().remove(printScale);

                if (printed) {
                    printerJob.endJob();
                    AlertUtils.showInfo("Print", "Report sent to printer.");
                } else {
                    AlertUtils.showError("Print Failed", "Failed to print the report.");
                }
            }
        } else {
            AlertUtils.showError("Print Error", "No printer available.");
        }
    }

    @FXML
    private void handleScheduleReport() {
        AlertUtils.showInfo("Schedule Report", 
            "Report scheduling functionality will be available in a future update.");
    }

    @FXML
    private void handleBack() {
        NavigationManager.getInstance().navigateTo("admin_panel");
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
