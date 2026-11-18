package net.gazeplay.games.whereisit;

import javafx.geometry.Dimension2D;
import lombok.extern.slf4j.Slf4j;

/**
 * Computes the layout of the pictures of "Where is it?" for the game area and the grid of the variant.
 */
@Slf4j
public class GameSizingComputer {

    /**
     * Height of the row of the lives at the top, in part of the height of the game area (16/11/2026): the hearts, as
     * in the other games (height * 0.055, from height * 0.015), and a margin under them.
     */
    static final double LIVES_ROW = 0.085;

    /**
     * Height of the band of the question, in part of the height of the game area. Since the menu of the games opens
     * with Esc (17/11/2026), the band no longer keeps the corner of the old configuration button: it was up to a quarter
     * of the height.
     */
    static final double BAND = 0.17;

    /**
     * Width of the left of the band, for the counter of the screens, in part of the width of the game area.
     */
    static final double BAND_LEFT = 0.15;

    private final int nbLines;
    private final int nbColumns;

    /**
     * Whether the grid leaves room at the top for the lives (variants with an end).
     */
    private final boolean livesRow;

    public GameSizingComputer(final int nbLines, final int nbColumns) {
        this(nbLines, nbColumns, false);
    }

    public GameSizingComputer(final int nbLines, final int nbColumns, final boolean livesRow) {
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.livesRow = livesRow;
    }

    /**
     * @return the layout for a game area
     * @throws IllegalStateException when the area is empty
     */
    public GameSizing computeGameSizing(final Dimension2D dimension2D) {
        final double width = dimension2D.getWidth();
        final double height = dimension2D.getHeight();
        if (width == 0 || height == 0) {
            throw new IllegalStateException("Invalid gaming area size : " + dimension2D);
        }

        final double gap = Math.max(8, Math.min(width, height) * 0.015);
        // The band holds the question, the counter of the screens on its left, the button to hear it again on its right
        final double bandHeight = height * BAND;
        final double bandTop = height - bandHeight;

        final double top = livesRow ? height * LIVES_ROW : 0;
        final double cellWidth = (width - gap * (nbColumns + 1)) / nbColumns;
        final double cellHeight = (bandTop - top - gap * (nbLines + 1)) / nbLines;

        final double bandLeft = Math.max(width * BAND_LEFT, 2 * gap + bandHeight * 0.7);
        final GameSizing gameSizing = new GameSizing(cellWidth, cellHeight, gap, bandTop, bandHeight, bandLeft, width,
            top);
        log.debug("gameSizing = {}", gameSizing);
        return gameSizing;
    }

}
