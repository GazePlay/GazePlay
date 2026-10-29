package net.gazeplay.games.whereisit;

/**
 * Layout of a round of "Where is it?": the grid of the pictures, with a gap between and around them, above a band at
 * the bottom of the game area for the question and the button to hear it again. The left of the band is kept free for
 * the configuration button of the game.
 *
 * @param cellWidth  width of a picture
 * @param cellHeight height of a picture
 * @param gap        space between the pictures and around them
 * @param bandTop    top of the band of the question
 * @param bandHeight height of the band
 * @param bandLeft   left of the free part of the band (right of the configuration button)
 * @param width      width of the game area
 */
record GameSizing(double cellWidth, double cellHeight, double gap, double bandTop, double bandHeight, double bandLeft,
                  double width) {

    /**
     * @return the left of the picture of this column
     */
    double cellX(final int column) {
        return gap + column * (cellWidth + gap);
    }

    /**
     * @return the top of the picture of this line
     */
    double cellY(final int line) {
        return gap + line * (cellHeight + gap);
    }

    /**
     * @return the size of the button to hear the question again, at the right of the band
     */
    double replayButtonSize() {
        return bandHeight * 0.7;
    }

    /**
     * @return the horizontal center of the free part of the band, between the configuration button and the button to
     * hear the question again
     */
    double bandCenterX() {
        return (bandLeft + bandRight()) / 2;
    }

    /**
     * @return the right of the free part of the band
     */
    double bandRight() {
        return width - 2 * gap - replayButtonSize();
    }
}
