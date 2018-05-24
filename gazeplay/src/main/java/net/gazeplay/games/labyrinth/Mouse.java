package net.gazeplay.games.labyrinth;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameContext;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.labyrinth.Labyrinth;

public abstract class Mouse extends Parent {

    private final GameContext gameContext;

    protected final Labyrinth gameInstance;

    final Stats stats;

    protected Rectangle mouse;

    protected int indiceX; // j
    protected int indiceY; // i

    public Mouse(double positionX, double positionY, double width, double height, GameContext gameContext, Stats stats,
            Labyrinth gameInstance) {

        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.stats = stats;

        this.mouse = new Rectangle(positionX, positionY, width, height);
        this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseFront.png"), 0, 0, 1, 1, true));
        this.getChildren().add(mouse);

        this.indiceX = 0; // largeur
        this.indiceY = 0; // hauteur

        // Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

    }

    public boolean isTheMouse(int i, int j) {
        return (i == indiceY && j == indiceX);
    }

    public void reOrientateMouse(int oldColumn, int oldRow, int newColumn, int newRow) {
        if (oldColumn != newColumn) {
            if (oldColumn < newColumn) { // Move to the right
                this.mouse
                        .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseRight.png"), 5, 5, 1, 1, true));
            } else { // Move to the Left
                this.mouse
                        .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseLeft.png"), 5, 5, 1, 1, true));
            }
        } else {
            if (oldRow < newRow) { // Move to the bottom
                this.mouse
                        .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseFront.png"), 5, 5, 1, 1, true));
            } else { // Move to the up
                this.mouse
                        .setFill(new ImagePattern(new Image("data/labyrinth/images/mouseBack.png"), 5, 5, 1, 1, true));
            }
        }
    }

}
