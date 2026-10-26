package net.gazeplay.games.rockPaperScissors;

import javafx.scene.image.Image;

/**
 * The signs of rock paper scissors, and the hidden sign of the opponent before it plays.
 */
public enum HandSign {
    ROCK, PAPER, SCISSORS, UNKNOWN;

    /**
     * @return 1 when this sign beats the other one, -1 when it loses, 0 for a draw
     */
    public int fight(HandSign opponentHandSign) {
        return switch (this) {
            case ROCK -> switch (opponentHandSign) {
                case ROCK -> 0;
                case PAPER -> -1;
                case SCISSORS -> 1;
                default -> 1;
            };
            case PAPER -> switch (opponentHandSign) {
                case ROCK -> 1;
                case PAPER -> 0;
                case SCISSORS -> -1;
                default -> 1;
            };
            case SCISSORS -> switch (opponentHandSign) {
                case ROCK -> -1;
                case PAPER -> 1;
                case SCISSORS -> 0;
                default -> 1;
            };
            default -> -1;
        };
    }

    /**
     * @return the horizontal position of the button of the sign, as a ratio of the width of the game area
     */
    public double getPosX() {
        return switch (this) {
            case ROCK -> 1.0 / 6.0;
            case PAPER -> 3.0 / 6.0;
            case SCISSORS -> 5.0 / 6.0;
            default -> -1;
        };
    }

    /**
     * @return the picture of the sign
     */
    public Image getImage() {
        return new Image("data/rockPaperScissors/" + this + ".png");
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
