package com.resolvit.ui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * Utility class for displaying various alert dialogs throughout the application.
 * Provides consistent styling and behavior for all dialog boxes.
 *
 * @author ResolvIt Team
 * @version 1.0.0
 */
public final class AlertHelper {

    private AlertHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Shows an information alert dialog.
     *
     * @param title   the dialog title
     * @param header  the header text (can be null)
     * @param content the content message
     */
    public static void showInfo(String title, String header, String content) {
        Alert alert = createAlert(AlertType.INFORMATION, title, header, content);
        alert.showAndWait();
    }

    /**
     * Shows an information alert with no header.
     *
     * @param title   the dialog title
     * @param content the content message
     */
    public static void showInfo(String title, String content) {
        showInfo(title, null, content);
    }

    /**
     * Shows a success alert dialog.
     *
     * @param title   the dialog title
     * @param content the success message
     */
    public static void showSuccess(String title, String content) {
        Alert alert = createAlert(AlertType.INFORMATION, title, "Success", content);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert dialog.
     *
     * @param title   the dialog title
     * @param header  the header text (can be null)
     * @param content the warning message
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = createAlert(AlertType.WARNING, title, header, content);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert with no header.
     *
     * @param title   the dialog title
     * @param content the warning message
     */
    public static void showWarning(String title, String content) {
        showWarning(title, null, content);
    }

    /**
     * Shows an error alert dialog.
     *
     * @param title   the dialog title
     * @param header  the header text (can be null)
     * @param content the error message
     */
    public static void showError(String title, String header, String content) {
        Alert alert = createAlert(AlertType.ERROR, title, header, content);
        alert.showAndWait();
    }

    /**
     * Shows an error alert with no header.
     *
     * @param title   the dialog title
     * @param content the error message
     */
    public static void showError(String title, String content) {
        showError(title, null, content);
    }

    /**
     * Shows a confirmation dialog with OK and Cancel buttons.
     *
     * @param title   the dialog title
     * @param header  the header text (can be null)
     * @param content the confirmation message
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = createAlert(AlertType.CONFIRMATION, title, header, content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows a confirmation dialog with no header.
     *
     * @param title   the dialog title
     * @param content the confirmation message
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String content) {
        return showConfirmation(title, null, content);
    }

    /**
     * Shows a confirmation dialog with custom button text.
     *
     * @param title          the dialog title
     * @param content        the confirmation message
     * @param confirmText    the text for the confirm button
     * @param cancelText     the text for the cancel button
     * @return true if the user clicked the confirm button, false otherwise
     */
    public static boolean showConfirmationWithCustomButtons(
            String title, String content, String confirmText, String cancelText) {

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        ButtonType confirmButton = new ButtonType(confirmText, ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(cancelText, ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }

    /**
     * Shows a text input dialog.
     *
     * @param title        the dialog title
     * @param header       the header text (can be null)
     * @param content      the content text
     * @param defaultValue the default input value
     * @return an Optional containing the user input, or empty if cancelled
     */
    public static Optional<String> showTextInput(
            String title, String header, String content, String defaultValue) {

        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait();
    }

    /**
     * Shows a text input dialog with no default value.
     *
     * @param title   the dialog title
     * @param content the content text
     * @return an Optional containing the user input, or empty if cancelled
     */
    public static Optional<String> showTextInput(String title, String content) {
        return showTextInput(title, null, content, "");
    }

    /**
     * Shows a logout confirmation dialog.
     *
     * @return true if the user confirms logout, false otherwise
     */
    public static boolean showLogoutConfirmation() {
        return showConfirmationWithCustomButtons(
                "Logout",
                "Are you sure you want to logout?",
                "Logout",
                "Cancel"
        );
    }

    /**
     * Shows a delete confirmation dialog.
     *
     * @param itemName the name of the item being deleted
     * @return true if the user confirms deletion, false otherwise
     */
    public static boolean showDeleteConfirmation(String itemName) {
        return showConfirmationWithCustomButtons(
                "Delete Confirmation",
                "Are you sure you want to delete " + itemName + "?\nThis action cannot be undone.",
                "Delete",
                "Cancel"
        );
    }

    /**
     * Shows an exception error dialog with details.
     *
     * @param title     the dialog title
     * @param header    the header text
     * @param exception the exception to display
     */
    public static void showException(String title, String header, Exception exception) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(exception.getMessage());

        // Create expandable content with stack trace
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);

        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        javafx.scene.layout.GridPane expContent = new javafx.scene.layout.GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new javafx.scene.control.Label("Stack trace:"), 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    /**
     * Creates a styled alert dialog.
     *
     * @param type    the alert type
     * @param title   the dialog title
     * @param header  the header text (can be null)
     * @param content the content message
     * @return the configured Alert
     */
    private static Alert createAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initStyle(StageStyle.DECORATED);
        return alert;
    }
}
