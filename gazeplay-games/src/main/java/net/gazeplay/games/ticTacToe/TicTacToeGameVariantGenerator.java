package net.gazeplay.games.ticTacToe;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of the Morpion (maintainer's choices of 10/11/2026): when the match ends (buttons, see
 * {@link TicTacToeEnd}), and at the bottom the opponent (see {@link TicTacToeOpponent}). One group only: the window of
 * the variants shows no switch for it. (Before the review: two players, or against the computer.)
 */
public class TicTacToeGameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "TicTacToe";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final TicTacToeOpponent opponent : TicTacToeOpponent.values()) {
            for (final TicTacToeEnd end : TicTacToeEnd.values()) {
                variants.add(new GroupedGameVariant(GROUP, opponent.getKey(), end.getKey(), null));
            }
        }
        return variants;
    }
}
