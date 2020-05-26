package net.gazeplay.games.whereisit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class WhereIsItGameVariantGenerator implements GameSpec.GameVariantGenerator {
    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new GameSpec.IntGameVariant(4),
            new GameSpec.IntGameVariant(6),
            new GameSpec.IntGameVariant(8)
        ));
    }
}
