package net.gazeplay.games.rockPaperScissors;

public enum HandSign {
    ROCK, PAPER, SCISSORS;

    public int figth(HandSign opponentHandSign) {
        return switch (this) {
            case ROCK -> switch (opponentHandSign) {
                case ROCK -> 0;
                case PAPER -> -1;
                case SCISSORS -> 1;
            };
            case PAPER -> switch (opponentHandSign) {
                case ROCK -> 1;
                case PAPER -> 0;
                case SCISSORS -> -1;
            };
            case SCISSORS -> switch (opponentHandSign) {
                case ROCK -> -1;
                case PAPER -> 1;
                case SCISSORS -> 0;
            };
        };
    }

    public double getPosX() {
        return switch (this) {
            case ROCK -> 1.0 / 6.0;
            case PAPER -> 3.0 / 6.0;
            case SCISSORS -> 5.0 / 6.0;
        };
    }

    public String getImagePath() {
        return "data/rockPaperScissors/" + this + ".png";
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
