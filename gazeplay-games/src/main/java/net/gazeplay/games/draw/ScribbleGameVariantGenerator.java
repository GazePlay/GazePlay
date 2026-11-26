package net.gazeplay.games.draw;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Gribouille" (maintainer's choices of 12/11/2026): what it draws on as buttons (see
 * {@link ScribbleBackground}), and at the bottom how the line is colored (see {@link ScribbleMode}) then how it starts
 * and ends (see {@link ScribbleTrace}, 26/11/2026).
 */
public class ScribbleGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final ScribbleMode mode : ScribbleMode.values()) {
            for (final ScribbleTrace trace : ScribbleTrace.values()) {
                for (final ScribbleBackground background : ScribbleBackground.values()) {
                    variants.add(new GroupedGameVariant(mode.getKey(), trace.getKey(), background.getKey(), null));
                }
            }
        }
        return variants;
    }
}
