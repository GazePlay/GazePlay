package net.gazeplay.games.whereisit.gamevariantgenerator;

import com.google.common.collect.Lists;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.games.whereisit.WhereIsItVariants;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

/**
 * Gives the variants of the game "WhereIsIt" proposed in the menu: each theme and size of grid with each end (5,
 * 10 or 15 screens, or without end, 16/11/2026; see {@link WhereIsItVariants}).
 */
public class WhereIsItGameVariantGenerator implements IGameVariantGenerator {

    /**
     * The hidden theme of the variants: the name of the game ("Where is the number?" shares these sizes).
     */
    private final String theme;

    public WhereIsItGameVariantGenerator() {
        this("WhereIsIt");
    }

    public WhereIsItGameVariantGenerator(final String theme) {
        this.theme = theme;
    }

    @Override
    public Set<IGameVariant> getVariants() {
        return WhereIsItVariants.withEnds(Lists.newArrayList(
            new DimensionGameVariant(1, 2),
            new DimensionGameVariant(2, 2),
            new DimensionGameVariant(2, 3),
            new DimensionGameVariant(3, 2),
            new DimensionGameVariant(3, 3)
        ), theme);
    }
}
