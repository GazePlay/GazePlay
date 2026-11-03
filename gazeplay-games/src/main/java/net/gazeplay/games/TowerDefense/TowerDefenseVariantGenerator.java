package net.gazeplay.games.TowerDefense;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Tower Defense" (maintainer's rules of 3/11/2026): when it is won (buttons, see
 * {@link TowerDefenseEnd}), and at the bottom how the waves start (see {@link TowerDefenseWaves}) and the level (see
 * {@link TowerDefenseLevel}). The map is drawn at random (no room left for it in the variants).
 */
public class TowerDefenseVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final TowerDefenseWaves waves : TowerDefenseWaves.values()) {
            for (final TowerDefenseLevel level : TowerDefenseLevel.values()) {
                for (final TowerDefenseEnd end : TowerDefenseEnd.values()) {
                    variants.add(new GroupedGameVariant(waves.getKey(), level.getKey(), end.getKey(), null));
                }
            }
        }
        return variants;
    }
}
