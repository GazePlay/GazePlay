package net.gazeplay.games.trainSwitches;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class TrainSwitchesGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new IntStringGameVariant(3, "ClassicTrain"),
            new IntStringGameVariant(3, "PauseTrain"),
            new IntStringGameVariant(3, "InfiniteTrain"),
            new IntStringGameVariant(3, "UniqueTrain"),
            new IntStringGameVariant(8, "ClassicTrain"),
            new IntStringGameVariant(8, "PauseTrain"),
            new IntStringGameVariant(8, "InfiniteTrain"),
            new IntStringGameVariant(8, "UniqueTrain"),
            new IntStringGameVariant(13, "ClassicTrain"),
            new IntStringGameVariant(13, "PauseTrain"),
            new IntStringGameVariant(13, "InfiniteTrain"),
            new IntStringGameVariant(13, "UniqueTrain")
        ));
    }
}
