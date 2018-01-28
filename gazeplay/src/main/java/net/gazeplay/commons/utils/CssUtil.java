package net.gazeplay.commons.utils;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.themes.BuiltInUiTheme;
import net.gazeplay.commons.utils.games.Utils;

import java.io.File;
import java.util.Optional;

@Slf4j
public class CssUtil {

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
        if (styleSheetPath != null) {
            stylesheets.add(styleSheetPath);
        }

        Utils.addStylesheets(stylesheets);
        log.info(stylesheets.toString());
    }

}
