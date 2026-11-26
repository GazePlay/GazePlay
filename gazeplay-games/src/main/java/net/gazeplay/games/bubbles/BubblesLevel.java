package net.gazeplay.games.bubbles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;

/**
 * How hard "Bubbles" is (tester's feedback of 23/11/2026, point 49; maintainer's choice of 26/11/2026: three levels):
 * the panes hiding the picture (a square of them) and how big the bubbles are. Hard is the game of before. The easiest
 * first, chosen when the window of the variants opens (maintainer's rule of 20/11/2026).
 */
@Getter
@RequiredArgsConstructor
public enum BubblesLevel {
    EASY("Easy", 3, 1.5),
    MEDIUM("Normal", 4, 1.2),
    HARD("Hard", 6, 1);

    /**
     * Key of translation (shared with other games), and value of the switch of the variants.
     */
    private final String key;
    /**
     * The panes on a side of the picture.
     */
    private final int side;
    /**
     * How much bigger the bubbles are than in the game of before.
     */
    private final double bubbleScale;

    /**
     * @return the panes hiding the picture
     */
    public int panes() {
        return side * side;
    }

    /**
     * @return the level of a variant, hard (the game of before) for a variant without it
     */
    public static BubblesLevel of(final IGameVariant variant) {
        if (variant instanceof GroupedGameVariant grouped) {
            for (final BubblesLevel level : values()) {
                if (grouped.getExtras().contains(level.key)) {
                    return level;
                }
            }
        }
        return HARD;
    }
}
