package net.gazeplay.games.rockPaperScissors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RockPaperScissorsGameVariant {
    HIDE_BO3(false, 2, "BO3"),
    HIDE_BO5(false, 3, "BO5"),
    HIDE_BO7(false, 4, "BO7"),
    HIDE_BO9(false, 5, "BO9"),
    VISIBLE_3(true, 3, "Rounds3"),
    VISIBLE_5(true, 5, "Rounds5"),
    VISIBLE_10(true, 10, "Rounds10");

    @Getter
    private final boolean visible;

    @Getter
    private final int nbRounds;

    @Getter
    private final String label;
}
