package net.gazeplay.games.bottle;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class BottleGameVariantGenerator implements GameSpec.GameVariantGenerator {
    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new GameSpec.IntGameVariant(4),
            new GameSpec.IntGameVariant(8),
            new GameSpec.IntGameVariant(16)
        ));
    }
}
