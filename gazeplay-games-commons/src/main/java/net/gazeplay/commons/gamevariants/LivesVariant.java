package net.gazeplay.commons.gamevariants;

import java.util.List;

/**
 * The choice "with hearts / without hearts" of the games with lives (maintainer's request of 16/11/2026): a switch of
 * their variants in groups ({@link GroupedGameVariant}), the last one, so that the user chooses. With hearts at the
 * start, as before. Without hearts: no hearts shown and the game is never lost; the mistakes are still counted.
 * <p>
 * A variant where the lives do not apply (a mode without mistakes, a game without end) has only the value "without
 * hearts": its row of switches has a single value and is not shown in the window of the variants, which shows a row
 * only with the values possible after the choices above it. A variant of before, without this switch, has its hearts.
 */
public final class LivesVariant {

    /**
     * With hearts (key of translation and value of the switch).
     */
    public static final String WITH = "LivesWith";

    /**
     * Without hearts (key of translation and value of the switch).
     */
    public static final String WITHOUT = "LivesWithout";

    private LivesVariant() {
    }

    /**
     * @param applicable whether the lives apply to the variant (its mode has mistakes, it has an end)
     * @return the values of the switch: without and with hearts, or only without when the lives do not apply. Without
     *   first (20/11/2026): the options go from the easiest to the hardest, and the first one is chosen when the window
     *   of the variants opens (maintainer's decision: the easiest is proposed by default)
     */
    public static List<String> options(final boolean applicable) {
        return applicable ? List.of(WITHOUT, WITH) : List.of(WITHOUT);
    }

    /**
     * @return whether a variant keeps its hearts: not when its switch says "without hearts"; a variant of before, or of
     *   another kind, keeps them
     */
    public static boolean withLives(final IGameVariant variant) {
        return !(variant instanceof GroupedGameVariant grouped && grouped.getExtras().contains(WITHOUT));
    }
}
