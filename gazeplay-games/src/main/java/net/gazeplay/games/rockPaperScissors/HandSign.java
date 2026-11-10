package net.gazeplay.games.rockPaperScissors;

import javafx.scene.image.Image;

/**
 * The signs of rock paper scissors: the rock breaks the scissors, the paper wraps the rock, the scissors cut the paper.
 */
public enum HandSign {
    ROCK("RpsRock"), PAPER("RpsPaper"), SCISSORS("RpsScissors");

    /**
     * Key of translation of its name.
     */
    private final String key;

    HandSign(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    /**
     * @return 1 when this sign beats the other one, -1 when it loses, 0 for a draw
     */
    public int fight(final HandSign other) {
        if (this == other) {
            return 0;
        }
        return beaten() == other ? 1 : -1;
    }

    /**
     * @return the sign this one beats
     */
    public HandSign beaten() {
        return switch (this) {
            case ROCK -> SCISSORS;
            case PAPER -> ROCK;
            case SCISSORS -> PAPER;
        };
    }

    /**
     * @return the sign that beats this one
     */
    public HandSign winner() {
        return switch (this) {
            case ROCK -> PAPER;
            case PAPER -> SCISSORS;
            case SCISSORS -> ROCK;
        };
    }

    /**
     * @return the key of translation of why this sign beats the one it beats ("the paper wraps the rock")
     */
    public String ruleKey() {
        return switch (this) {
            case ROCK -> "RpsRuleRock";
            case PAPER -> "RpsRulePaper";
            case SCISSORS -> "RpsRuleScissors";
        };
    }

    /**
     * @return the picture of the sign (an emoji of OpenMoji)
     */
    public Image getImage() {
        return new Image("data/rockPaperScissors/" + this + ".png");
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
