package net.gazeplay.ui.menus;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every icon of the menus has its SVG file, with a path.
 */
class MenuIconTest {

    @ParameterizedTest
    @EnumSource(MenuIcon.class)
    void shouldReadThePathOfItsFile(MenuIcon icon) {
        String path = icon.getPath();

        assertTrue(path.startsWith("M") || path.startsWith("m"), icon + ": " + path);
        assertTrue(path.endsWith("Z"), icon + ": " + path);
    }
}
