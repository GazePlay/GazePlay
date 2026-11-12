package net.gazeplay.games.goosegame;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The rules of "Jeu de l'oie" (maintainer's choice of 12/11/2026): classic (all the special squares of the board), or
 * simple, for the youngest (only the geese, and the end to reach exactly).
 */
@Getter
@RequiredArgsConstructor
public enum GooseRules {
    CLASSIC("GooseClassic"),
    SIMPLE("GooseSimple");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the rules of a key, classic for an unknown one
     */
    public static GooseRules of(final String key) {
        for (final GooseRules rules : values()) {
            if (rules.key.equals(key)) {
                return rules;
            }
        }
        return CLASSIC;
    }
}
