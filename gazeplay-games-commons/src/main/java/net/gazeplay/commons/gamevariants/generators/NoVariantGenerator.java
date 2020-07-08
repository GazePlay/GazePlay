package net.gazeplay.commons.gamevariants.generators;

import net.gazeplay.GameSpec;

import java.util.LinkedHashSet;
import java.util.Set;

public class NoVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return new LinkedHashSet<>();
    }
}
