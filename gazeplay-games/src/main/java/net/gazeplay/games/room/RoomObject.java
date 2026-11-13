package net.gazeplay.games.room;

import lombok.Getter;

/**
 * The objects of the rooms of "La pièce" (13/11/2026), OpenMoji pictures (CC BY-SA 4.0, data/room/objects, made by
 * gradle/images/make_room_images.py), 8 per room, 2 per wall in the order of the walls: where each one stands (see
 * {@link Kind}) and its height as a part of the height of the game.
 */
@Getter
public enum RoomObject {
    WINDOW(RoomPlace.LIVING, "window", Kind.WALL, 0.3),
    COUCH(RoomPlace.LIVING, "couch", Kind.FLOOR, 0.34),
    TELEVISION(RoomPlace.LIVING, "television", Kind.TABLE, 0.2),
    PLANT(RoomPlace.LIVING, "plant", Kind.FLOOR, 0.3),
    CLOCK(RoomPlace.LIVING, "clock", Kind.SHELF, 0.15),
    BOOKS(RoomPlace.LIVING, "books", Kind.TABLE, 0.16),
    DOOR(RoomPlace.LIVING, "door", Kind.FLOOR, 0.52),
    CHAIR(RoomPlace.LIVING, "chair", Kind.FLOOR, 0.28),

    PAN(RoomPlace.KITCHEN, "pan", Kind.COUNTER, 0.16),
    TEAPOT(RoomPlace.KITCHEN, "teapot", Kind.COUNTER, 0.16),
    CUP(RoomPlace.KITCHEN, "cup", Kind.SHELF, 0.13),
    BOWL(RoomPlace.KITCHEN, "bowl", Kind.COUNTER, 0.14),
    APPLE(RoomPlace.KITCHEN, "apple", Kind.COUNTER, 0.14),
    BREAD(RoomPlace.KITCHEN, "bread", Kind.SHELF, 0.13),
    MILK(RoomPlace.KITCHEN, "milk", Kind.COUNTER, 0.16),
    SPOON(RoomPlace.KITCHEN, "spoon", Kind.SHELF, 0.15),

    BED(RoomPlace.BEDROOM, "bed", Kind.FLOOR, 0.3),
    TEDDY(RoomPlace.BEDROOM, "teddy", Kind.FLOOR, 0.18),
    ALARM(RoomPlace.BEDROOM, "alarm", Kind.TABLE, 0.15),
    TSHIRT(RoomPlace.BEDROOM, "tshirt", Kind.WALL, 0.2),
    SOCKS(RoomPlace.BEDROOM, "socks", Kind.FLOOR, 0.14),
    BOOK(RoomPlace.BEDROOM, "book", Kind.SHELF, 0.13),
    BALL(RoomPlace.BEDROOM, "ball", Kind.FLOOR, 0.15),
    BULB(RoomPlace.BEDROOM, "bulb", Kind.CEILING, 0.16),

    BATHTUB(RoomPlace.BATHROOM, "bathtub", Kind.FLOOR, 0.3),
    SHOWER(RoomPlace.BATHROOM, "shower", Kind.WALL, 0.22),
    TOILET(RoomPlace.BATHROOM, "toilet", Kind.FLOOR, 0.3),
    PAPER(RoomPlace.BATHROOM, "paper", Kind.WALL, 0.14),
    MIRROR(RoomPlace.BATHROOM, "mirror", Kind.WALL, 0.24),
    SOAP(RoomPlace.BATHROOM, "soap", Kind.SHELF, 0.11),
    TOOTHBRUSH(RoomPlace.BATHROOM, "toothbrush", Kind.SHELF, 0.13),
    SPONGE(RoomPlace.BATHROOM, "sponge", Kind.TABLE, 0.11);

    /**
     * Where an object stands: on the floor, hung on the wall, on a wall shelf, on a small table, on a kitchen counter,
     * or hanging from the ceiling (all drawn by the game but the objects).
     */
    enum Kind { FLOOR, WALL, SHELF, TABLE, COUNTER, CEILING }

    private final RoomPlace place;
    private final String file;
    private final Kind kind;
    private final double size;

    RoomObject(final RoomPlace place, final String file, final Kind kind, final double size) {
        this.place = place;
        this.file = file;
        this.kind = kind;
        this.size = size;
    }

    /**
     * @return the picture of the object
     */
    String picture() {
        return "data/room/objects/" + file + ".png";
    }

    /**
     * @return the key of its name ("Le canapé")
     */
    String nameKey() {
        return "Room" + camel() + "Name";
    }

    /**
     * @return the key of the question asking for it ("Où est le canapé ?")
     */
    String questionKey() {
        return "RoomWhere" + camel();
    }

    private String camel() {
        final StringBuilder out = new StringBuilder();
        for (final String part : name().split("_")) {
            out.append(part.charAt(0)).append(part.substring(1).toLowerCase());
        }
        return out.toString();
    }

    /**
     * @return its wall in its room, 0 to 3
     */
    int wall() {
        return place.objects().indexOf(this) / 2;
    }

    /**
     * @return its place on its wall, 0 at the left, 1 at the right
     */
    int slot() {
        return place.objects().indexOf(this) % 2;
    }
}
