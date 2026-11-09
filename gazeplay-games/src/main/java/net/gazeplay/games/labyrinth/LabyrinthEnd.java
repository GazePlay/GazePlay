package net.gazeplay.games.labyrinth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When the Labyrinth ends (maintainer's choice of 9/11/2026): after 3, 5 or 10 goals reached (cheeses or houses), or
 * never.
 */
@Getter
@RequiredArgsConstructor
public enum LabyrinthEnd {
    GOALS_3("LabyrinthGoals3", 3),
    GOALS_5("LabyrinthGoals5", 5),
    GOALS_10("LabyrinthGoals10", 10),
    ENDLESS("LabyrinthEndless", 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The goals to reach, 0 for a game without end.
     */
    private final int goals;

    /**
     * @return the end of a key, never for an unknown one
     */
    public static LabyrinthEnd of(final String key) {
        for (final LabyrinthEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
