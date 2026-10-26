package net.gazeplay.games.labyrinth;

import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;


/**
 * The character the player moves in the labyrinth: the mouse, or a character drawn at random. Subclasses define how it
 * is moved.
 */
public abstract class Mouse extends Parent {

    protected final IGameContext gameContext;

    protected final Labyrinth gameInstance;

    protected final Rectangle mouse;
    private String orientation;
    public boolean souris;
    String ImageChoisie;

    int indiceX; // j
    int indiceY; // i

    int nbMove;

    public Mouse(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext, final Stats stats,
                 final Labyrinth gameInstance) {

        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.souris =true;
        this.ImageChoisie=getRandomCaractere();
        this.mouse = new Rectangle(positionX, positionY, width, height);
        this.getChildren().add(mouse);
        this.indiceX = 0;
        this.indiceY = 0;

        nbMove = 0;
        this.orientation = "front";

    }

    /**
     * Shows the picture of the character.
     */
    public void setImage ()
    {
        if (this.souris == true) {
            this.mouse.setFill(new ImagePattern(new Image("data/labyrinth/images/mouseFront.png"), 5, 5, 1, 1, true));
        } else {
            this.mouse.setFill(new ImagePattern(new Image(ImageChoisie), 5, 5, 1, 1, true));
        }
    }
    /**
     * Replaces the mouse by a character drawn at random.
     */
    public void setRandomPersonnage(){
        this.souris=false;
    }
    /**
     * @return the picture of a character drawn at random
     */
    String getRandomCaractere()
    {
        int choix = gameInstance.getRandomGenerator().nextInt(17);
        String img;
        switch (choix)
        {
            case 0:
                img="data/common/default/images/noBackGround/bear.png";
                break;
            case 1:
                img="data/common/default/images/noBackGround/chouette.png";
                break;
            case 2:
                img="data/common/default/images/noBackGround/crabe.png";
                break;
            case 3:
                img="data/common/default/images/noBackGround/dog.png";
                break;
            case 4:
                img="data/common/default/images/noBackGround/ecureuil.png";
                break;
            case 5:
                img="data/common/default/images/noBackGround/elephant.png";
                break;
            case 6:
                img="data/common/default/images/noBackGround/fox.png";
                break;
            case 7:
                img="data/common/default/images/noBackGround/giraffe.png";
                break;
            case 8:
                img="data/common/default/images/noBackGround/herisson.png";
                break;
            case 9:
                img="data/common/default/images/noBackGround/hippo.png";
                break;
            case 10:
                img="data/common/default/images/noBackGround/lion.png";
                break;
            case 11:
                img="data/common/default/images/noBackGround/meduse.png";
                break;
            case 12:
                img="data/common/default/images/noBackGround/poulpe.png";
                break;
            case 13:
                img="data/common/default/images/noBackGround/renard.png";
                break;
            case 14:
                img="data/common/default/images/noBackGround/snake.png";
                break;
            case 15:
                img="data/common/default/images/noBackGround/tortue.png";
                break;
            default:
                img="data/common/default/images/noBackGround/turtle.png";
                break;
        }
        return img;
    }

    /**
     * @return whether the mouse is on a box
     */
    boolean isTheMouse(final int i, final int j) {
        return (i == indiceY && j == indiceX);
    }

    /**
     * Highlights the mouse (selected).
     */
    void putInBold() {
        if (souris == true) {
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
        } else {
            this.mouse
                .setFill(new ImagePattern(new Image(this.ImageChoisie), 5, 5, 1, 1, true));
        }
    }

    /**
     * Removes the highlight of the mouse.
     */
    void putInLight() {
        if (souris == true) {
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
        } else {
            this.mouse
                .setFill(new ImagePattern(new Image(this.ImageChoisie), 5, 5, 1, 1, true));
        }
    }

    /**
     * Turns the mouse in the direction of its move.
     */
    void reOrientateMouse(final int oldColumn, final int oldRow, final int newColumn, final int newRow) {
        if(souris ==true) {
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
        else{
            this.mouse
                .setFill(new ImagePattern(new Image(this.ImageChoisie), 5, 5, 1, 1, true));
        }
    }

    /**
     * @return a progress indicator of a fixation
     */
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
