package net.gazeplay.ui.menus;

import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;

/**
 * The fonts of the menus, shipped with GazePlay (SIL Open Font License 1.1, see data/fonts): Andika for the texts, made
 * for beginning readers, and Fredoka for the titles, the font of "Learning" in the logo. Loaded once in the JVM of the
 * menus; the games do not use them. Writings that they do not cover (e.g. Arabic, Chinese) fall back to a font of the
 * system.
 */
@Slf4j
public final class MenuFonts {

    private static final String[] FONT_FILES = {
        "data/fonts/Andika-Regular.ttf",
        "data/fonts/Andika-Bold.ttf",
        // Fixed instance (weight 600) of the variable Fredoka: JavaFX only uses the default instance of a variable font
        "data/fonts/Fredoka-SemiBold.ttf"
    };

    private static boolean loaded;

    private MenuFonts() {
    }

    /**
     * Loads the fonts at the first call; a missing font only leaves the font of the system. Called on the JavaFX thread
     * only (screens and dialogs of the menus).
     */
    public static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        for (final String file : FONT_FILES) {
            final var url = MenuFonts.class.getClassLoader().getResource(file);
            final Font font = url == null ? null : Font.loadFont(url.toExternalForm(), 12);
            if (font == null) {
                log.warn("Font not loaded: {}", file);
            } else {
                log.info("Font loaded: {} (family {})", file, font.getFamily());
            }
        }
    }
}
