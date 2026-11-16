package net.gazeplay.games.whereisit;

import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The variants of the family "Where is it?" since 16/11/2026: the theme at the top (a switch when the game has several,
 * e.g. Farm, Forest), the end at the bottom (see {@link WhereIsItEnd}), the sizes of the grid as buttons, in a
 * {@link GroupedGameVariant} ({@code GroupedGameVariant:<theme>:<end>:<lines>x<columns>:-:<hearts>}); the hearts
 * (with or without, see {@link LivesVariant}) only with an end.
 * <p>
 * The variants of before (in a shortcut, or a game to replay) are still read, without end as they were:
 * {@code DimensionDifficultyGameVariant:<lines>:<columns>:<theme>} and {@code DimensionGameVariant:<lines>:<columns>}.
 */
public final class WhereIsItVariants {

    private WhereIsItVariants() {
    }

    /**
     * @param sizes       the themes and sizes of the game, as the variants of before (each theme with its sizes)
     * @param singleTheme the theme of a game without themes (no switch is shown for it)
     * @return each theme and size with each end
     */
    public static Set<IGameVariant> withEnds(final List<? extends IGameVariant> sizes, final String singleTheme) {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final IGameVariant size : sizes) {
            for (final WhereIsItEnd end : WhereIsItEnd.values()) {
                for (final String lives : LivesVariant.options(end.getScreens() > 0)) {
                    variants.add(new GroupedGameVariant(theme(size, singleTheme), end.getKey(),
                        lines(size) + "x" + columns(size), null, lives));
                }
            }
        }
        return variants;
    }

    /**
     * @return the theme of a variant, {@code singleTheme} for a game without themes
     */
    public static String theme(final IGameVariant variant, final String singleTheme) {
        if (variant instanceof GroupedGameVariant grouped) {
            return grouped.getGroup();
        }
        if (variant instanceof DimensionDifficultyGameVariant old) {
            return old.getVariant();
        }
        return singleTheme;
    }

    /**
     * @return the lines of the grid of a variant, 2 when unknown
     */
    public static int lines(final IGameVariant variant) {
        if (variant instanceof GroupedGameVariant grouped && grouped.isSize()) {
            return grouped.getLines();
        }
        if (variant instanceof DimensionDifficultyGameVariant old) {
            return old.getWidth();
        }
        if (variant instanceof DimensionGameVariant old) {
            return old.getWidth();
        }
        return 2;
    }

    /**
     * @return the columns of the grid of a variant, 2 when unknown
     */
    public static int columns(final IGameVariant variant) {
        if (variant instanceof GroupedGameVariant grouped && grouped.isSize()) {
            return grouped.getColumns();
        }
        if (variant instanceof DimensionDifficultyGameVariant old) {
            return old.getHeight();
        }
        if (variant instanceof DimensionGameVariant old) {
            return old.getHeight();
        }
        return 2;
    }

    /**
     * @return whether a variant keeps its hearts (see {@link LivesVariant})
     */
    public static boolean withLives(final IGameVariant variant) {
        return LivesVariant.withLives(variant);
    }

    /**
     * @return the end of a variant, without end for a variant of before
     */
    public static WhereIsItEnd end(final IGameVariant variant) {
        return variant instanceof GroupedGameVariant grouped ? WhereIsItEnd.of(grouped.getSubGroup())
            : WhereIsItEnd.ENDLESS;
    }
}
