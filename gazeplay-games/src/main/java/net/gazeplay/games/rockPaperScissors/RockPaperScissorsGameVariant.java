package net.gazeplay.games.rockPaperScissors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The variants of "Pierre papier ciseaux" before its review of 10/11/2026, kept so that the old variants (saved
 * statistics, shortcuts) are still read: "BO3" (2 rounds to win) is played against the computer to 2 wins, "BO5" to 3,
 * "BO7" and "BO9" to 5; the sign shown in 3 or 5 rounds is played learning the rule to 5 right answers, in 10 rounds to
 * 10.
 */
@Getter
@RequiredArgsConstructor
public enum RockPaperScissorsGameVariant {
    HIDE_BO3(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.WINS_2),
    HIDE_BO5(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.WINS_3),
    HIDE_BO7(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.WINS_5),
    HIDE_BO9(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.WINS_5),
    VISIBLE_3(RockPaperScissorsMode.LEARN, RockPaperScissorsEnd.ANSWERS_5),
    VISIBLE_5(RockPaperScissorsMode.LEARN, RockPaperScissorsEnd.ANSWERS_5),
    VISIBLE_10(RockPaperScissorsMode.LEARN, RockPaperScissorsEnd.ANSWERS_10);

    private final RockPaperScissorsMode mode;
    private final RockPaperScissorsEnd end;

    /**
     * @return the label of the former variants (no longer shown)
     */
    public String getLabel() {
        return name();
    }
}
