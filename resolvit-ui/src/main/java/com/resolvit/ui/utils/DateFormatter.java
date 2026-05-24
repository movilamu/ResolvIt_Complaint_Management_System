package com.resolvit.ui.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for formatting dates and times throughout the application.
 * Provides consistent date display formats across all UI components.
 *
 * @author ResolvIt Team
 * @version 1.0.0
 */
public final class DateFormatter {

    // Standard formatters
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final DateTimeFormatter DATE_TIME_FULL = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy 'at' HH:mm");
    private static final DateTimeFormatter DATE_SHORT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateFormatter() {
        // Utility class - prevent instantiation
    }

    /**
     * Formats a LocalDateTime to standard date format (dd MMM yyyy).
     *
     * @param dateTime the date time to format
     * @return formatted date string, or empty string if null
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMAT);
    }

    /**
     * Formats a LocalDate to standard date format (dd MMM yyyy).
     *
     * @param date the date to format
     * @return formatted date string, or empty string if null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMAT);
    }

    /**
     * Formats a LocalDateTime to time only format (HH:mm).
     *
     * @param dateTime the date time to format
     * @return formatted time string, or empty string if null
     */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(TIME_FORMAT);
    }

    /**
     * Formats a LocalDateTime to date and time format (dd MMM yyyy, HH:mm).
     *
     * @param dateTime the date time to format
     * @return formatted date time string, or empty string if null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_TIME_FORMAT);
    }

    /**
     * Formats a LocalDateTime to full format (EEEE, dd MMMM yyyy at HH:mm).
     *
     * @param dateTime the date time to format
     * @return formatted full date time string, or empty string if null
     */
    public static String formatDateTimeFull(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_TIME_FULL);
    }

    /**
     * Formats a LocalDate to short format (dd/MM/yyyy).
     *
     * @param date the date to format
     * @return formatted short date string, or empty string if null
     */
    public static String formatDateShort(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_SHORT);
    }

    /**
     * Formats a LocalDateTime to ISO format.
     *
     * @param dateTime the date time to format
     * @return formatted ISO date time string, or empty string if null
     */
    public static String formatISO(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(ISO_DATE_TIME);
    }

    /**
     * Formats a date time as a relative time string (e.g., "2 hours ago", "Yesterday").
     *
     * @param dateTime the date time to format
     * @return relative time string
     */
    public static String formatRelative(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (days == 1) {
            return "Yesterday";
        } else if (days < 7) {
            return days + " days ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        } else if (days < 365) {
            long months = days / 30;
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            return formatDate(dateTime);
        }
    }

    /**
     * Formats a duration between two dates as a human-readable string.
     *
     * @param start the start date time
     * @param end   the end date time
     * @return duration string (e.g., "2 days, 3 hours")
     */
    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "";

        long totalMinutes = ChronoUnit.MINUTES.between(start, end);

        if (totalMinutes < 0) {
            return "Invalid duration";
        }

        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append(days == 1 ? " day" : " days");
        }
        if (hours > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(hours).append(hours == 1 ? " hour" : " hours");
        }
        if (minutes > 0 && days == 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(minutes).append(minutes == 1 ? " min" : " mins");
        }

        return sb.length() > 0 ? sb.toString() : "Less than a minute";
    }

    /**
     * Calculates the average resolution time from a list of durations.
     *
     * @param totalMinutes array of resolution times in minutes
     * @return formatted average duration string
     */
    public static String formatAverageResolutionTime(long[] totalMinutes) {
        if (totalMinutes == null || totalMinutes.length == 0) {
            return "N/A";
        }

        long sum = 0;
        for (long m : totalMinutes) {
            sum += m;
        }
        long avgMinutes = sum / totalMinutes.length;

        if (avgMinutes < 60) {
            return avgMinutes + " mins";
        } else if (avgMinutes < 24 * 60) {
            return (avgMinutes / 60) + " hours";
        } else {
            return (avgMinutes / (24 * 60)) + " days";
        }
    }

    /**
     * Parses a date string in ISO format to LocalDate.
     *
     * @param dateStr the date string to parse
     * @return parsed LocalDate, or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr, ISO_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses a date time string in ISO format to LocalDateTime.
     *
     * @param dateTimeStr the date time string to parse
     * @return parsed LocalDateTime, or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;
        try {
            return LocalDateTime.parse(dateTimeStr, ISO_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}
