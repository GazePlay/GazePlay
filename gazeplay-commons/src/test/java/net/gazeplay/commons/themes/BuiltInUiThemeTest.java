package net.gazeplay.commons.themes;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltInUiThemeTest {

    @Test
    void shouldFindAThemeFromItsSavedValue() {
        assertEquals(Optional.of(BuiltInUiTheme.ORANGE), BuiltInUiTheme.findFromConfigPropertyValue("builtin:ORANGE"));
        assertEquals(Optional.of(BuiltInUiTheme.ORANGE),
            BuiltInUiTheme.findFromConfigPropertyValue("data/stylesheets/main-orange.css"));
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
