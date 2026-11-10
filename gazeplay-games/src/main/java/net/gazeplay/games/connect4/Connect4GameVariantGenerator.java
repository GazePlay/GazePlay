package net.gazeplay.games.connect4;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Quatre en ligne" (maintainer's choices of 10/11/2026): when the match ends (buttons, see
 * {@link Connect4End}), and at the bottom the opponent (see {@link Connect4Opponent}). One group only: the window of
 * the variants shows no switch for it. (Before the review: the player or the computer playing first.)
 */
public class Connect4GameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "Connect4";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final Connect4Opponent opponent : Connect4Opponent.values()) {
            for (final Connect4End end : Connect4End.values()) {
                variants.add(new GroupedGameVariant(GROUP, opponent.getKey(), end.getKey(), null));
            }
        }
        return variants;
    }
}
