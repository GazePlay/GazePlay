package net.gazeplay.commons.utils;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.Utils;

@Slf4j
public class CssUtil {

    public static void setPreferredStylesheets(Configuration config, Scene scene) {
        ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.add(config.getCssfile());
        Utils.addStylesheets(stylesheets);
        log.info(stylesheets.toString());
    }

}
