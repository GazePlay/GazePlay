package net.gazeplay.games.room;

import javafx.scene.paint.Color;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * The rooms of "La pièce" (maintainer's choices of 13/11/2026), each with the color of its walls and its floor, and the
 * value "all the rooms" of the search (each question in a room drawn at random). The buttons of the variants.
 */
@Getter
public enum RoomPlace {
    LIVING("RoomLiving", Color.rgb(206, 226, 196), false),
    KITCHEN("RoomKitchen", Color.rgb(248, 232, 184), true),
    BEDROOM("RoomBedroom", Color.rgb(206, 220, 242), false),
    BATHROOM("RoomBathroom", Color.rgb(200, 234, 232), true),
    ALL("RoomAll", Color.WHITE, false);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    private final Color wall;

    /**
     * Whether the floor is tiled (else a wooden floor).
     */
    private final boolean tiles;

    RoomPlace(final String key, final Color wall, final boolean tiles) {
        this.key = key;
        this.wall = wall;
        this.tiles = tiles;
    }

    /**
     * @return the objects of the room in the order of its walls, 2 per wall
     */
    List<RoomObject> objects() {
        final List<RoomObject> objects = new ArrayList<>();
        for (final RoomObject object : RoomObject.values()) {
            if (object.getPlace() == this) {
                objects.add(object);
            }
        }
        return objects;
    }

    /**
     * @return the room of a key, the living room for an unknown one
     */
    public static RoomPlace of(final String key) {
        for (final RoomPlace place : values()) {
            if (place.key.equals(key)) {
                return place;
            }
        }
        return LIVING;
    }
}
