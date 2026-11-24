package net.gazeplay.ui;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.components.CssUtil;
import net.gazeplay.components.GameStyle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The theme of the games (24/11/2026), with the stylesheets of gazeplay-core (base-light.css, base-dark.css): what
 * takes its colors in the style follows a change of theme at once, without being drawn again.
 */
@ExtendWith(ApplicationExtension.class)
class GameThemeColorsTest {

    @Test
    void shouldColorTheEmptyHeartsAsTheTheme() throws Exception {
        final Configuration light = config(false);
        final Configuration dark = config(true);
        final Paint[] fills = WaitForAsyncUtils.asyncFx(() -> {
            final Group hearts = GameStyle.lives(1, 3, 40, 400);
            final Shape empty = (Shape) hearts.getChildren().get(2);
            final Scene scene = new Scene(new Pane(hearts));
            CssUtil.setPreferredStylesheets(light, scene);
            hearts.applyCss();
            final Paint onLight = empty.getFill();
            CssUtil.setPreferredStylesheets(dark, scene);
            hearts.applyCss();
            return new Paint[]{onLight, empty.getFill()};
        }).get();
        assertEquals(Color.rgb(120, 120, 130, 0.8), fills[0]);
        assertEquals(Color.rgb(200, 200, 215, 0.8), fills[1]);
    }

    /**
     * The background of the theme, the same as {@link CssUtil#backgroundColor} (kept equal by hand).
     */
    @Test
    void shouldGiveTheBackgroundOfTheTheme() throws Exception {
        for (final boolean dark : new boolean[]{false, true}) {
            final Configuration config = config(dark);
            final Color background = WaitForAsyncUtils.asyncFx(() -> {
                final Region region = new Region();
                region.setStyle("-fx-background-color: -gp-bg;");
                final Scene scene = new Scene(new Pane(region));
                CssUtil.setPreferredStylesheets(config, scene);
                region.applyCss();
                return (Color) region.getBackground().getFills().getFirst().getFill();
            }).get();
            assertEquals(Color.web(CssUtil.backgroundColor(dark)), background, "dark: " + dark);
        }
    }

    private static Configuration config(final boolean dark) {
        final Configuration config = mock(Configuration.class);
        when(config.getCssFile()).thenReturn("builtin:GREEN");
        when(config.isDarkThemeEnabled()).thenReturn(dark);
        return config;
    }
}
