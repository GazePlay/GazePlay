package net.gazeplay.commons.utils;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Screen;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.themes.BuiltInUiTheme;
import net.gazeplay.commons.utils.games.Utils;

import java.io.File;
import java.util.Optional;

import static net.gazeplay.commons.utils.games.Utils.FILESEPARATOR;

@Slf4j
public class CssUtil {

    private static int[] supportedWidth = { 2560, 1920, 1600, 1440, 1280, 1024, 800 };

    public static void setPreferredStylesheets(Configuration config, Scene scene) {
        ObservableList<String> stylesheets = scene.getStylesheets();

        String cssfile = config.getCssfile();

        Optional<BuiltInUiTheme> configuredBuiltInUiTheme = BuiltInUiTheme.findFromConfigPropertyValue(cssfile);

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

        stylesheets.removeAll(stylesheets);

        stylesheets.add("data/stylesheets/base.css");

        addMediaWidthStylesheet(stylesheets);

        if (styleSheetPath != null) {
            stylesheets.add(styleSheetPath);
        }

        addStylesheets(stylesheets);
        log.info(stylesheets.toString());
    }

    private static void addMediaWidthStylesheet(ObservableList<String> stylesheets) {
        final int actualScreenWidth = (int) Screen.getPrimary().getBounds().getWidth();
        log.info("actualScreenWidth = {}", actualScreenWidth);

        for (int width : supportedWidth) {
            if (width <= actualScreenWidth) {
                String stylesheet = "data/stylesheets/base-max-width-" + String.format("%04d", width) + ".css";
                stylesheets.add(stylesheet);
                log.info("Added : {}", stylesheet);
                return;
            }
        }
    }

    /**
     * @return CSS files found in the styles folder
     */
    public static void addStylesheets(ObservableList<String> styleSheets) {
        final File styleFolder = new File(getStylesFolder());
        if (styleFolder.exists()) {
            File[] filesInStyleFolder = styleFolder.listFiles();
            assert filesInStyleFolder != null;
            for (File f : filesInStyleFolder) {
                if (f.toString().endsWith(".css")) {
                    styleSheets.add("file://" + f.toString());
                }
            }
        }
    }

    /**
     * @return styles directory for GazePlay : in the default directory of GazePlay, a folder called styles
     */
    public static String getStylesFolder() {
        return Utils.getGazePlayFolder() + "styles" + FILESEPARATOR;
    }

}
