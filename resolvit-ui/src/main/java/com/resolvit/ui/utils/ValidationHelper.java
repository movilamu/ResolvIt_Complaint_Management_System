package com.resolvit.ui.utils;

import java.util.regex.Pattern;

/**
 * Utility class for validating user input in UI forms.
 * Provides real-time validation feedback for various field types.
 *
 * @author ResolvIt Team
 * @version 1.0.0
 */
public final class ValidationHelper {

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[0-9]{10,15}$"
    );
    private static final Pattern ROLL_NUMBER_PATTERN = Pattern.compile(
            "^[A-Za-z0-9-]{3,20}$"
    );

    // Validation constants
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final int MIN_TITLE_LENGTH = 10;
    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MIN_DESCRIPTION_LENGTH = 30;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

    private ValidationHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates an email address format.
     *
     * @param email the email to validate
     * @return validation result
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return ValidationResult.invalid("Email is required");
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.invalid("Please enter a valid email address");
        }
        return ValidationResult.valid();
    }

    /**
     * Validates a password for strength requirements.
     *
     * @param password the password to validate
     * @return validation result with strength indicator
     */
    public static PasswordValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordValidationResult(false, "Password is required", PasswordStrength.WEAK);
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return new PasswordValidationResult(false,
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters",
                    PasswordStrength.WEAK);
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            return new PasswordValidationResult(false,
                    "Password must not exceed " + MAX_PASSWORD_LENGTH + " characters",
                    PasswordStrength.WEAK);
        }

        // Calculate strength
        PasswordStrength strength = calculatePasswordStrength(password);

        return new PasswordValidationResult(true, "Password is valid", strength);
    }

    /**
     * Calculates password strength based on complexity.
     *
     * @param password the password to evaluate
     * @return password strength level
     */
    public static PasswordStrength calculatePasswordStrength(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return PasswordStrength.WEAK;
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        boolean isLong = password.length() >= 12;

        int score = 0;
        if (hasUppercase) score++;
        if (hasLowercase) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;
        if (isLong) score++;

        if (score >= 4) return PasswordStrength.STRONG;
        if (score >= 2) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }

    /**
     * Validates that two passwords match.
     *
     * @param password        the original password
     * @param confirmPassword the confirmation password
     * @return validation result
     */
    public static ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return ValidationResult.invalid("Please confirm your password");
        }
        if (!password.equals(confirmPassword)) {
            return ValidationResult.invalid("Passwords do not match");
        }
        return ValidationResult.valid();
    }

    /**
     * Validates a user's name.
     *
     * @param name the name to validate
     * @return validation result
     */
    public static ValidationResult validateName(String name) {
        if (name == null || name.isBlank()) {
            return ValidationResult.invalid("Name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() < MIN_NAME_LENGTH) {
            return ValidationResult.invalid("Name must be at least " + MIN_NAME_LENGTH + " characters");
        }
        if (trimmed.length() > MAX_NAME_LENGTH) {
            return ValidationResult.invalid("Name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
        return ValidationResult.valid();
    }

    /**
     * Validates a phone number.
     *
     * @param phone the phone number to validate
     * @return validation result
     */
    public static ValidationResult validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return ValidationResult.valid(); // Phone is optional
        }
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (!PHONE_PATTERN.matcher(digitsOnly).matches()) {
            return ValidationResult.invalid("Please enter a valid phone number (10-15 digits)");
        }
        return ValidationResult.valid();
    }

    /**
     * Validates a roll number.
     *
     * @param rollNumber the roll number to validate
     * @return validation result
     */
    public static ValidationResult validateRollNumber(String rollNumber) {
        if (rollNumber == null || rollNumber.isBlank()) {
            return ValidationResult.invalid("Roll number is required");
        }
        if (!ROLL_NUMBER_PATTERN.matcher(rollNumber.trim()).matches()) {
            return ValidationResult.invalid("Please enter a valid roll number (3-20 alphanumeric characters)");
        }
        return ValidationResult.valid();
    }

    /**
     * Validates a complaint title.
     *
     * @param title the title to validate
     * @return validation result
     */
    public static ValidationResult validateTitle(String title) {
        if (title == null || title.isBlank()) {
            return ValidationResult.invalid("Title is required");
        }
        String trimmed = title.trim();
        if (trimmed.length() < MIN_TITLE_LENGTH) {
            return ValidationResult.invalid(
                    "Title must be at least " + MIN_TITLE_LENGTH + " characters (" +
                            trimmed.length() + "/" + MIN_TITLE_LENGTH + ")");
        }
        if (trimmed.length() > MAX_TITLE_LENGTH) {
            return ValidationResult.invalid(
                    "Title must not exceed " + MAX_TITLE_LENGTH + " characters");
        }
        return ValidationResult.valid();
    }

    /**
     * Validates a complaint description.
     *
     * @param description the description to validate
     * @return validation result
     */
    public static ValidationResult validateDescription(String description) {
        if (description == null || description.isBlank()) {
            return ValidationResult.invalid("Description is required");
        }
        String trimmed = description.trim();
        if (trimmed.length() < MIN_DESCRIPTION_LENGTH) {
            return ValidationResult.invalid(
                    "Description must be at least " + MIN_DESCRIPTION_LENGTH + " characters (" +
                            trimmed.length() + "/" + MIN_DESCRIPTION_LENGTH + ")");
        }
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            return ValidationResult.invalid(
                    "Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        return ValidationResult.valid();
    }

    /**
     * Validates a file for upload (type and size).
     *
     * @param fileName the file name
     * @param fileSize the file size in bytes
     * @return validation result
     */
    public static ValidationResult validateFile(String fileName, long fileSize) {
        if (fileName == null || fileName.isBlank()) {
            return ValidationResult.valid(); // File is optional
        }

        // Check file extension
        String extension = getFileExtension(fileName).toLowerCase();
        if (!isAllowedExtension(extension)) {
            return ValidationResult.invalid(
                    "Invalid file type. Allowed: JPG, PNG, PDF");
        }

        // Check file size
        if (fileSize > MAX_FILE_SIZE_BYTES) {
            return ValidationResult.invalid(
                    "File size must not exceed 5MB");
        }

        return ValidationResult.valid();
    }

    /**
     * Gets the file extension from a filename.
     *
     * @param fileName the file name
     * @return the extension without the dot, or empty string
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }

    /**
     * Checks if a file extension is allowed.
     *
     * @param extension the extension to check (without dot)
     * @return true if allowed
     */
    private static boolean isAllowedExtension(String extension) {
        return extension.equals("jpg") ||
                extension.equals("jpeg") ||
                extension.equals("png") ||
                extension.equals("pdf");
    }

    /**
     * Result of a validation check.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, "");
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Result of password validation including strength.
     */
    public static class PasswordValidationResult extends ValidationResult {
        private final PasswordStrength strength;

        public PasswordValidationResult(boolean valid, String message, PasswordStrength strength) {
            super(valid, message);
            this.strength = strength;
        }

        public PasswordStrength getStrength() {
            return strength;
        }
    }

    /**
     * Password strength levels.
     */
    public enum PasswordStrength {
        WEAK("#B71C1C", "Weak"),
        MEDIUM("#FF6F00", "Medium"),
        STRONG("#2E7D32", "Strong");

        private final String color;
        private final String label;

        PasswordStrength(String color, String label) {
            this.color = color;
            this.label = label;
        }

        public String getColor() {
            return color;
        }

        public String getLabel() {
            return label;
        }
    }
}
