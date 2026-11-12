package net.gazeplay.games.literacy;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Identifier les lettres" (maintainer's choices of 12/11/2026): the level as buttons (see
 * {@link LettersLevel}), and at the bottom the pictures (see {@link LettersPictures}; "My images" plays with the images
 * of the user), how the letters are written (see {@link LettersCase}) and the end (see {@link LettersEnd}).
 */
public class LettersGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final LettersPictures pictures : LettersPictures.values()) {
            for (final LettersCase letterCase : LettersCase.values()) {
                for (final LettersEnd end : LettersEnd.values()) {
                    for (final LettersLevel level : LettersLevel.values()) {
                        variants.add(new GroupedGameVariant(pictures.getKey(), letterCase.getKey(), level.getKey(),
                            pictures == LettersPictures.MY_IMAGES ? LettersPictures.USER_IMAGES_FOLDER : null,
                            end.getKey()));
                    }
                }
            }
        }
        return variants;
    }
}
