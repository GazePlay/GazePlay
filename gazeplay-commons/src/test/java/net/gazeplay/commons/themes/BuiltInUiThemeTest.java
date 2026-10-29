package net.gazeplay.commons.themes;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BuiltInUiThemeTest {

    @Test
    void shouldFindAThemeFromItsSavedValue() {
        assertEquals(Optional.of(BuiltInUiTheme.ORANGE), BuiltInUiTheme.findFromConfigPropertyValue("builtin:ORANGE"));
        assertEquals(Optional.of(BuiltInUiTheme.ORANGE),
            BuiltInUiTheme.findFromConfigPropertyValue("data/stylesheets/main-orange.css"));
    }

    /**
     * Seven main colors in the settings, each with its own shade on each theme. The system theme is no longer offered.
     */
    @Test
    void shouldOfferSevenMainColors() {
        List<BuiltInUiTheme> colors = BuiltInUiTheme.offeredColors();
        assertEquals(List.of(BuiltInUiTheme.SILVER_AND_GOLD, BuiltInUiTheme.ORANGE, BuiltInUiTheme.GREEN,
            BuiltInUiTheme.LIGHT_BLUE, BuiltInUiTheme.BLUE, BuiltInUiTheme.PINK_AND_PURPLE, BuiltInUiTheme.VIOLET),
            colors);
        assertFalse(colors.contains(BuiltInUiTheme.SYSTEM));
        assertEquals(colors.size(), colors.stream().map(color -> color.getAccentColor(false)).distinct().count());
        assertEquals(colors.size(), colors.stream().map(color -> color.getAccentColor(true)).distinct().count());
        assertEquals(BuiltInUiTheme.SILVER_AND_GOLD, BuiltInUiTheme.DEFAULT_THEME);
    }

    /**
     * Violet and pink share the former stylesheet "Pink and Purple": its path, saved by older versions, is pink.
     */
    @Test
    void shouldFindThePinkFromTheFormerStylesheetPath() {
        assertEquals(Optional.of(BuiltInUiTheme.PINK_AND_PURPLE),
            BuiltInUiTheme.findFromConfigPropertyValue("data/stylesheets/main-pink-purple.css"));
        assertEquals(Optional.of(BuiltInUiTheme.VIOLET), BuiltInUiTheme.findFromConfigPropertyValue("builtin:VIOLET"));
    }

    /**
     * The AFSR themes were removed with the AFSR edition: a value saved by a former version is no longer found, and
     * the callers (CssUtil, configuration screen) use the default theme. The saved value itself is not rewritten.
     */
    @Test
    void shouldNotFindTheRemovedAfsrThemes() {
        assertEquals(Optional.empty(), BuiltInUiTheme.findFromConfigPropertyValue("builtin:AFSRGREEN"));
        assertEquals(Optional.empty(), BuiltInUiTheme.findFromConfigPropertyValue("builtin:AFSRPURPLE"));
        assertEquals(Optional.empty(), BuiltInUiTheme.findFromConfigPropertyValue("data/stylesheets/afsr-green.css"));
    }
}
