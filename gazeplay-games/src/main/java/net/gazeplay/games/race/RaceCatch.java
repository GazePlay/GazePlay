package net.gazeplay.games.race;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How a bug of "FrogsRace" is caught (maintainer's request of 26/11/2026, with point 55: too easy without fixation): as
 * soon as the gaze enters it, or after a fixation, a ring going round it meanwhile. The bugs fly: with the setting of
 * the moving targets, a fixation left keeps its progress. The easiest first, chosen when the window of the variants
 * opens (maintainer's rule of 20/11/2026).
 */
@Getter
@RequiredArgsConstructor
public enum RaceCatch {
    AT_ONCE("RaceCatchAtOnce"),
    FIXATION("RaceCatchFixation");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * @return the way of a key, at once (the game of before) for an unknown one
     */
    public static RaceCatch of(final String key) {
        for (final RaceCatch way : values()) {
            if (way.key.equals(key)) {
                return way;
            }
        }
        return AT_ONCE;
    }
}
