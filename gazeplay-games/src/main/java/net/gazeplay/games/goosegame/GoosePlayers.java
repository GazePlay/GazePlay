package net.gazeplay.games.goosegame;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Who plays "Jeu de l'oie" (maintainer's choice of 12/11/2026, as the horses): one person against 1 to 3 pawns of the
 * computer, or 2 to 4 persons taking turns with the same eye tracker or mouse.
 */
@Getter
@RequiredArgsConstructor
public enum GoosePlayers {
    VS_COMPUTER_1("GooseVsComputer1", 2, 1),
    VS_COMPUTER_2("GooseVsComputer2", 3, 1),
    VS_COMPUTER_3("GooseVsComputer3", 4, 1),
    PLAYERS_2("GoosePlayers2", 2, 2),
    PLAYERS_3("GoosePlayers3", 3, 3),
    PLAYERS_4("GoosePlayers4", 4, 4);

    /**
     * Key of translation, and value (the button) of the variants.
     */
    private final String key;

    /**
     * The pawns on the board.
     */
    private final int pawns;

    /**
     * The pawns played by persons, the first ones; the others are played by the computer.
     */
    private final int humans;

    /**
     * @return whether a person plays against the computer
     */
    public boolean againstComputer() {
        return humans < pawns;
    }

    /**
     * @return the players of a key, one against one computer for an unknown one
     */
    public static GoosePlayers of(final String key) {
        for (final GoosePlayers players : values()) {
            if (players.key.equals(key)) {
                return players;
            }
        }
        return VS_COMPUTER_1;
    }

    /**
     * @return the players of a former variant (2 to 5 persons, "IntGameVariant"): that many persons, 4 at most
     */
    public static GoosePlayers persons(final int number) {
        return switch (Math.max(2, Math.min(4, number))) {
            case 2 -> PLAYERS_2;
            case 3 -> PLAYERS_3;
            default -> PLAYERS_4;
        };
    }
}
