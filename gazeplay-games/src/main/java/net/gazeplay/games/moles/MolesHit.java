package net.gazeplay.games.moles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How a target of the moles game is hit (maintainer's choice of 4/11/2026, as in the shooting game): as soon as it is
 * touched, or after a fixation of the length of the settings, which a ring shows.
 */
@Getter
@RequiredArgsConstructor
public enum MolesHit {
    AT_ONCE("MolesAtOnce"),
    FIXATION("MolesFixation");

    /**
     * Key of translation, and other switch of the variants.
     */
    private final String key;

    /**
     * @return the way of a key, at once for an unknown one
     */
    public static MolesHit of(final String key) {
        for (final MolesHit hit : values()) {
            if (hit.key.equals(key)) {
                return hit;
            }
        }
        return AT_ONCE;
    }
}
