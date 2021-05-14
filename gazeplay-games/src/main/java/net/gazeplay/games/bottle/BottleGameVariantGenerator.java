package net.gazeplay.games.bottle;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class BottleGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new IntStringGameVariant(4, "Normal"),
            new IntStringGameVariant(8, "Normal"),
            new IntStringGameVariant(12, "Normal"),
            new IntStringGameVariant(16, "Normal"),
            new IntStringGameVariant(4, "Big"),
            new IntStringGameVariant(8, "Big"),
            new IntStringGameVariant(12, "Big"),
            new IntStringGameVariant(16, "Big"),
            new IntStringGameVariant(4, "Small"),
            new IntStringGameVariant(8, "Small"),
            new IntStringGameVariant(12, "Small"),
            new IntStringGameVariant(16, "Small"),
            new IntStringGameVariant(4, "High"),
            new IntStringGameVariant(8, "High"),
            new IntStringGameVariant(12, "High"),
            new IntStringGameVariant(16, "High"),
            new IntStringGameVariant(4, "Tiny"),
            new IntStringGameVariant(8, "Tiny"),
            new IntStringGameVariant(12, "Tiny"),
            new IntStringGameVariant(16, "Tiny")
        ));
    }
}
