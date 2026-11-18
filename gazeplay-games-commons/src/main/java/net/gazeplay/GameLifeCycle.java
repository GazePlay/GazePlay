package net.gazeplay;

/**
 * A game, as seen by the game screen: it is launched for each round and disposed between the rounds.
 */
public interface GameLifeCycle {

    /**
     * Launch the game / starts a new game round
     * <p>
     * In case the game is a multi-round game, this method can be called multiple times.
     */
    void launch();

    /**
     * Free resources allocated in the previous game round.
     * <p>
     * Stops any running animation.
     */
    void dispose();

    /**
     * @return whether the menu of the game screen (Esc) offers the settings of the games (fixation length, speed and
     *   size of the elements, progress ring) and Restart; false for an evaluation, whose menu has only the volume,
     *   Resume and Quit (decision of the maintainer of 13/11/2026, done on 17/11/2026)
     */
    default boolean offersGameSettings() {
        return true;
    }

    /**
     * @return the settings of the menu (Esc) that act on this game, for its variant; the others are greyed (17/11/2026).
     *   By default, a fixation with a ring drawn by the game (most of the games)
     */
    default java.util.Set<MenuSetting> menuSettings() {
        return MenuSetting.fixationWithOwnRing();
    }

}
