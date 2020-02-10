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

@Slf4j
public class CssUtil {

    private static final int[] supportedWidth = {2560, 1920, 1600, 1440, 1280, 1024, 800};

    public static void setPreferredStylesheets(final Configuration config, final Scene scene, final Supplier<Dimension2D> screenDimensionSupplier) {
        final ObservableList<String> stylesheets = scene.getStylesheets();

        final String cssfile = config.getCssFile();

        final Optional<BuiltInUiTheme> configuredBuiltInUiTheme = BuiltInUiTheme.findFromConfigPropertyValue(cssfile);

        final String styleSheetPath;

        if (configuredBuiltInUiTheme.isPresent()) {
            styleSheetPath = configuredBuiltInUiTheme.get().getStyleSheetPath();
        } else {
            if (cssfile == null || !new File(cssfile).exists()) {
                styleSheetPath = BuiltInUiTheme.DEFAULT_THEME.getStyleSheetPath();
            } else {
                styleSheetPath = cssfile;
            }
        }

        stylesheets.clear();

        stylesheets.add("data/stylesheets/base.css");

        addMediaWidthStylesheet(stylesheets, screenDimensionSupplier);

        if (styleSheetPath != null) {
            stylesheets.add(styleSheetPath);
        }

        addStylesheets(stylesheets);
        log.info(stylesheets.toString());
    }

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
