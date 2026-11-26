package net.gazeplay.games.dice;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Dés" (maintainer's choices of 12/11/2026). At the bottom, the mode (see {@link DiceMode}),
 * throwing first since 26/11/2026.
 * Counting: the levels as buttons (see {@link DiceLevel}), and at the bottom when it ends (see {@link DiceEnd}) and
 * what a wrong total does (see {@link DiceMistakes}: with hearts or without hearts, 17/11/2026). Throwing: 1 to 6 dice as buttons; its single end and way show no
 * switch (the window shows only the rows of the mode chosen with two values or more).
 */
public class DiceGameVariantGenerator implements IGameVariantGenerator {

    /**
     * The most dice of the throwing mode.
     */
    public static final int MOST_DICE = 6;

    /**
     * The end of the throwing mode, which has none, never shown: not one of the ends of counting, so that the window of
     * the variants, opened on throwing, offers counting with its first end (5 totals) and not "without end"
     * (maintainer's feedback of 26/11/2026, since throwing comes first).
     */
    static final String THROW_END = "DiceThrowNoEnd";

    /**
     * @return the key of the button of a number of dice of the throwing mode ("Dice3": 3 dice)
     */
    public static String throwKey(final int dice) {
        return "Dice" + dice;
    }

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        // Throwing first, the playful mode, chosen when the window opens; then counting, which asks to know (tester's
        // feedback of 23/11/2026, point 60)
        for (int dice = 1; dice <= MOST_DICE; dice++) {
            variants.add(new GroupedGameVariant(DiceMode.THROW.getKey(), THROW_END, throwKey(dice),
                null, DiceMistakes.SEARCH_AGAIN.getKey()));
        }
        for (final DiceEnd end : DiceEnd.values()) {
            for (final DiceMistakes mistakes : DiceMistakes.values()) {
                for (final DiceLevel level : DiceLevel.values()) {
                    variants.add(new GroupedGameVariant(DiceMode.COUNT.getKey(), end.getKey(), level.getKey(), null,
                        mistakes.getKey()));
                }
            }
        }
        return variants;
    }
}
