package net.gazeplay.games.room;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of the search of "La pièce" (13/11/2026): the question with the picture of the object (for a child who does
 * not read yet), or its name only. A switch of the variants.
 */
@Getter
@RequiredArgsConstructor
public enum RoomLevel {
    PICTURE("RoomWithPicture"),
    WORD("RoomWordOnly");

    /**
     * Key of translation, and value of the switch.
     */
    private final String key;

    /**
     * @return the level of a key, with the picture for an unknown one
     */
    public static RoomLevel of(final String key) {
        for (final RoomLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return PICTURE;
    }
}
