package net.gazeplay.commons.gamevariants.generators;

import net.gazeplay.commons.gamevariants.IGameVariant;

import java.util.LinkedHashSet;
import java.util.Set;

public class NoVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        return new LinkedHashSet<>();
    }
}
