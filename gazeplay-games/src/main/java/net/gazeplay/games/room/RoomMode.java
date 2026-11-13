package net.gazeplay.games.room;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "La pièce" is played (maintainer's choices of 13/11/2026): find the object asked, or look around freely.
 */
@Getter
@RequiredArgsConstructor
public enum RoomMode {
    SEARCH("RoomSearch"),
    EXPLORE("RoomExplore");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, the exploration for an unknown one (the game had no variant before its review)
     */
    public static RoomMode of(final String key) {
        for (final RoomMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return EXPLORE;
    }
}
