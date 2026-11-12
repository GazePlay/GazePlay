package net.gazeplay.games.dice;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Dés" (maintainer's choices of 12/11/2026). At the bottom, the mode (see {@link DiceMode}).
 * Counting: the levels as buttons (see {@link DiceLevel}), and at the bottom when it ends (see {@link DiceEnd}) and
 * what a wrong total does (see {@link DiceMistakes}). Throwing: 1 to 6 dice as buttons; its single end and way show no
 * switch (the window shows only the rows of the mode chosen with two values or more).
 */
public class DiceGameVariantGenerator implements IGameVariantGenerator {

    /**
     * The most dice of the throwing mode.
     */
    public static final int MOST_DICE = 6;

    /**
     * @return the key of the button of a number of dice of the throwing mode ("Dice3": 3 dice)
     */
    public static String throwKey(final int dice) {
        return "Dice" + dice;
    }

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final DiceEnd end : DiceEnd.values()) {
            for (final DiceMistakes mistakes : DiceMistakes.values()) {
                for (final DiceLevel level : DiceLevel.values()) {
                    variants.add(new GroupedGameVariant(DiceMode.COUNT.getKey(), end.getKey(), level.getKey(), null,
                        mistakes.getKey()));
                }
            }
        }
        for (int dice = 1; dice <= MOST_DICE; dice++) {
            variants.add(new GroupedGameVariant(DiceMode.THROW.getKey(), DiceEnd.ENDLESS.getKey(), throwKey(dice),
                null, DiceMistakes.SEARCH_AGAIN.getKey()));
        }
        return variants;
    }
}
