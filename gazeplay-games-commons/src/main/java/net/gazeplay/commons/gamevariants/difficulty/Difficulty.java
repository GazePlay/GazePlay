package net.gazeplay.commons.gamevariants.difficulty;

/**
 * Difficulty of a game, shown capitalized ("Easy", "Normal", "Hard").
 */
public enum Difficulty {
    EASY, NORMAL, HARD;

    @Override
    public String toString() {
        String txt = super.toString();
        return txt.substring(0, 1).toUpperCase() + txt.substring(1).toLowerCase();
    }
}
