package net.gazeplay.games.slidingpuzzle;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class PuzzleGameVariantGenerator implements GameSpec.GameVariantGenerator {
    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(1, "Numbers"),
            new GameSpec.IntGameVariant(2, "Mona Lisa"), new GameSpec.IntGameVariant(3, "Fish"),
            new GameSpec.IntGameVariant(4, "Biboule")));
    }
}
