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
 * light and on the dark theme, and the former stylesheet of the color is still loaded for the games; the buttons of
 * the games are light or dark as chosen (24/11/2026).
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
        assertEquals(CssUtil.buttonsStylesheet(false), stylesheets.get(2));
        assertEquals(List.of("data/stylesheets/main-pink-purple.css", CssUtil.MENUS_STYLESHEET),
            stylesheets.subList(3, 5));
    }

    /**
     * The 4 looks (24/11/2026): the buttons of the games are light or dark as chosen, whatever the theme.
     */
    @Test
    void shouldColorTheButtonsAsChosenWhateverTheTheme() throws Exception {
        for (boolean darkTheme : new boolean[]{false, true}) {
            assertEquals(Color.web("#FFFFFF"), colorOf("-gp-button", darkTheme, false));
            assertEquals(Color.web("#2B2118"), colorOf("-gp-button-text", darkTheme, false));
            assertEquals(Color.web("#2A221C"), colorOf("-gp-button", darkTheme, true));
            assertEquals(Color.web("#FFF4E8"), colorOf("-gp-button-text", darkTheme, true));
        }
    }

    /**
     * The theme of the games apart from that of the menus (24/11/2026): the JVM of a game takes the first one.
     */
    @Test
    void shouldTakeTheThemeOfTheGamesInTheJvmOfAGame() throws Exception {
        Configuration config = config("builtin:GREEN", false);
        when(config.isGameDarkThemeEnabled()).thenReturn(true);
        try {
            assertEquals(false, CssUtil.isDarkTheme(config));
            CssUtil.useTheThemeOfTheGames();
            assertEquals(true, CssUtil.isDarkTheme(config));
            // The stylesheets chosen: the dark base (its colors are checked in gazeplay-core, where it lives)
            Scene scene = WaitForAsyncUtils.asyncFx(() -> new Scene(new Pane())).get();
            WaitForAsyncUtils.asyncFx(() -> CssUtil.setPreferredStylesheets(config, scene)).get();
            assertEquals("data/stylesheets/base-dark.css", scene.getStylesheets().getFirst());
        } finally {
            CssUtil.useTheThemeOfTheMenus();
        }
    }

    /**
     * The theme and the buttons of the evaluations, apart from those of the games and of the menus (24/11/2026).
     */
    @Test
    void shouldTakeTheThemeAndTheButtonsOfTheEvaluationsInTheJvmOfAnEvaluation() throws Exception {
        Configuration config = config("builtin:GREEN", false);
        when(config.isGameDarkThemeEnabled()).thenReturn(false);
        when(config.isEvalDarkThemeEnabled()).thenReturn(true);
        when(config.isDarkButtonsEnabled()).thenReturn(false);
        when(config.isEvalDarkButtonsEnabled()).thenReturn(true);
        try {
            CssUtil.useTheThemeOfTheEvaluations();
            assertEquals(true, CssUtil.isDarkTheme(config));
            assertEquals(true, CssUtil.isDarkButtons(config));
            assertEquals(Color.web("#2A221C"), colorOf(config, "-gp-button"));
            CssUtil.useTheThemeOfTheGames();
            assertEquals(false, CssUtil.isDarkTheme(config));
            assertEquals(false, CssUtil.isDarkButtons(config));
        } finally {
            CssUtil.useTheThemeOfTheMenus();
        }
    }

    /**
     * @return the color of a region filled with -gp-accent, once the stylesheets of the settings are applied
     */
    private static Color accentOf(String cssFile, boolean dark) throws Exception {
        return colorOf(config(cssFile, dark), "-gp-accent");
    }

    private static Color colorOf(String lookup, boolean darkTheme, boolean darkButtons) throws Exception {
        Configuration config = config("builtin:GREEN", darkTheme);
        when(config.isDarkButtonsEnabled()).thenReturn(darkButtons);
        return colorOf(config, lookup);
    }

    private static Color colorOf(Configuration config, String lookup) throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            Region region = new Region();
            region.setStyle("-fx-background-color: " + lookup + ";");
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
