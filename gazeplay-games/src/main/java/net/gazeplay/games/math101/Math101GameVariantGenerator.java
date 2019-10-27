package net.gazeplay.games.math101;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class Math101GameVariantGenerator implements GameSpec.GameVariantGenerator {
    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 8"),
            new GameSpec.IntGameVariant(1, "0 to 12"), new GameSpec.IntGameVariant(2, "0 to 20")));
    }
}
