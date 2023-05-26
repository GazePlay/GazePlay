package net.gazeplay.games.egg;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class EggGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new IntStringGameVariant(2, "Classic"),
            new IntStringGameVariant(3, "Classic"),
            new IntStringGameVariant(4, "Classic"),
            new IntStringGameVariant(5, "Classic"),
            new IntStringGameVariant(2, "ImageShrink"),
            new IntStringGameVariant(3, "ImageShrink"),
            new IntStringGameVariant(4, "ImageShrink"),
            new IntStringGameVariant(5, "ImageShrink")
        ));
    }

    @Override
    public String getVariantChooseText() {
        return "Choose number of steps";
    }


}
