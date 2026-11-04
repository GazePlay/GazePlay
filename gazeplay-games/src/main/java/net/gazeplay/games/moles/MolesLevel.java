package net.gazeplay.games.moles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of the moles game (maintainer's choice of 4/11/2026): the holes, the targets out at the same time, and how
 * long a target stays out after the time of the fixation, so that no target is impossible to hit whatever the length of
 * the settings (it used to stay 2 to 4.5 s, for a fixation of 2 s by default).
 */
@Getter
@RequiredArgsConstructor
public enum MolesLevel {
    EASY("MolesEasy", 6, 1, 3),
    NORMAL("MolesNormal", 9, 2, 2),
    HARD("MolesHard", 9, 3, 1.5);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    private final int holes;

    /**
     * The targets out at the same time, at most.
     */
    private final int targets;

    /**
     * The seconds a target stays out, on top of the time of the fixation, at the normal animation speed.
     */
    private final double stay;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static MolesLevel of(final String key) {
        for (final MolesLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
