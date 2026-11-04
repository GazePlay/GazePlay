package net.gazeplay.games.moles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When the moles game ends (maintainer's choice of 4/11/2026): never, after 1 or 3 minutes, or after 20 or 50 targets
 * hit; then "Bravo" and the statistics. There is no life and no penalty: a target missed goes back in its hole.
 */
@Getter
@RequiredArgsConstructor
public enum MolesEnd {
    ENDLESS("MolesEndless", 0, 0),
    TIME_1("MolesTime1", 0, 60),
    TIME_3("MolesTime3", 0, 180),
    HITS_20("MolesHits20", 20, 0),
    HITS_50("MolesHits50", 50, 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The targets to hit, 0 when the end is not a number of targets.
     */
    private final int targets;

    /**
     * The time of the game, in seconds, 0 when the end is not a time.
     */
    private final int seconds;

    /**
     * @return the end of a key, never for an unknown one
     */
    public static MolesEnd of(final String key) {
        for (final MolesEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
