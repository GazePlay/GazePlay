package net.gazeplay.games.bottle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When the ball is thrown in "Bouteille" (maintainer's decisions of 24/11/2026, tester's feedback point 78, as in Cream
 * pie): as soon as a bottle is looked at (to understand that the gaze makes something happen; the first, so chosen by
 * default) or after a fixation of the length of the settings (to train the fixation). Only to break them all: with
 * targets, as soon as looked at is too hard (the gaze crosses other bottles on its way), so always after a fixation.
 */
@Getter
@RequiredArgsConstructor
public enum BottleThrow {
    LOOK("BottleAtOnce"),
    FIXATION("BottleAfterFixation");

    /**
     * Key of translation, and switch of the variants.
     */
    private final String key;

    /**
     * @return the way of a key among the switches of a variant, after a fixation when none (the variants of before)
     */
    public static BottleThrow of(final java.util.List<String> switches) {
        for (final BottleThrow way : values()) {
            if (switches.contains(way.key)) {
                return way;
            }
        }
        return FIXATION;
    }
}
