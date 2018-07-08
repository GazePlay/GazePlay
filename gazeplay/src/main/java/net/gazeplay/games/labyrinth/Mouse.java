package net.gazeplay.games.labyrinth;

import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameContext;
import net.gazeplay.commons.utils.stats.Stats;

public abstract class Mouse extends Parent {

    protected final GameContext gameContext;

    protected final Labyrinth gameInstance;

    final Stats stats;

    protected Rectangle mouse;
    protected String orientation;

    protected int indiceX; // j
    protected int indiceY; // i

    public int nbMove;

    public Mouse(double positionX, double positionY, double width, double height, GameContext gameContext, Stats stats,
            Labyrinth gameInstance) {

        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.stats = stats;

        this.mouse = new Rectangle(positionX, positionY, width, height);
        this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseFront.png"), 5, 5, 1, 1, true));
        this.getChildren().add(mouse);

        this.indiceX = 0;
        this.indiceY = 0;

        nbMove = 0;

        this.orientation = "front";

    }

    public boolean isTheMouse(int i, int j) {
        return (i == indiceY && j == indiceX);
    }

    public void putInBold() {
        if (orientation.equals("back")) {
            this.mouse
                    .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseBackBold.png"), 5, 5, 1, 1, true));
        } else if (orientation.equals("front")) {
            this.mouse
                    .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseFrontBold.png"), 5, 5, 1, 1, true));
        } else if (orientation.equals("left")) {
            this.mouse
                    .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseLeftBold.png"), 5, 5, 1, 1, true));
        } else if (orientation.equals("right")) {
            this.mouse
                    .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseRightBold.png"), 5, 5, 1, 1, true));
        }
    }

    public void putInLight() {
        if (orientation.equals("back")) {
            this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseBack.png"), 5, 5, 1, 1, true));
        } else if (orientation.equals("front")) {
            this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseFront.png"), 5, 5, 1, 1, true));
        } else if (orientation.equals("left")) {
            this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseLeft.png"), 5, 5, 1, 1, true));
        } else if (orientation.equals("right")) {
            this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseRight.png"), 5, 5, 1, 1, true));
        }
    }

    public void reOrientateMouse(int oldColumn, int oldRow, int newColumn, int newRow) {
        putInBold();
        nbMove++;
        if (oldColumn != newColumn) {
            if (oldColumn < newColumn) { // Move to the right
                this.orientation = "right";
                this.mouse
                        .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseRight.png"), 5, 5, 1, 1, true));
            } else { // Move to the Left
                this.orientation = "left";
                this.mouse
                        .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseLeft.png"), 5, 5, 1, 1, true));
            }
        } else {
            if (oldRow < newRow) { // Move to the bottom
                this.orientation = "front";
                this.mouse
                        .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseFront.png"), 5, 5, 1, 1, true));
            } else { // Move to the up
                this.orientation = "back";
                this.mouse
                        .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseBack.png"), 5, 5, 1, 1, true));
            }
        }
    }

    protected ProgressIndicator createProgressIndicator(double x, double y, double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(x + width * 0.05);
        indicator.setTranslateY(y + height * 0.2);
        indicator.setMouseTransparent(true);
        indicator.setMinWidth(width);
        indicator.setMinHeight(height);
        indicator.setOpacity(0);
        return indicator;
    }

}
