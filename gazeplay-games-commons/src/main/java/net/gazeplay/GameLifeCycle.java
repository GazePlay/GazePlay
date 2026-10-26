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

}
