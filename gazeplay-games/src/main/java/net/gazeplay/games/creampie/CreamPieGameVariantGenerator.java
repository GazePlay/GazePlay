package net.gazeplay.games.creampie;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Tartes à la crème" (maintainer's choices of 12/11/2026): the pictures at the top (see
 * {@link CreamPiePictures}; "My images" plays with the images of the user), the size of the targets as buttons (see
 * {@link CreamPieSize}), and at the bottom when the pie is thrown (see {@link CreamPieThrow}), whether the targets move
 * (see {@link CreamPieMotion}) and the end (see {@link CreamPieEnd}).
 */
public class CreamPieGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final CreamPiePictures pictures : CreamPiePictures.values()) {
            for (final CreamPieThrow way : CreamPieThrow.values()) {
                for (final CreamPieMotion motion : CreamPieMotion.values()) {
                    for (final CreamPieEnd end : CreamPieEnd.values()) {
                        for (final CreamPieSize size : CreamPieSize.values()) {
                            variants.add(new GroupedGameVariant(pictures.getKey(), way.getKey(), size.getKey(),
                                pictures == CreamPiePictures.MY_IMAGES ? CreamPiePictures.USER_IMAGES_FOLDER : null,
                                motion.getKey(), end.getKey()));
                        }
                    }
                }
            }
        }
        return variants;
    }
}
