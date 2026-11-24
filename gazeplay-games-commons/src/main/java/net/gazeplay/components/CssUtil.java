package net.gazeplay.components;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.themes.BuiltInUiTheme;
import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * Stylesheets of the scenes: the light or dark base, the main color of the menus, the former stylesheet of the main
 * color (still used by the controls of the games), the look of the menus, then the user's own stylesheets of the
 * "styles" folder of the GazePlay folder. The stylesheets by width of screen (base-max-width-*.css) were removed with
 * the former cards of the menu (October 2026).
 */
@Slf4j
public class CssUtil {

    /**
     * The look of the menus (REFONTE_GRAPHIQUE.md).
     */
    static final String MENUS_STYLESHEET = "data/stylesheets/menus.css";

    /**
     * Replaces the stylesheets of a scene by those of the current settings. A theme that is neither built in nor an
     * existing file gives the default theme.
     */
    public static void setPreferredStylesheets(final Configuration config, final Scene scene) {
        final ObservableList<String> stylesheets = scene.getStylesheets();

        final String cssfile = config.getCssFile();

        final Optional<BuiltInUiTheme> configuredBuiltInUiTheme = BuiltInUiTheme.findFromConfigPropertyValue(cssfile);

        final String styleSheetPath;

        if (configuredBuiltInUiTheme.isPresent()) {
            styleSheetPath = configuredBuiltInUiTheme.get().getStyleSheetPath();
        } else {
            // Blank and isFile(): since Java 25, new File("") is the current folder and exists
            if (cssfile == null || cssfile.isBlank() || !new File(cssfile).isFile()) {
                styleSheetPath = BuiltInUiTheme.DEFAULT_THEME.getStyleSheetPath();
            } else {
                styleSheetPath = cssfile;
            }
        }
        final boolean dark = config.isDarkThemeEnabled();
        stylesheets.clear();
        if (dark) {
           stylesheets.add("data/stylesheets/base-dark.css");
        }
        else {
            stylesheets.add("data/stylesheets/base-light.css");
        }
        // The main color of the menus, after the base, whose -gp-accent it replaces (the coral of a stylesheet file)
        stylesheets.add(accentStylesheet(configuredBuiltInUiTheme.orElse(BuiltInUiTheme.DEFAULT_THEME)
            .getAccentColor(dark)));
        // The buttons of the games, light or dark whatever the theme (24/11/2026)
        stylesheets.add(buttonsStylesheet(Boolean.TRUE.equals(config.isDarkButtonsEnabled())));

        if (styleSheetPath != null) {
            stylesheets.add(styleSheetPath);
        }
        // Rules under .gp-menu only, which the games never carry: after the theme, so that they win over it
        stylesheets.add(MENUS_STYLESHEET);

        addStylesheets(stylesheets);
        log.info(stylesheets.toString());
    }

    /**
     * @return a stylesheet setting the accent color of the menus (-gp-accent), as a data URI (read by JavaFX since
     * its version 17): one stylesheet for any color, without a file per color and per theme
     */
    static String accentStylesheet(final String color) {
        final String css = ".root { -gp-accent: " + color + "; }";
        return "data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * The background of the games: -gp-bg of base-light.css and base-dark.css (to keep the same).
     */
    static final String LIGHT_BACKGROUND = "#FFF8EF";
    static final String DARK_BACKGROUND = "#1B1612";

    /**
     * @return the background color of the game area for this theme (24/11/2026: the setting "Background color" is
     * removed for the games, maintainer's decision)
     */
    public static String backgroundColor(final boolean darkTheme) {
        return darkTheme ? DARK_BACKGROUND : LIGHT_BACKGROUND;
    }

    /**
     * Light buttons: white, the text of the light theme. Dark buttons: the surface and the text of the dark theme.
     */
    static final String LIGHT_BUTTON = "#FFFFFF";
    static final String LIGHT_BUTTON_TEXT = "#2B2118";
    static final String DARK_BUTTON = "#2A221C";
    static final String DARK_BUTTON_TEXT = "#FFF4E8";

    /**
     * Maintainer's decision of 24/11/2026: the theme (light or dark) and the buttons of the games (light or dark) are
     * chosen apart, 4 looks; the light buttons are well seen on the dark theme. A button of a game is filled with
     * {@code -gp-button}, its text in {@code -gp-button-text} (see {@link GameStyle#BUTTON_STYLE}).
     *
     * @return a stylesheet setting the colors of the buttons of the games, as a data URI
     */
    static String buttonsStylesheet(final boolean dark) {
        final String css = ".root { -gp-button: " + (dark ? DARK_BUTTON : LIGHT_BUTTON) + "; -gp-button-text: "
            + (dark ? DARK_BUTTON_TEXT : LIGHT_BUTTON_TEXT) + "; }";
        return "data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Adds the .css files of the "styles" folder of the GazePlay folder, if any.
     */
    public static void addStylesheets(final ObservableList<String> styleSheets) {
        final File styleFolder = getStylesFolder();
        if (styleFolder.exists()) {
            final File[] filesInStyleFolder = styleFolder.listFiles();
            assert filesInStyleFolder != null;
            for (final File f : filesInStyleFolder) {
                if (f.toString().endsWith(".css")) {
                    styleSheets.add("file://" + f.toString());
                }
            }
        }
    }

    /**
     * @return styles directory for GazePlay : in the default directory of GazePlay, a folder called styles
     */
    public static File getStylesFolder() {
        return new File(GazePlayDirectories.getGazePlayFolder(), "styles");
    }

}
