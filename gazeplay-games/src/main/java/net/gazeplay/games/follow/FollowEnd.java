package net.gazeplay.games.follow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Suivre le chemin" ends (13/11/2026): after 1, 3 or 5 maps, or never.
 */
@Getter
@RequiredArgsConstructor
public enum FollowEnd {
    MAPS_1("FollowEnd1", 1),
    MAPS_3("FollowEnd3", 3),
    MAPS_5("FollowEnd5", 5),
    ENDLESS("FollowEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The maps to finish, 0 without end.
     */
    private final int maps;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static FollowEnd of(final String key) {
        for (final FollowEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
