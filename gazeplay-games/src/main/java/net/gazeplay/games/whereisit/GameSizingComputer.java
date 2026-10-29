package net.gazeplay.games.whereisit;

import javafx.geometry.Dimension2D;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Computes the layout of the pictures of "Where is it?" for the game area and the grid of the variant.
 */
@AllArgsConstructor
@Slf4j
public class GameSizingComputer {

    /**
     * Smallest size of the configuration button of the games (GameContextFactory.BUTTON_MIN_HEIGHT); it is a tenth of
     * the width of the window, 30 pixels above the bottom (GameContext.updateConfigPane).
     */
    private static final double CONFIG_BUTTON_MIN_SIZE = 64;

    private static final double CONFIG_BUTTON_BOTTOM_MARGIN = 30;

    private final int nbLines;
    private final int nbColumns;

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
        // The band holds the question, and leaves the configuration button out of the grid
        final double bandHeight = Math.min(height * 0.25,
            Math.max(height * 0.14, configButton + CONFIG_BUTTON_BOTTOM_MARGIN + gap));
        final double bandTop = height - bandHeight;

        final double cellWidth = (width - gap * (nbColumns + 1)) / nbColumns;
        final double cellHeight = (bandTop - gap * (nbLines + 1)) / nbLines;

        final GameSizing gameSizing = new GameSizing(cellWidth, cellHeight, gap, bandTop, bandHeight,
            2 * gap + configButton, width);
        log.debug("gameSizing = {}", gameSizing);
        return gameSizing;
    }

}
