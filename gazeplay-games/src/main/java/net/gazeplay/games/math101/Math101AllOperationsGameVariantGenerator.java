package net.gazeplay.games.math101;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.MathGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

/**
 * Gives the variants of the game "Math101: All operations" proposed in the menu.
 */
public class Math101AllOperationsGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new MathGameVariant(0, 5),
            new MathGameVariant(0, 10),
            new MathGameVariant(0, 15),
            new MathGameVariant(0, 20)
        ));
    }
}
