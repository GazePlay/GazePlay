package net.gazeplay.components;

import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.themes.BuiltInUiTheme;
import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Stylesheets of the scenes: the light or dark base, one adapted to the width of the screen, the color theme of the
 * settings, the look of the menus, then the user's own stylesheets of the "styles" folder of the GazePlay folder.
 */
@Slf4j
public class CssUtil {

    private static final int[] supportedWidth = {2560, 1920, 1600, 1440, 1280, 1024, 800};

    /**
     * The look of the menus (REFONTE_GRAPHIQUE.md).
     */
    static final String MENUS_STYLESHEET = "data/stylesheets/menus.css";

    /**
     * Replaces the stylesheets of a scene by those of the current settings. A theme that is neither built in nor an
     * existing file gives the default theme.
     */
    public static void setPreferredStylesheets(final Configuration config, final Scene scene, final Supplier<Dimension2D> screenDimensionSupplier) {
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
        stylesheets.clear();
        if (config.isDarkThemeEnabled()) {
           stylesheets.add("data/stylesheets/base-dark.css");
        }
        else {
            stylesheets.add("data/stylesheets/base-light.css");
        }
        addMediaWidthStylesheet(stylesheets, screenDimensionSupplier);

        if (styleSheetPath != null) {
            stylesheets.add(styleSheetPath);
        }
        // Rules under .gp-menu only, which the games never carry: after the theme, so that they win over it
        stylesheets.add(MENUS_STYLESHEET);

        addStylesheets(stylesheets);
        log.info(stylesheets.toString());
    }

    /**
     * Adds the stylesheet of the largest supported width not wider than the screen (none below 800 pixels).
     */
    private static void addMediaWidthStylesheet(final ObservableList<String> stylesheets, final Supplier<Dimension2D> screenDimensionSupplier) {
        final int actualScreenWidth = (int) screenDimensionSupplier.get().getWidth();
        log.info("actualScreenWidth = {}", actualScreenWidth);

        for (final int width : supportedWidth) {
            if (width <= actualScreenWidth) {
                final String stylesheet = "data/stylesheets/base-max-width-" + String.format("%04d", width) + ".css";
                stylesheets.add(stylesheet);
                log.info("Added : {}", stylesheet);
                return;
            }
        }
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
