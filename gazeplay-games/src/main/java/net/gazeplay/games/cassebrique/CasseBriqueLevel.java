package net.gazeplay.games.cassebrique;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Casse-brique": the number and the size of the bricks (maintainer's decision of 20/11/2026, after a
 * tester's feedback: the game is already hard with the gaze, which follows the ball instead of the paddle; a faster ball
 * made it out of reach). Discovery: one line of 6 very big bricks, to learn the game; easy: 4 lines of 8; normal: 6
 * lines of 13; hard: 8 lines of 15. The ball, as slow as in the easy level of before, and the large paddle (30 % of the
 * screen) are the same in every level; the speed follows the setting of the animation speed. (Before: the level
 * changed the width of the paddle, the speed of the ball, and in hard one brick in three had to be hit twice.)
 */
@Getter
@RequiredArgsConstructor
public enum CasseBriqueLevel {
    DISCOVERY("CasseBriqueDiscovery", 6, 1),
    EASY("CasseBriqueEasy", 8, 4),
    NORMAL("CasseBriqueNormal", 13, 6),
    HARD("CasseBriqueHard", 15, 8);

    /**
     * The width of the paddle, in widths of the screen, in every level.
     */
    public static final double PADDLE_WIDTH = 0.30;

    /**
     * The speed of the ball, in heights of the screen per second at the normal animation speed, in every level.
     */
    public static final double BALL_SPEED = 0.55;

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The bricks of a line of the wall.
     */
    private final int columns;

    /**
     * The lines of bricks of the wall.
     */
    private final int lines;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static CasseBriqueLevel of(final String key) {
        for (final CasseBriqueLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
