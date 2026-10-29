package net.gazeplay.components;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import net.gazeplay.commons.configuration.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The main color of the menus (REFONTE_GRAPHIQUE.md 3.3): its stylesheet replaces the -gp-accent of the base, on the
 * light and on the dark theme, and the former stylesheet of the color is still loaded for the games.
 */
@ExtendWith(ApplicationExtension.class)
class CssUtilTest {

    @Test
    void shouldApplyTheMainColorOfTheLightTheme() throws Exception {
        assertEquals(Color.web("#2E8B47"), accentOf("builtin:GREEN", false));
    }

    @Test
    void shouldApplyTheMainColorOfTheDarkTheme() throws Exception {
        assertEquals(Color.web("#8C9EFF"), accentOf("builtin:BLUE", true));
    }

    /**
     * The former default theme, the system theme and a removed theme all give the coral of the menus.
     */
    @Test
    void shouldGiveTheCoralToTheFormerThemes() throws Exception {
        for (String formerValue : List.of("builtin:SILVER_AND_GOLD", "builtin:SYSTEM", "builtin:AFSRGREEN", "")) {
            assertEquals(Color.web("#F2643A"), accentOf(formerValue, false), formerValue);
        }
    }

    @Test
    void shouldKeepTheStylesheetOfTheColorForTheGames() throws Exception {
        Scene scene = WaitForAsyncUtils.asyncFx(() -> new Scene(new Pane())).get();
        Configuration config = config("builtin:PINK_AND_PURPLE", false);

        WaitForAsyncUtils.asyncFx(() -> CssUtil.setPreferredStylesheets(config, scene)).get();

        List<String> stylesheets = scene.getStylesheets();
        assertEquals("data/stylesheets/base-light.css", stylesheets.get(0));
        assertEquals(CssUtil.accentStylesheet("#D13D7A"), stylesheets.get(1));
        assertEquals(List.of("data/stylesheets/main-pink-purple.css", CssUtil.MENUS_STYLESHEET),
            stylesheets.subList(2, 4));
    }

    /**
     * @return the color of a region filled with -gp-accent, once the stylesheets of the settings are applied
     */
    private static Color accentOf(String cssFile, boolean dark) throws Exception {
        Configuration config = config(cssFile, dark);
        return WaitForAsyncUtils.asyncFx(() -> {
            Region region = new Region();
            region.setStyle("-fx-background-color: -gp-accent;");
            Scene scene = new Scene(new Pane(region));
            CssUtil.setPreferredStylesheets(config, scene);
            region.applyCss();
            return (Color) region.getBackground().getFills().getFirst().getFill();
        }).get();
    }

    private static Configuration config(String cssFile, boolean dark) {
        Configuration config = mock(Configuration.class);
        when(config.getCssFile()).thenReturn(cssFile);
        when(config.isDarkThemeEnabled()).thenReturn(dark);
        return config;
    }
}
