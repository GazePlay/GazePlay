package net.gazeplay.commons.gamevariants.generators;

import net.gazeplay.commons.gamevariants.IGameVariant;

import java.util.Set;

/**
 * Gives the variants of a game proposed in the menu.
 */
public interface IGameVariantGenerator {

    /**
     * @return the translation key of the title of the window of the variants
     */
    default String getVariantChooseText() {
        return "Choose Game Variant";
    }

    /**
     * @return the variants, in the order shown
     */
    Set<IGameVariant> getVariants();
}
