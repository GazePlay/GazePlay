package net.gazeplay.games.labyrinth;

import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public abstract class Mouse extends Parent {

    protected final IGameContext gameContext;

    protected final Labyrinth gameInstance;

    protected final Rectangle mouse;
    private String orientation;

    int indiceX; // j
    int indiceY; // i

    int nbMove;

    public Mouse(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext, final Stats stats,
                 final Labyrinth gameInstance) {

        this.gameContext = gameContext;
        this.gameInstance = gameInstance;

        this.mouse = new Rectangle(positionX, positionY, width, height);
        this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseFront.png"), 5, 5, 1, 1, true));
        this.getChildren().add(mouse);

        this.indiceX = 0;
        this.indiceY = 0;

        nbMove = 0;

        this.orientation = "front";

    }

    boolean isTheMouse(final int i, final int j) {
        return (i == indiceY && j == indiceX);
    }

    void putInBold() {
        switch (orientation) {
            case "back":
                this.mouse
                    .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseBackBold.png"), 5, 5, 1, 1, true));
                break;
            case "front":
                this.mouse
                    .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseFrontBold.png"), 5, 5, 1, 1, true));
                break;
            case "left":
                this.mouse
                    .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseLeftBold.png"), 5, 5, 1, 1, true));
                break;
            case "right":
                this.mouse
                    .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseRightBold.png"), 5, 5, 1, 1, true));
                break;
            default:
                throw new IllegalArgumentException(orientation);
        }
    }

    void putInLight() {
        switch (orientation) {
            case "back":
                this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseBack.png"), 5, 5, 1, 1, true));
                break;
            case "front":
                this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseFront.png"), 5, 5, 1, 1, true));
                break;
            case "left":
                this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseLeft.png"), 5, 5, 1, 1, true));
                break;
            case "right":
                this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseRight.png"), 5, 5, 1, 1, true));
                break;
            default:
                throw new IllegalArgumentException(orientation);
        }
    }

    void reOrientateMouse(final int oldColumn, final int oldRow, final int newColumn, final int newRow) {
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

    protected ProgressIndicator createProgressIndicator(final double x, final double y, final double width, final double height) {
        final ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(x + width * 0.05);
        indicator.setTranslateY(y + height * 0.2);
        indicator.setMouseTransparent(true);
        indicator.setMinWidth(width);
        indicator.setMinHeight(height);
        indicator.setOpacity(0);
        return indicator;
    }

}
