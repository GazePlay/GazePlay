package net.gazeplay.games.slidingpuzzle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of the sliding puzzle (maintainer's choices of 9/11/2026): when it ends (buttons, see
 * {@link PuzzleEnd}), and at the bottom what the pieces show (see {@link PuzzleContent}) and the level (see
 * {@link PuzzleLevel}).
 */
public class PuzzleGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final PuzzleContent content : PuzzleContent.values()) {
            for (final PuzzleLevel level : PuzzleLevel.values()) {
                for (final PuzzleEnd end : PuzzleEnd.values()) {
                    variants.add(new GroupedGameVariant(content.getKey(), level.getKey(), end.getKey(), null));
                }
            }
        }
        return variants;
    }

    /**
     * The 4 variants before the review of 9/11/2026, kept (with their name and place) so that the old variants (saved
     * statistics, shortcuts) are still read: the numbers, or the pictures of the game, at the normal level, without end.
     */
    @Getter
    @RequiredArgsConstructor
    public enum PuzzleGameVariant {
        NUMBERS(PuzzleContent.NUMBERS),
        MONA_LISA(PuzzleContent.PICTURES),
        FISH(PuzzleContent.PICTURES),
        BIBOULE(PuzzleContent.PICTURES);

        private final PuzzleContent content;
    }
}
