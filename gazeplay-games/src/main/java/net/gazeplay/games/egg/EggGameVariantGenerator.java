package net.gazeplay.games.egg;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Gives the variants of "Egg Game" (maintainer's decisions of 2/11/2026): the number of fixations hatching an egg, 2
 * to 5 (buttons), and what comes out of the eggs at the bottom (see {@link EggContent}; "My images" plays with the
 * images of the user, see {@code UserImagesVariant}). One group only: the window of the variants shows no switch for
 * it.
 */
public class EggGameVariantGenerator implements IGameVariantGenerator {

    static final String GROUP = "EggGame";

    static final String STEPS = "EggSteps";

    static final List<Integer> NUMBERS_OF_STEPS = List.of(2, 3, 4, 5);

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final EggContent content : EggContent.values()) {
            for (final int steps : NUMBERS_OF_STEPS) {
                variants.add(new GroupedGameVariant(GROUP, content.getKey(), STEPS + steps,
                    content == EggContent.MY_IMAGES ? EggContent.USER_IMAGES_FOLDER : null));
            }
        }
        return variants;
    }
}
