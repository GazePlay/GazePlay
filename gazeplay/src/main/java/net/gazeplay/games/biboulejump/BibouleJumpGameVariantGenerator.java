package net.gazeplay.games.biboulejump;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class BibouleJumpGameVariantGenerator implements GameSpec.GameVariantGenerator {
    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(

            new GameSpec.IntGameVariant(0, "With moving platforms"),

            new GameSpec.IntGameVariant(1, "Without moving platforms")

        ));
    }
}
