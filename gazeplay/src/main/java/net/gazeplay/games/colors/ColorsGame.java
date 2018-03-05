package net.gazeplay.games.colors;

import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;

/**
 * Game where you select a color in order to colorize a white and black draw.
 * 
 * @author Thomas MEDARD
 */
public class ColorsGame implements GameLifeCycle {

    private final GameContext gameContext;

    private final Pane root;

    private ColorToolBox colorToolBox;

    public ColorsGame(GameContext gameContext) {

        this.gameContext = gameContext;

        root = gameContext.getRoot();

    }

    @Override
    public void launch() {

        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double width = dimension2D.getWidth();
        double height = dimension2D.getHeight();

        buildToolBox(width, height);
        buildDraw(width, height);
    }

    @Override
    public void dispose() {

    }

    private void buildToolBox(double width, double height) {

        this.colorToolBox = new ColorToolBox();
        Node colorToolBoxPane = new TitledPane("Colors", colorToolBox);

        this.root.getChildren().add(colorToolBoxPane);

        double x = 0;
        double y = height * 0.8;
        colorToolBox.relocate(x, y);
    }

    private void buildDraw(double width, double height) {

        // TODO

        Rectangle testRect = new Rectangle(width, height);
        testRect.setFill(Color.RED);
        testRect.addEventFilter(MouseEvent.MOUSE_CLICKED, (event) -> {
            testRect.setFill(colorToolBox.getSelectedColorBox().getColor());
        });

        root.getChildren().add(testRect);

        testRect.toBack();
    }
}
