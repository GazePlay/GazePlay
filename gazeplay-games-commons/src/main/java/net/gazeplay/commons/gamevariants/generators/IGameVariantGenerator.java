package net.gazeplay.commons.gamevariants.generators;

import net.gazeplay.GameSpec;

import java.util.Set;

public interface IGameVariantGenerator {

    default String getVariantChooseText() {
        return "Choose Game Variant";
    }

    Set<GameSpec.GameVariant> getVariants();
}
