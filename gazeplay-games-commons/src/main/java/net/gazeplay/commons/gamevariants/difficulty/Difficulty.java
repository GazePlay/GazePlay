package net.gazeplay.commons.gamevariants.difficulty;

public enum Difficulty {
    EASY("easy"), NORMAL("normal"), HARD("hard");

    private final String key;

    Difficulty(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
