package com.resolvit.ui.utils;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for handling file uploads in the application.
 * Provides file selection dialogs and file validation.
 *
 * @author ResolvIt Team
 * @version 1.0.0
 */
public final class FileUploadHelper {

    private static final Logger log = LoggerFactory.getLogger(FileUploadHelper.class);

    // Configuration constants
    private static final String UPLOAD_DIRECTORY = "uploads";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // Allowed file extensions
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".pdf"};

    private FileUploadHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Opens a file chooser dialog for selecting an attachment.
     *
     * @param stage the parent stage
     * @return Optional containing the selected file, or empty if cancelled
     */
    public static Optional<File> selectFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachment");

        // Set extension filters
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("All Supported Files", "*.jpg", "*.jpeg", "*.png", "*.pdf"),
                new ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"),
                new ExtensionFilter("PDF Documents", "*.pdf")
        );

        // Set initial directory to user's home
        File userHome = new File(System.getProperty("user.home"));
        if (userHome.exists()) {
            fileChooser.setInitialDirectory(userHome);
        }

        File selectedFile = fileChooser.showOpenDialog(stage);
        return Optional.ofNullable(selectedFile);
    }

    /**
     * Validates a file for upload.
     *
     * @param file the file to validate
     * @return validation result
     */
    public static FileValidationResult validateFile(File file) {
        if (file == null) {
            return FileValidationResult.invalid("No file selected");
        }

        if (!file.exists()) {
            return FileValidationResult.invalid("File does not exist");
        }

        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            return FileValidationResult.invalid(
                    String.format("File size exceeds maximum allowed (%.1f MB)",
                            MAX_FILE_SIZE / (1024.0 * 1024.0)));
        }

        // Check file extension
        String fileName = file.getName().toLowerCase();
        boolean validExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            return FileValidationResult.invalid(
                    "Invalid file type. Allowed: JPG, PNG, PDF");
        }

        return FileValidationResult.valid(file);
    }

    /**
     * Copies a file to the upload directory with a unique name.
     *
     * @param sourceFile the source file to copy
     * @return the path to the uploaded file, or null if failed
     */
    public static String saveUploadedFile(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("Created upload directory: {}", uploadDir);
            }

            // Generate unique filename
            String originalName = sourceFile.getName();
            String extension = getFileExtension(originalName);
            String uniqueName = UUID.randomUUID().toString() + extension;

            // Copy file to upload directory
            Path targetPath = uploadDir.resolve(uniqueName);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File uploaded: {} -> {}", originalName, targetPath);
            return targetPath.toString();

        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deletes an uploaded file.
     *
     * @param filePath the path to the file to delete
     * @return true if deletion was successful
     */
    public static boolean deleteUploadedFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Deleted file: {}", filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("Failed to delete file {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    /**
     * Gets the file extension from a filename.
     *
     * @param fileName the file name
     * @return the extension including the dot, or empty string
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return fileName.substring(lastDot);
    }

    /**
     * Checks if a file is an image based on extension.
     *
     * @param fileName the file name
     * @return true if the file is an image
     */
    public static boolean isImage(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }

    /**
     * Checks if a file is a PDF based on extension.
     *
     * @param fileName the file name
     * @return true if the file is a PDF
     */
    public static boolean isPdf(String fileName) {
        if (fileName == null) return false;
        return fileName.toLowerCase().endsWith(".pdf");
    }

    /**
     * Formats a file size for display.
     *
     * @param bytes the file size in bytes
     * @return formatted string (e.g., "1.5 MB")
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Gets the maximum allowed file size.
     *
     * @return max file size in bytes
     */
    public static long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    /**
     * Gets the maximum allowed file size formatted.
     *
     * @return max file size as formatted string
     */
    public static String getMaxFileSizeFormatted() {
        return formatFileSize(MAX_FILE_SIZE);
    }

    /**
     * Result of file validation.
     */
    public static class FileValidationResult {
        private final boolean valid;
        private final String message;
        private final File file;

        private FileValidationResult(boolean valid, String message, File file) {
            this.valid = valid;
            this.message = message;
            this.file = file;
        }

        public static FileValidationResult valid(File file) {
            return new FileValidationResult(true, "Valid file", file);
        }

        public static FileValidationResult invalid(String message) {
            return new FileValidationResult(false, message, null);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public File getFile() {
            return file;
        }
    }
}
