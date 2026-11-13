package net.gazeplay.games.room;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When the search of "La pièce" ends (maintainer's choice of 13/11/2026): after 5, 10 or 20 objects, or never; the
 * exploration has no end.
 */
@Getter
@RequiredArgsConstructor
public enum RoomEnd {
    OBJECTS_5("RoomEnd5", 5),
    OBJECTS_10("RoomEnd10", 10),
    OBJECTS_20("RoomEnd20", 20),
    ENDLESS("RoomEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The objects to find, 0 without end.
     */
    private final int objects;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static RoomEnd of(final String key) {
        for (final RoomEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
