package net.gazeplay.commons.gamevariants.difficulty;

public enum Difficulty {
    EASY, NORMAL, HARD;

    @Override
    public String toString() {
        String txt = super.toString();
        return txt.substring(0, 1).toUpperCase() + txt.substring(1).toLowerCase();
    }
}
