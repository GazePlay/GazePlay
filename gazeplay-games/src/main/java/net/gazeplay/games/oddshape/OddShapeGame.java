package net.gazeplay.games.oddshape;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.event.Event;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

import javafx.stage.Screen;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

@Slf4j
public class OddShapeGame implements GameLifeCycle {


    //List of available shapes, built from the OddShapes Enum
    private ArrayList<OddShapes> validShapes = OddShapes.getAllShapes();

    //Attributes related to the shapes
    private OddShapes baseShape;
    private ArrayList<Shape> badShapes;
    private Shape goodShape;
    private ArrayList<Shape> hiddenShapes;

    //List of available colors
    private ArrayList<Color> validColors = new ArrayList<>();

    //Attributes necessary for the game to function
    private final IGameContext gameContext;
    private final Stats stats;
    private final OddShapeVariant gameVariant;

    //Attributes related to the eye-tracker
    private ProgressIndicator progressIndicator;
    private Timeline progressTimeline;

    //Attributes related to the scene
    private final Pane root;
    private double width = Screen.getPrimary().getVisualBounds().getWidth();
    private double height = Screen.getPrimary().getVisualBounds().getHeight();

    //Game constructor
    public OddShapeGame(final IGameContext gameContext, final Stats stats, OddShapeVariant gameVariant) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.root = this.gameContext.getRoot();
        this.gameVariant = gameVariant;

        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setOpacity(0);
        progressIndicator.setMouseTransparent(true);

    }


    /**
     * launch()
     *
     * Starts a new game.
     */
    @Override
    public void launch() {

        //Adds progressIndicator to the scene
        root.getChildren().add(progressIndicator);
        //Shows background
        createBackground();
        //Initialises the available colors
        initialiseColors();
        //Selects a shape
        baseShape = selectShape();
        //Creates "bad" shapes group
        badShapes = createShapeGroup(createShape(baseShape));
        //Creates "good" shape
        goodShape = createGoodShape();
        //Shows all shapes on screen
        drawShapes();

        //Eye-tracker integration
        stats.notifyNewRoundReady();
        //Stats integration
        gameContext.getGazeDeviceManager().addStats(stats);


    }

    /**
     * dispose()
     *
     * Cleans up the scene.
     */
    @Override
    public void dispose() {
        //Resets the pool of shapes
        validShapes = OddShapes.getAllShapes();
        //Removes all elements from screen
        root.getChildren().removeAll();

    }

    //-----------------------------------------------------------------

    /**
     * buildEvent()
     * @param shape
     * @return
     *
     * Manages the selection of an item when the mouse or the gaze of the user hovers it.
     */
    private EventHandler<Event> buildEvent(final Shape shape) {
        return e -> {
            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                progressTimeline = new Timeline();
                progressTimeline.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(progressIndicator.progressProperty(), 1)));

                progressTimeline.setOnFinished(actionEvent -> checkShape(shape));

                progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                progressIndicator.setOpacity(1);
                progressIndicator.setProgress(0);

                progressTimeline.play();
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                progressTimeline.stop();
                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);
            }
        };
    }

    /**
     * createProgressIndicator()
     * @param shape
     * @return
     *
     * Creates a ProgressIndicator object, based on a shape.
     */
    private ProgressIndicator createProgressIndicator(final Shape shape) {
        final ProgressIndicator progressIndicator = new ProgressIndicator(0);

        progressIndicator.setMinWidth(shape.getLayoutBounds().getWidth());
        progressIndicator.setMinHeight(shape.getLayoutBounds().getHeight());
        progressIndicator.setOpacity(0);

        return progressIndicator;
    }

    //-----------------------------------------------------------------

    /**!
     * checkShape()
     * @param shape
     *
     * Checks if the clicked shape is considered "good" or "bad".
     * If it's a win, all shapes are disabled, so they cannot be clicked during the animation.
     */
    private void checkShape(Shape shape){
        if(shape == goodShape){
            for(Shape shapes: badShapes){
                shapes.setOnMouseClicked(null);
            }
            win();
        } else {
            onError(shape);
        }
    }

    /**
     * onError()
     * @param shape
     *
     * Generates an error picture when a "bad" shape is clicked.
     */

    private void onError(Shape shape){
        //Error picture
        final Image image = new Image("data/common/images/error.png");
        ImageView imageCroix = new ImageView(image);
        //Reduces its size
        imageCroix.setFitHeight(100);
        imageCroix.setFitWidth(100);
        //Adds it to the scene
        root.getChildren().add(imageCroix);
        //Figures out where the center of the shape is
        double centreX = shape.getLayoutX() + shape.getLayoutBounds().getWidth() / 2;
        double centreY = shape.getLayoutY() + shape.getLayoutBounds().getHeight() / 2;
        //And attaches the picture to it
        imageCroix.setX(centreX - imageCroix.getFitWidth() / 2);
        imageCroix.setY(centreY - imageCroix.getFitHeight() / 2);
        imageCroix.setVisible(true);
    }

    /**
     * win()
     *
     * Plays the win animation and generates the next game.
     */

    private void win(){
        gameContext.playWinTransition(500, actionEvent -> {

            dispose();

            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            launch();
        });
    }

    /**
     * initialiseColors()
     *
     * Resets the pool of available colors.
     */
    private void initialiseColors(){
        //Empties all colors left
        validColors.clear();
        //And puts a new set back
        validColors.add(Color.CRIMSON);
        validColors.add(Color.LIGHTSKYBLUE);
        validColors.add(Color.LIMEGREEN);
        validColors.add(Color.ORANGE);
        validColors.add(Color.HOTPINK);
        validColors.add(Color.GOLD);
        validColors.add(Color.PURPLE);
    }
    //-----------------------------------------------------------------

    /**
     * drawShapes()
     *
     * Shows all shapes on screen and manages their appearance as well as their behaviour.
     * Also where events are applied.
     */
    private void drawShapes() {
        Random rand = new Random();

        //Generating the "bad" shapes
        for(Shape shapes : badShapes){
            shapes.setStroke(Color.BLACK);
            shapes.setStrokeWidth(20);
            //Their location on screen
            shapes.setLayoutX(rand.nextInt((int) width-150));
            shapes.setLayoutY(rand.nextInt((int) height-150));

            //When the shape is clicked with the mouse
            shapes.setOnMouseClicked(mouseEvent -> checkShape(shapes));
            //Eye-tracker integration
            this.progressIndicator = createProgressIndicator(shapes);
            shapes.addEventFilter(MouseEvent.ANY, buildEvent(shapes));
            shapes.addEventFilter(GazeEvent.ANY, buildEvent(shapes));


            root.getChildren().add(shapes);
        }
        //Generating the "good" shape
        goodShape.setStroke(Color.BLACK);
        goodShape.setStrokeWidth(20);
        //Their location on screen
        goodShape.setLayoutX(rand.nextInt( (int) width-150));
        goodShape.setLayoutY(rand.nextInt( (int) height-150));

        //When the shape is clicked with the mouse
        goodShape.setOnMouseClicked(mouseEvent -> checkShape(goodShape));
        //Eye-tracker integration
        this.progressIndicator = createProgressIndicator(goodShape);
        goodShape.addEventFilter(MouseEvent.ANY,buildEvent(goodShape));
        goodShape.addEventFilter(GazeEvent.ANY,buildEvent(goodShape));

        root.getChildren().add(goodShape);
    }

    private void createBackground() {
        //background by kues on freepik
        Background background = new Background(new BackgroundImage(
            new Image("data/oddshapes/papier.jpg"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(20, 40, false, false, false, true)
        ));
        root.setBackground(background);
    }


    //-----------------------------------------------------------------

    /**
     * selectShape()
     * @return
     *
     * Selects a OddShape randomly from a list.
     * Removes that value afterwards, so it is not picked again.
     */
    private OddShapes selectShape() {
        OddShapes selectedShape = validShapes.get(new Random().nextInt(validShapes.size()));
        validShapes.remove(selectedShape);

        return selectedShape;
    }

    /**
     * selectColor()
     *
     * Selects a color randomly from a list.
     * Whether the color is removed or not afterwards depends on the chosen variant.
     */

    private Color selectColor(){
        Color selectedColor;
        //"Normal" variant
        if(this.gameVariant == OddShapeVariant.NORMAL) {
            selectedColor = validColors.get(new Random().nextInt(validShapes.size())); //on rend le jeu un peu plus simple en ne pouvant pas avoir de formes bonnes et mauvaises de la même couleur
            validColors.remove(selectedColor);
            return selectedColor;
        //"Harder" variant
        } else {
            selectedColor = validColors.get(new Random().nextInt(validShapes.size()));
        }
        return selectedColor;
    }


    /**
     * createShape()
     * @param shape
     * @return
     *
     * Creates a Shape object based on the OddShape selected with selectShape().
     */
    private Shape createShape(OddShapes shape) {
        switch(shape) {
            case CIRCLE -> {
                return new Circle(100);
            }
            case RECTANGLE -> {
                return new Rectangle(300, 200);
            }
            case CARRE -> {
                return new Rectangle(200, 200);
            }
            case TRIANGLE -> {
                Polygon triangle = new Polygon();
                triangle.getPoints().addAll(50.0, 0.0, 0.0, 200.0, 200.0, 200.0);
                return triangle;
            }

            default -> {
                throw new RuntimeException("forme inconnue");
            }
        }
    }

    /**
     * createShapeGroup()
     * @param baseShape
     * @return
     *
     * Creates a group of Shape objects based on a Shape.
     * It is duplicated using duplicateShape() then put in a list.
     * These are considered the "bad" shapes.
     */
    private ArrayList<Shape> createShapeGroup(Shape baseShape){
        //9 are added, the 10th will be the good shape
        ArrayList<Shape> shapeList = new ArrayList<>();
        Color selectedColor = selectColor();
        for(int i = 0; i <= 9; i++){
            shapeList.add(duplicateShape(baseShape));
            shapeList.get(i).setFill(selectedColor);
        }
        return shapeList;
    }


    /**
     * createGoodShape()
     * @return
     *
     * Creates the "good" shape based on the validShapes and validColors() lists.
     */
    private Shape createGoodShape(){
        //Can't fall on the same shape as the "bad" ones
        OddShapes randomShape = validShapes.get(new Random().nextInt(validShapes.size()-1));
        //A random color is taken...
        Color randomColor = selectColor();
        //...and the shape is created.
        Shape finalShape = createShape(randomShape);
        //Filling it with the chosen color
        finalShape.setFill(randomColor);
        return finalShape;
    }

    /**
     * duplicateShape()
     * @param shape
     * @return
     *
     * Creates a clone of a shape, depending on its type.
     */
    private Shape duplicateShape(Shape shape) {
        if (shape instanceof Circle) {
            Circle og = (Circle) shape;
            Circle clone = new Circle(og.getCenterX(), og.getCenterY(), og.getRadius());
            clone.setFill(og.getFill());
            clone.setStroke(og.getStroke());
            clone.setStrokeWidth(og.getStrokeWidth());
            return clone;
        } else if (shape instanceof Rectangle) {
            Rectangle og = (Rectangle) shape;
            Rectangle clone = new Rectangle(og.getX(), og.getY(), og.getWidth(), og.getHeight());
            clone.setFill(og.getFill());
            clone.setStroke(og.getStroke());
            clone.setStrokeWidth(og.getStrokeWidth());
            return clone;
        } else if (shape instanceof Polygon) {
            Polygon og = (Polygon) shape;
            Polygon clone = new Polygon();
            clone.getPoints().addAll(og.getPoints());
            clone.setFill(og.getFill());
            clone.setStroke(og.getStroke());
            clone.setStrokeWidth(og.getStrokeWidth());
            return clone;
        }
        else return shape;
    }


}
