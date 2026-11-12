package net.gazeplay.games.goosegame;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Jeu de l'oie" (maintainer's choices of 12/11/2026): who plays as buttons (see
 * {@link GoosePlayers}), and at the bottom the rules (see {@link GooseRules}).
 */
public class GooseGameVariantGenerator implements IGameVariantGenerator {

    /**
     * The single sub-group of the variants (no switch).
     */
    static final String BOARD = "GooseBoard";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final GooseRules rules : GooseRules.values()) {
            for (final GoosePlayers players : GoosePlayers.values()) {
                variants.add(new GroupedGameVariant(rules.getKey(), BOARD, players.getKey(), null));
            }
        }
        return variants;
    }
}
