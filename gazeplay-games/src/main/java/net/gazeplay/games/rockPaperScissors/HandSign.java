package net.gazeplay.games.rockPaperScissors;

import javafx.scene.image.Image;

public enum HandSign {
    UNKNOWN, ROCK, PAPER, SCISSORS;

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

    public double getPosX() {
        return switch (this) {
            case ROCK -> 1.0 / 6.0;
            case PAPER -> 3.0 / 6.0;
            case SCISSORS -> 5.0 / 6.0;
            default -> -1;
        };
    }

    public Image getImage() {
        return new Image("data/rockPaperScissors/" + this + ".png");
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
