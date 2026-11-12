package net.gazeplay.games.draw;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * What "Gribouille" draws on (maintainer's choice of 12/11/2026): a white sheet or a blackboard; the first color of the
 * palette is the ink which shows best on it (black on the sheet, white on the board).
 */
@Getter
@RequiredArgsConstructor
public enum ScribbleBackground {
    SHEET("ScribbleSheet", Color.rgb(252, 251, 247), Color.rgb(40, 38, 48)),
    BOARD("ScribbleBoard", Color.rgb(36, 52, 46), Color.rgb(246, 246, 240));

    /**
     * Key of translation, and value (the button) of the variants.
     */
    private final String key;
    private final Color paper;
    private final Color ink;

    /**
     * @return the background of a key, the sheet for an unknown one
     */
    public static ScribbleBackground of(final String key) {
        for (final ScribbleBackground background : values()) {
            if (background.key.equals(key)) {
                return background;
            }
        }
        return SHEET;
    }
}
