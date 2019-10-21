package net.gazeplay.games.cakes;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class CakesGameVariantGenerator implements GameSpec.GameVariantGenerator {
    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(

            new GameSpec.IntGameVariant(0, "free"),

            new GameSpec.IntGameVariant(1, "normal"),

            new GameSpec.IntGameVariant(2, "extreme")

        ));
    }
}
