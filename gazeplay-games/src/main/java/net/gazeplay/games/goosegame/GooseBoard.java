package net.gazeplay.games.goosegame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The squares of the board of "Jeu de l'oie" (data/goosegame/gooseboard.png, drawn by gradle/images/make_goose_images.py
 * on the centers of positions.json): 0 is the start, 63 the end. What each square does, and the way of a pawn.
 */
public final class GooseBoard {

    public static final int START = 0;
    public static final int END = 63;
    /**
     * Geese: the pawn moves again by the same throw.
     */
    public static final Set<Integer> GEESE = Set.of(5, 9, 13, 18, 24, 28, 34, 36, 40, 45, 49, 54);
    /**
     * The bridge: from 7, the pawn goes on to 32.
     */
    public static final int BRIDGE = 7;
    public static final int BRIDGE_END = 32;
    /**
     * The inn: the pawn sleeps for two turns.
     */
    public static final int INN = 19;
    public static final int INN_TURNS = 2;
    /**
     * The well and the prison: the pawn waits there until another pawn comes or passes by.
     */
    public static final int WELL = 31;
    public static final int PRISON = 52;
    /**
     * The arrow back: the pawn goes back to the start.
     */
    public static final int BACK = 58;

    /**
     * What a square does to a pawn stopping on it.
     */
    public enum Kind { NORMAL, GOOSE, BRIDGE, INN, WELL, PRISON, BACK, END }

    private GooseBoard() {
    }

    /**
     * @return what a square does with these rules (the simple rules keep only the geese and the end)
     */
    public static Kind kind(final int square, final GooseRules rules) {
        if (square == END) {
            return Kind.END;
        }
        if (GEESE.contains(square)) {
            return Kind.GOOSE;
        }
        if (rules == GooseRules.SIMPLE) {
            return Kind.NORMAL;
        }
        return switch (square) {
            case BRIDGE -> Kind.BRIDGE;
            case INN -> Kind.INN;
            case WELL -> Kind.WELL;
            case PRISON -> Kind.PRISON;
            case BACK -> Kind.BACK;
            default -> Kind.NORMAL;
        };
    }

    /**
     * @return the squares a pawn goes through, one by one, moving forward by a throw from a square: past the end, it
     * moves back by the squares left (the end must be reached exactly)
     */
    public static List<Integer> way(final int from, final int steps) {
        final List<Integer> squares = new ArrayList<>();
        int square = from;
        int direction = 1;
        for (int i = 0; i < steps; i++) {
            if (square == END) {
                direction = -1;
            }
            square += direction;
            squares.add(square);
        }
        return squares;
    }
}
