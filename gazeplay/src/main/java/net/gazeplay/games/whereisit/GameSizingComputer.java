package net.gazeplay.games.whereisit;

import javafx.geometry.Rectangle2D;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class GameSizingComputer {

    private final int nbLines;
    private final int nbColumns;
    private final boolean fourThree;

    public GameSizing computeGameSizing() {
        Rectangle2D bounds = javafx.stage.Screen.getPrimary().getBounds();

        double screenWidth = bounds.getWidth();
        double screenHeight = bounds.getHeight();

        final double width;
        final double height;
        final double shift;

        log.info("16/9 or 16/10 screen ? = " + ((screenWidth / screenHeight) - (16.0 / 9.0)));

        if (fourThree && ((screenWidth / screenHeight) - (16.0 / 9.0)) < 0.1) {
            width = 4 * screenHeight / 3;
            height = screenHeight;
            shift = (screenWidth - width) / 2;
        } else {
            width = screenWidth;
            height = screenHeight;
            shift = 0;
        }

        return new GameSizing(width / nbColumns, height / nbLines, shift);
    }

}
