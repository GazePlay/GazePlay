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
            new IntStringGameVariant(4, "NormalB"),
            new IntStringGameVariant(8, "NormalB"),
            new IntStringGameVariant(12, "NormalB"),
            new IntStringGameVariant(16, "NormalB"),
            new IntStringGameVariant(4, "BigB"),
            new IntStringGameVariant(8, "BigB"),
            new IntStringGameVariant(12, "BigB"),
            new IntStringGameVariant(16, "BigB"),
            new IntStringGameVariant(4, "SmallB"),
            new IntStringGameVariant(8, "SmallB"),
            new IntStringGameVariant(12, "SmallB"),
            new IntStringGameVariant(16, "SmallB"),
            new IntStringGameVariant(4, "HighB"),
            new IntStringGameVariant(8, "HighB"),
            new IntStringGameVariant(12, "HighB"),
            new IntStringGameVariant(16, "HighB"),
            new IntStringGameVariant(4, "TinyB"),
            new IntStringGameVariant(8, "TinyB"),
            new IntStringGameVariant(12, "TinyB"),
            new IntStringGameVariant(16, "TinyB"),
            new IntStringGameVariant(4, "InfinityB"),
            new IntStringGameVariant(8, "InfinityB"),
            new IntStringGameVariant(12, "InfinityB"),
            new IntStringGameVariant(16, "InfinityB")
        ));
    }
}
