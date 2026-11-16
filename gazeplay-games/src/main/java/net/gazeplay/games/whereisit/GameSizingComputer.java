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
     * Smallest size of the free corner left of the band: the place of the old configuration button (a tenth of the width
     * of the window, 30 pixels above the bottom), kept for the layout until the redesign of the menu of the games.
     */
    private static final double CONFIG_BUTTON_MIN_SIZE = 64;

    private static final double CONFIG_BUTTON_BOTTOM_MARGIN = 30;

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
        final double configButton = Math.max(width / 10, CONFIG_BUTTON_MIN_SIZE);
        // The band holds the question, and keeps the corner of the old configuration button out of the grid
        final double bandHeight = Math.min(height * 0.25,
            Math.max(height * 0.14, configButton + CONFIG_BUTTON_BOTTOM_MARGIN + gap));
        final double bandTop = height - bandHeight;

        final double top = livesRow ? height * LIVES_ROW : 0;
        final double cellWidth = (width - gap * (nbColumns + 1)) / nbColumns;
        final double cellHeight = (bandTop - top - gap * (nbLines + 1)) / nbLines;

        final GameSizing gameSizing = new GameSizing(cellWidth, cellHeight, gap, bandTop, bandHeight,
            2 * gap + configButton, width, top);
        log.debug("gameSizing = {}", gameSizing);
        return gameSizing;
    }

}
