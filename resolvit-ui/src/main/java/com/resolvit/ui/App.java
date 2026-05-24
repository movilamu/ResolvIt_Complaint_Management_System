package com.resolvit.ui;

import com.resolvit.dao.connection.DatabaseInitializer;
import com.resolvit.ui.utils.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Main entry point for the ResolvIt application.
 * Initializes the JavaFX application, database connection,
 * and loads the initial login screen.
 *
 * @author ResolvIt Team
 * @version 1.0.0
 */
public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final String APP_TITLE = "ResolvIt - Complaint Management System";
    private static final int MIN_WIDTH = 1024;
    private static final int MIN_HEIGHT = 768;

    private static Stage primaryStage;
    private static Scene mainScene;

    /**
     * Application entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        log.info("Starting ResolvIt application...");
        launch(args);
    }

    /**
     * JavaFX start method - initializes the application.
     *
     * @param stage the primary stage
     */
    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        try {
            // Initialize database
            initializeDatabase();

            // Load login screen
            Parent root = loadFXML("login");

            // Create scene with default theme
            mainScene = new Scene(root, MIN_WIDTH, MIN_HEIGHT);

            // Apply default theme (light)
            ThemeManager.getInstance().applyTheme(mainScene, false);

            // Configure stage
            configureStage(stage);

            // Show the stage
            stage.setScene(mainScene);
            stage.show();

            log.info("ResolvIt application started successfully");

        } catch (Exception e) {
            log.error("Failed to start application: {}", e.getMessage(), e);
            showErrorAndExit("Failed to start application: " + e.getMessage());
        }
    }

    /**
     * Initializes the database connection and schema.
     */
    private void initializeDatabase() {
        log.info("Initializing database...");
        try {
            DatabaseInitializer.initialize();
            log.info("Database initialized successfully");
        } catch (Exception e) {
            log.error("Database initialization failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Configures the primary stage properties.
     *
     * @param stage the stage to configure
     */
    private void configureStage(Stage stage) {
        stage.setTitle(APP_TITLE);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.centerOnScreen();

        // Set application icon
        try {
            Image icon = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/assets/logo.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            log.warn("Could not load application icon: {}", e.getMessage());
        }

        // Handle close request
        stage.setOnCloseRequest(event -> {
            log.info("Application closing...");
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Loads an FXML file from the resources folder.
     *
     * @param fxmlName the name of the FXML file (without extension)
     * @return the loaded Parent node
     * @throws IOException if the FXML file cannot be loaded
     */
    public static Parent loadFXML(String fxmlName) throws IOException {
        String path = "/fxml/" + fxmlName + ".fxml";
        log.debug("Loading FXML: {}", path);
        FXMLLoader loader = new FXMLLoader(App.class.getResource(path));
        return loader.load();
    }

    /**
     * Navigates to a new screen by loading the specified FXML.
     *
     * @param fxmlName the name of the FXML file (without extension)
     */
    public static void navigateTo(String fxmlName) {
        try {
            Parent root = loadFXML(fxmlName);
            mainScene.setRoot(root);
            log.info("Navigated to: {}", fxmlName);
        } catch (IOException e) {
            log.error("Failed to navigate to {}: {}", fxmlName, e.getMessage(), e);
            showError("Navigation Error", "Could not load screen: " + fxmlName);
        }
    }

    /**
     * Navigates to a new screen with a custom controller.
     *
     * @param fxmlName   the name of the FXML file
     * @param controller the controller instance to use
     */
    public static void navigateToWithController(String fxmlName, Object controller) {
        try {
            String path = "/fxml/" + fxmlName + ".fxml";
            FXMLLoader loader = new FXMLLoader(App.class.getResource(path));
            loader.setController(controller);
            Parent root = loader.load();
            mainScene.setRoot(root);
            log.info("Navigated to: {} with custom controller", fxmlName);
        } catch (IOException e) {
            log.error("Failed to navigate to {}: {}", fxmlName, e.getMessage(), e);
            showError("Navigation Error", "Could not load screen: " + fxmlName);
        }
    }

    /**
     * Gets the primary stage.
     *
     * @return the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Gets the main scene.
     *
     * @return the main scene
     */
    public static Scene getMainScene() {
        return mainScene;
    }

    /**
     * Shows an error dialog.
     *
     * @param title   the dialog title
     * @param message the error message
     */
    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Shows an error and exits the application.
     *
     * @param message the error message
     */
    private void showErrorAndExit(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Fatal Error");
        alert.setHeaderText("Application cannot start");
        alert.setContentText(message);
        alert.showAndWait();
        Platform.exit();
        System.exit(1);
    }

    /**
     * Called when the application is stopping.
     */
    @Override
    public void stop() {
        log.info("ResolvIt application stopped");
    }
}
