package net.gazeplay.games.rockPaperScissors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Pierre papier ciseaux" ends (maintainer's choices of 10/11/2026): against the computer, the first to win 2, 3
 * or 5 rounds; learning the rule, after 5 or 10 right answers; or never.
 */
@Getter
@RequiredArgsConstructor
public enum RockPaperScissorsEnd {
    WINS_2("RpsWins2", 2, RockPaperScissorsMode.VERSUS),
    WINS_3("RpsWins3", 3, RockPaperScissorsMode.VERSUS),
    WINS_5("RpsWins5", 5, RockPaperScissorsMode.VERSUS),
    ANSWERS_5("RpsAnswers5", 5, RockPaperScissorsMode.LEARN),
    ANSWERS_10("RpsAnswers10", 10, RockPaperScissorsMode.LEARN),
    ENDLESS("RpsEndless", 0, null);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The rounds to win (against the computer) or the right answers (learning), 0 without end.
     */
    private final int count;

    /**
     * The mode it is given for, null for both.
     */
    private final RockPaperScissorsMode mode;

    boolean isFor(final RockPaperScissorsMode chosen) {
        return mode == null || mode == chosen;
    }

    /**
     * @return the end of a key, 3 rounds won for an unknown one
     */
    public static RockPaperScissorsEnd of(final String key) {
        for (final RockPaperScissorsEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return WINS_3;
    }
}
