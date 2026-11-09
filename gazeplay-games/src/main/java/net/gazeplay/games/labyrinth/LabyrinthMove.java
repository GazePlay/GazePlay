package net.gazeplay.games.labyrinth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How the player moves the character of the Labyrinth (maintainer's choice of 9/11/2026): a fixation on an arrow in a
 * box next to the character, or on an arrow around the labyrinth; or a fixation on the character, then a look at a box
 * next to it.
 */
@Getter
@RequiredArgsConstructor
public enum LabyrinthMove {
    LOCAL_ARROWS("LabyrinthLocalArrows"),
    GLOBAL_ARROWS("LabyrinthGlobalArrows"),
    SELECT_THEN_GUIDE("LabyrinthSelectThenGuide");

    /**
     * Key of translation, and value of the third row of switches of the variants.
     */
    private final String key;

    /**
     * @return the way of a key, arrows next to the character for an unknown one
     */
    public static LabyrinthMove of(final String key) {
        for (final LabyrinthMove move : values()) {
            if (move.key.equals(key)) {
                return move;
            }
        }
        return LOCAL_ARROWS;
    }
}
