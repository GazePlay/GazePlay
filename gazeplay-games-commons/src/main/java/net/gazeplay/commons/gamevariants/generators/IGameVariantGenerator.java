package net.gazeplay.commons.gamevariants.generators;

import net.gazeplay.commons.gamevariants.IGameVariant;

import java.util.Set;

public interface IGameVariantGenerator {

    default String getVariantChooseText() {
        return "Choose Game Variant";
    }

    Set<IGameVariant> getVariants();
}
