package net.gazeplay.games.whereisit.gamevariantgenerator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

/**
 * Gives the variants of the game "WhereIsIt" proposed in the menu.
 */
public class WhereIsItGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new DimensionGameVariant(1, 2),
            new DimensionGameVariant(2, 2),
            new DimensionGameVariant(2, 3),
            new DimensionGameVariant(3, 2),
            new DimensionGameVariant(3, 3)
        ));
    }
}
