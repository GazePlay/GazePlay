package net.gazeplay.games.rockPaperScissors;

public enum HandSign {
    ROCK, PAPER, SCISSORS;

    public String imagePath() {
        return "data/rockPaperScissors/" + this + ".png";
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
