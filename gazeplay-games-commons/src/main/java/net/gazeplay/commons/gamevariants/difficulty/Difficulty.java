package net.gazeplay.commons.gamevariants.difficulty;

public enum Difficulty {
    EASY("Easy"), NORMAL("Normal"), HARD("Hard");

    private final String key;

    Difficulty(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
