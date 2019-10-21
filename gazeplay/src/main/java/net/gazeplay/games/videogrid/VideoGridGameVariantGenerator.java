package net.gazeplay.games.videogrid;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class VideoGridGameVariantGenerator implements GameSpec.GameVariantGenerator {
    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(

            new GameSpec.DimensionGameVariant(2, 1),

            new GameSpec.DimensionGameVariant(2, 2),

            new GameSpec.DimensionGameVariant(3, 3),

            new GameSpec.DimensionGameVariant(4, 4)

        ));
    }
}
