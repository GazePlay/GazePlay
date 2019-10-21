package net.gazeplay.games.math101;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class Math104GameVariantGenerator implements GameSpec.GameVariantGenerator {
    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 10"),
            new GameSpec.IntGameVariant(1, "0 to 15"), new GameSpec.IntGameVariant(2, "0 to 20"),
            new GameSpec.IntGameVariant(3, "0 to 30")));
    }
}
