package net.gazeplay.games.gazeplayEval.round;

import net.gazeplay.games.gazeplayEval.GameState;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
class GameSizing {
    public static double width;
    public static double height;
    public static double shift;

    public static void computeGameSizing(int rows, int cols, Scene scene) {
        Rectangle2D bounds = new Rectangle2D(0, 0, scene.getWidth(), scene.getHeight());
        computeGameSizing(rows, cols, bounds);
    }

    public static void computeGameSizing(int rows, int cols, Pane pane) {
        Rectangle2D bounds = new Rectangle2D(0, 0, pane.getWidth(), pane.getHeight());
        computeGameSizing(rows, cols, bounds);
    }

    public static void computeGameSizing(int rows, int cols, Dimension2D dimension2D) {
        Rectangle2D bounds = new Rectangle2D(0, 0, dimension2D.getWidth(), dimension2D.getHeight() * 0.9);
        computeGameSizing(rows, cols, bounds);
    }

    public static void computeGameSizing(int rows, int cols, Rectangle2D bounds) {
        double sceneWidth = bounds.getWidth();
        double sceneHeight = bounds.getHeight();

        if (sceneWidth == 0 || sceneHeight == 0) {
            throw new IllegalStateException("Invalid gaming area size: bounds = " + bounds);
        }

        log.info("16/9 or 16/10 screen? -> " + ((sceneWidth / sceneHeight) - (16.0 / 9.0)));

        GameSizing.height = sceneHeight / rows;
        if (GameState.fourThree && ((sceneWidth / sceneHeight) - (16.0 / 9.0)) < 0.1) {
            GameSizing.width = 4 * sceneHeight / (3 * cols);
            GameSizing.shift = (sceneWidth - width) / 2;
        } else {
            GameSizing.width = sceneWidth / cols;
            GameSizing.shift = 0;
        }

        log.debug("new game sizing: " + GameSizing.width + "x" + GameSizing.height + " shift=" + GameSizing.shift);
    }
}
