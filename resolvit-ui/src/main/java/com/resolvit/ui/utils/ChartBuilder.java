package com.resolvit.ui.utils;

import com.resolvit.core.entities.Complaint;
import com.resolvit.core.enums.Category;
import com.resolvit.core.enums.Priority;
import com.resolvit.core.enums.Status;
import javafx.collections.FXCollections;
import javafx.scene.chart.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for building JavaFX charts for analytics.
 * Provides consistent styling and data formatting for all chart types.
 *
 * @author ResolvIt Team
 * @version 1.0.0
 */
public final class ChartBuilder {

    // Chart colors matching theme
    private static final String PRIMARY_COLOR = "#2E7D32";
    private static final String ACCENT_COLOR = "#FF6F00";
    private static final String[] CHART_COLORS = {
            "#2E7D32", "#1976D2", "#FF6F00", "#7B1FA2",
            "#C62828", "#00838F", "#558B2F", "#6D4C41"
    };

    private ChartBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a bar chart showing complaints by category.
     *
     * @param complaints the list of complaints
     * @return configured BarChart
     */
    public static BarChart<String, Number> createCategoryBarChart(List<Complaint> complaints) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Complaints");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Complaints by Category");
        chart.setLegendVisible(false);
        chart.setAnimated(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Complaints");

        Map<Category, Long> counts = complaints.stream()
                .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));

        for (Category cat : Category.values()) {
            long count = counts.getOrDefault(cat, 0L);
            series.getData().add(new XYChart.Data<>(cat.getDisplayName(), count));
        }

        chart.getData().add(series);
        applyBarChartStyle(chart);

        return chart;
    }

    /**
     * Creates a line chart showing monthly complaint trends.
     *
     * @param complaints the list of complaints
     * @param months     number of months to show
     * @return configured LineChart
     */
    public static LineChart<String, Number> createMonthlyTrendChart(
            List<Complaint> complaints, int months) {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Complaints");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Monthly Complaint Trend");
        chart.setAnimated(true);
        chart.setCreateSymbols(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Complaints");

        // Get counts by month
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = YearMonth.from(now.minusMonths(i));
            String label = month.format(formatter);

            long count = complaints.stream()
                    .filter(c -> c.getCreatedAt() != null)
                    .filter(c -> YearMonth.from(c.getCreatedAt()).equals(month))
                    .count();

            series.getData().add(new XYChart.Data<>(label, count));
        }

        chart.getData().add(series);
        applyLineChartStyle(chart);

        return chart;
    }

    /**
     * Creates a pie chart showing status distribution.
     *
     * @param complaints the list of complaints
     * @return configured PieChart
     */
    public static PieChart createStatusPieChart(List<Complaint> complaints) {
        PieChart chart = new PieChart();
        chart.setTitle("Status Distribution");
        chart.setAnimated(true);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);

        Map<Status, Long> counts = complaints.stream()
                .collect(Collectors.groupingBy(Complaint::getStatus, Collectors.counting()));

        for (Status status : Status.values()) {
            long count = counts.getOrDefault(status, 0L);
            if (count > 0) {
                PieChart.Data slice = new PieChart.Data(
                        status.getDisplayName() + " (" + count + ")", count);
                chart.getData().add(slice);
            }
        }

        // Apply colors after adding data
        applyPieChartColors(chart);

        return chart;
    }

    /**
     * Creates a bar chart showing top departments by complaint count.
     *
     * @param departmentCounts map of department names to complaint counts
     * @param topN             number of departments to show
     * @return configured BarChart
     */
    public static BarChart<Number, String> createDepartmentBarChart(
            Map<String, Long> departmentCounts, int topN) {

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Number of Complaints");

        CategoryAxis yAxis = new CategoryAxis();
        yAxis.setLabel("Department");

        BarChart<Number, String> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Top Departments by Complaints");
        chart.setLegendVisible(false);
        chart.setAnimated(true);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Complaints");

        // Sort and take top N
        departmentCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .forEach(entry -> series.getData().add(
                        new XYChart.Data<>(entry.getValue(), entry.getKey())));

        chart.getData().add(series);

        return chart;
    }

    /**
     * Creates a stacked bar chart showing priority distribution by category.
     *
     * @param complaints the list of complaints
     * @return configured StackedBarChart
     */
    public static StackedBarChart<String, Number> createPriorityByCategory(
            List<Complaint> complaints) {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");

        StackedBarChart<String, Number> chart = new StackedBarChart<>(xAxis, yAxis);
        chart.setTitle("Priority Distribution by Category");
        chart.setAnimated(true);

        // Create a series for each priority
        for (Priority priority : Priority.values()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(priority.getDisplayName());

            for (Category category : Category.values()) {
                long count = complaints.stream()
                        .filter(c -> c.getCategory() == category)
                        .filter(c -> c.getPriority() == priority)
                        .count();

                series.getData().add(new XYChart.Data<>(category.getDisplayName(), count));
            }

            chart.getData().add(series);
        }

        return chart;
    }

    /**
     * Creates an area chart showing resolution time trends.
     *
     * @param avgResolutionByMonth map of month labels to average resolution hours
     * @return configured AreaChart
     */
    public static AreaChart<String, Number> createResolutionTimeChart(
            Map<String, Double> avgResolutionByMonth) {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Avg Resolution Time (hours)");

        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle("Average Resolution Time Trend");
        chart.setAnimated(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Resolution Time");

        avgResolutionByMonth.forEach((month, hours) ->
                series.getData().add(new XYChart.Data<>(month, hours)));

        chart.getData().add(series);

        return chart;
    }

    /**
     * Applies consistent styling to bar charts.
     */
    private static void applyBarChartStyle(BarChart<String, Number> chart) {
        chart.setStyle("-fx-background-color: transparent;");
        chart.setCategoryGap(10);
        chart.setBarGap(3);
    }

    /**
     * Applies consistent styling to line charts.
     */
    private static void applyLineChartStyle(LineChart<String, Number> chart) {
        chart.setStyle("-fx-background-color: transparent;");
    }

    /**
     * Applies colors to pie chart slices based on status.
     */
    private static void applyPieChartColors(PieChart chart) {
        int colorIndex = 0;
        for (PieChart.Data data : chart.getData()) {
            String color = CHART_COLORS[colorIndex % CHART_COLORS.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            colorIndex++;
        }
    }

    /**
     * Gets the color for a specific status.
     *
     * @param status the status
     * @return hex color string
     */
    public static String getStatusColor(Status status) {
        return status.getHexColor();
    }

    /**
     * Gets the color for a specific priority.
     *
     * @param priority the priority
     * @return hex color string
     */
    public static String getPriorityColor(Priority priority) {
        return priority.getHexColor();
    }

    /**
     * Gets the color for a specific category.
     *
     * @param category the category
     * @return hex color string
     */
    public static String getCategoryColor(Category category) {
        int index = category.ordinal() % CHART_COLORS.length;
        return CHART_COLORS[index];
    }
}
