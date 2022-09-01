package net.gazeplay.games.bottle;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class BottleGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new IntStringGameVariant(4, "NormalBottles"),
            new IntStringGameVariant(8, "NormalBottles"),
            new IntStringGameVariant(12, "NormalBottles"),
            new IntStringGameVariant(16, "NormalBottles"),
            new IntStringGameVariant(4, "BigBottles"),
            new IntStringGameVariant(8, "BigBottles"),
            new IntStringGameVariant(12, "BigBottles"),
            new IntStringGameVariant(16, "BigBottles"),
            new IntStringGameVariant(4, "SmallBottles"),
            new IntStringGameVariant(8, "SmallBottles"),
            new IntStringGameVariant(12, "SmallBottles"),
            new IntStringGameVariant(16, "SmallBottles"),
            new IntStringGameVariant(4, "HighBottles"),
            new IntStringGameVariant(8, "HighBottles"),
            new IntStringGameVariant(12, "HighBottles"),
            new IntStringGameVariant(16, "HighBottles"),
            new IntStringGameVariant(4, "TinyBottles"),
            new IntStringGameVariant(8, "TinyBottles"),
            new IntStringGameVariant(12, "TinyBottles"),
            new IntStringGameVariant(16, "TinyBottles"),
            new IntStringGameVariant(4, "InfinityBottles"),
            new IntStringGameVariant(8, "InfinityBottles"),
            new IntStringGameVariant(12, "InfinityBottles"),
            new IntStringGameVariant(16, "InfinityBottles")
        ));
    }
}
