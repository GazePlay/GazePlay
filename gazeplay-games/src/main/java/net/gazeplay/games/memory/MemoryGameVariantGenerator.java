package net.gazeplay.games.memory;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Gives the variants of Memory (30/10/2026: one card in the menu instead of six): what the cards show (switches at the
 * top), the size of the grid or the dynamic difficulty (buttons), the cards hidden or visible (switches at the bottom).
 */
public class MemoryGameVariantGenerator implements IGameVariantGenerator {

    static final String HIDDEN = "MemoryHidden";
    static final String VISIBLE = "MemoryVisible";
    static final String DYNAMIC = "Dynamic";

    /**
     * Sizes of the grid (lines x columns); the dynamic difficulty starts with 2 x 2.
     */
    static final List<String> SIZES = List.of("2x2", "2x3", "3x2", "3x4", "4x3");

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final MemoryContent content : MemoryContent.values()) {
            final String folder = content == MemoryContent.USER_IMAGES ? MemoryContent.USER_IMAGES_FOLDER : null;
            for (final String cards : List.of(HIDDEN, VISIBLE)) {
                for (final String size : SIZES) {
                    variants.add(new GroupedGameVariant(content.getKey(), cards, size, folder));
                }
                variants.add(new GroupedGameVariant(content.getKey(), cards, DYNAMIC, folder));
            }
        }
        return variants;
    }
}
