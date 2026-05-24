package com.resolvit.ui.utils;

import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * Manages theme switching between light and dark modes.
 * Implements the Singleton pattern for consistent theme state across the application.
 *
 * @author ResolvIt Team
 * @version 1.0.0
 */
public final class ThemeManager {

    private static final Logger log = LoggerFactory.getLogger(ThemeManager.class);
    private static final String PREF_DARK_MODE = "darkMode";
    private static final String LIGHT_THEME_PATH = "/css/theme-light.css";
    private static final String DARK_THEME_PATH = "/css/theme-dark.css";
    private static final String COMPONENTS_CSS_PATH = "/css/components.css";
    private static final String ANIMATIONS_CSS_PATH = "/css/animations.css";

    private static volatile ThemeManager instance;
    private final Preferences preferences;
    private boolean isDarkMode;

    /**
     * Private constructor for singleton pattern.
     */
    private ThemeManager() {
        preferences = Preferences.userNodeForPackage(ThemeManager.class);
        isDarkMode = preferences.getBoolean(PREF_DARK_MODE, false);
        log.info("ThemeManager initialized with darkMode={}", isDarkMode);
    }

    /**
     * Gets the singleton instance of ThemeManager.
     *
     * @return the ThemeManager instance
     */
    public static ThemeManager getInstance() {
        if (instance == null) {
            synchronized (ThemeManager.class) {
                if (instance == null) {
                    instance = new ThemeManager();
                }
            }
        }
        return instance;
    }

    /**
     * Checks if dark mode is currently enabled.
     *
     * @return true if dark mode is enabled
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }

    /**
     * Toggles between light and dark themes.
     *
     * @param scene the scene to apply the theme to
     */
    public void toggleTheme(Scene scene) {
        isDarkMode = !isDarkMode;
        applyTheme(scene, isDarkMode);
        savePreference();
        log.info("Theme toggled to: {}", isDarkMode ? "dark" : "light");
    }

    /**
     * Applies the specified theme to the scene.
     *
     * @param scene    the scene to style
     * @param darkMode true for dark theme, false for light theme
     */
    public void applyTheme(Scene scene, boolean darkMode) {
        if (scene == null) {
            log.warn("Cannot apply theme to null scene");
            return;
        }

        // Clear existing stylesheets
        scene.getStylesheets().clear();

        // Add theme stylesheet
        String themePath = darkMode ? DARK_THEME_PATH : LIGHT_THEME_PATH;
        addStylesheet(scene, themePath);

        // Add component styles
        addStylesheet(scene, COMPONENTS_CSS_PATH);

        // Add animations
        addStylesheet(scene, ANIMATIONS_CSS_PATH);

        log.debug("Applied {} theme to scene", darkMode ? "dark" : "light");
    }

    /**
     * Applies the current theme to the scene.
     *
     * @param scene the scene to style
     */
    public void applyCurrentTheme(Scene scene) {
        applyTheme(scene, isDarkMode);
    }

    /**
     * Sets the dark mode preference.
     *
     * @param scene    the scene to apply the theme to
     * @param darkMode true for dark mode, false for light mode
     */
    public void setDarkMode(Scene scene, boolean darkMode) {
        if (this.isDarkMode != darkMode) {
            this.isDarkMode = darkMode;
            applyTheme(scene, darkMode);
            savePreference();
            log.info("Dark mode set to: {}", darkMode);
        }
    }

    /**
     * Adds a stylesheet to the scene if it exists.
     *
     * @param scene the scene to add the stylesheet to
     * @param path  the resource path of the stylesheet
     */
    private void addStylesheet(Scene scene, String path) {
        try {
            String css = Objects.requireNonNull(
                    getClass().getResource(path)).toExternalForm();
            scene.getStylesheets().add(css);
            log.debug("Added stylesheet: {}", path);
        } catch (NullPointerException e) {
            log.warn("Stylesheet not found: {}", path);
        }
    }

    /**
     * Saves the theme preference to persistent storage.
     */
    private void savePreference() {
        preferences.putBoolean(PREF_DARK_MODE, isDarkMode);
        log.debug("Theme preference saved: darkMode={}", isDarkMode);
    }

    /**
     * Gets the CSS class for primary button based on current theme.
     *
     * @return the CSS class name
     */
    public String getPrimaryButtonClass() {
        return "btn-primary";
    }

    /**
     * Gets the CSS class for secondary button based on current theme.
     *
     * @return the CSS class name
     */
    public String getSecondaryButtonClass() {
        return "btn-secondary";
    }

    /**
     * Gets the CSS class for danger/destructive button based on current theme.
     *
     * @return the CSS class name
     */
    public String getDangerButtonClass() {
        return "btn-danger";
    }
}
