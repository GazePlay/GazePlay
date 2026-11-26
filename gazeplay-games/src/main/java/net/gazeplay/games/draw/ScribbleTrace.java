package net.gazeplay.games.draw;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "Gribouille" starts and ends a line (tester's feedback of 23/11/2026, point 48; maintainer's choices of
 * 26/11/2026): freely, the line following the gaze as soon as it is on the sheet, or by fixation, a fixation putting
 * the pencil down and another one lifting it, for a drawing made on purpose.
 */
@Getter
@RequiredArgsConstructor
public enum ScribbleTrace {
    FREE("ScribbleFreeLine"),
    FIXATION("ScribbleFixationLine");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * @return the way of a key, free for an unknown one
     */
    public static ScribbleTrace of(final String key) {
        for (final ScribbleTrace trace : values()) {
            if (trace.key.equals(key)) {
                return trace;
            }
        }
        return FREE;
    }
}
