package net.gazeplay.commons.gamevariants.generators;

import net.gazeplay.commons.gamevariants.IGameVariant;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * For the games without variants: the game starts directly.
 */
public class NoVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        return new LinkedHashSet<>();
    }
}
