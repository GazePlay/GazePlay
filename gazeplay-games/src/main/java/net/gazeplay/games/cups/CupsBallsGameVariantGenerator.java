package net.gazeplay.games.cups;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class CupsBallsGameVariantGenerator implements GameSpec.GameVariantGenerator {
    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(
            Lists.newArrayList(new GameSpec.CupsGameVariant(3), new GameSpec.CupsGameVariant(5)));
    }
}
