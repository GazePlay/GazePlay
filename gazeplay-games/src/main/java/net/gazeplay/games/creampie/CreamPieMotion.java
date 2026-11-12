package net.gazeplay.games.creampie;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Whether the targets of "Tartes à la crème" move (maintainer's choice of 12/11/2026): still, as before, or gliding
 * slowly, to be followed by the gaze.
 */
@Getter
@RequiredArgsConstructor
public enum CreamPieMotion {
    STILL("PieStill"),
    MOVING("PieMoving");

    /**
     * Key of translation, and other switch of the variants.
     */
    private final String key;

    /**
     * @return the motion of a key, still for an unknown one
     */
    public static CreamPieMotion of(final String key) {
        for (final CreamPieMotion motion : values()) {
            if (motion.key.equals(key)) {
                return motion;
            }
        }
        return STILL;
    }
}
