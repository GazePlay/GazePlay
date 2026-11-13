package net.gazeplay.games.ladder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Échelle" ends (13/11/2026): after 3, 5 or 10 games, or never.
 */
@Getter
@RequiredArgsConstructor
public enum LadderEnd {
    GAMES_3("LadderEnd3", 3),
    GAMES_5("LadderEnd5", 5),
    GAMES_10("LadderEnd10", 10),
    ENDLESS("LadderEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The games to play, 0 without end.
     */
    private final int games;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static LadderEnd of(final String key) {
        for (final LadderEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
