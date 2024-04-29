package net.gazeplay.games.colorblend;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

/**
 * Choose between multiple colors and blend them together
 *
 * @author Yanis HARKATI
 */
@Slf4j
public class ColorBlend implements GameLifeCycle {
    private IGameContext gameContext;
    private Stats stats;

    private Paint color1;
    private Paint color2;
    private Circle circle;

    private static final int CIRCLE_RADIUS = 400;
    private static final int PALETTE_WIDTH = 180;

    public ColorBlend(final IGameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        final Dimension2D dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
    }

    @Override
    public void launch() {
        createBackground();
        createPalette();
    }

    @Override
    public void dispose() {

    }

    /**
     * Apply background
     */
    private void createBackground() {
        Background background = new Background(new BackgroundImage(
            new Image("data/colorblend/images/park.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
        ));
        gameContext.getRoot().setBackground(background);
    }

    /**
     * Create the palette of colors
     */
    private void createPalette() {
        //Create colors
        Rectangle[] colors = {
            createColorRectangle(Color.RED),
            createColorRectangle(Color.ORANGE),
            createColorRectangle(Color.GREEN),
            createColorRectangle(Color.LIGHTBLUE),
            createColorRectangle(Color.DARKBLUE),
            createColorRectangle(Color.MAGENTA),
            createColorRectangle(Color.PINK),
            createColorRectangle(Color.WHITE),
            createColorRectangle(Color.BLACK),
            createColorRectangle(Color.YELLOW)
        };

        for (Rectangle color : colors) {
            color.setOnMouseClicked(mouseEvent -> handleMouseClick(color));
        }

        // Création de la grille pour disposer les rectangles par paires
        GridPane colorGrid = new GridPane();
        colorGrid.setPadding(new Insets(20));
        colorGrid.setHgap(10);
        colorGrid.setVgap(10);

        // Ajout des rectangles de couleur à la grille par paires
        for (int i = 0; i < colors.length; i += 2) {
            colorGrid.addRow(i / 2, colors[i], colors[i + 1]);
        }

        // Création du rectangle englobant
        Rectangle paletteRectangle = createPaletteRectangle(colors.length / 2);

        // Empilement du rectangle englobant et de la grille dans une StackPane
        StackPane root = new StackPane(paletteRectangle, colorGrid);

        // Création de la scène
        gameContext.getChildren().add(root);
    }

    /**
     * Create a rectangle for a color
     * @param color the color used
     * @return a Rectangle Object used for choosing color
     */
    private Rectangle createColorRectangle(Color color) {
        Rectangle rectangle = new Rectangle(75, 75);
        rectangle.setFill(color);
        rectangle.setStroke(color.darker());
        rectangle.setStrokeWidth(1);
        return rectangle;
    }


    private Rectangle createPaletteRectangle(int numPairs) {
        double height = numPairs * 53 + (numPairs - 1) * 10; // Hauteur = (height of a color rectangle + spacing) * size of colors - spacing
        Rectangle rectangle = new Rectangle(PALETTE_WIDTH, height); // constant width for more visibility
        rectangle.setFill(Color.BEIGE);
        rectangle.setStroke(Color.BEIGE.darker());
        rectangle.setStrokeWidth(2);
        return rectangle;
    }

    private void handleMouseClick(Rectangle color) {
        if (this.color1 == null) {
            this.color1 = color.getFill();
            this.circle = createCirle();
            this.circle.setFill(this.color1);
        } else {
            this.color2 = color.getFill();
            color.setStroke(Color.WHITE);
            Color newColor = blendColors();
            this.circle.setFill(newColor);
        }

    }
    /**
     * Get a blended color from 2 initial colors
     *
     * @return a blended Color object
     */
    private Color blendColors() {
        //Get the initial R G B elements of colors
        Color color1 = (Color) this.color1;
        Color color2 = (Color) this.color2;

        double r1 = color1.getRed();
        double g1 = color1.getGreen();
        double b1 = color1.getBlue();

        double r2 = color2.getRed();
        double g2 = color2.getGreen();
        double b2 = color2.getBlue();

        // Merge colors
        double r = (r1 + r2) / 2;
        double g = (g1 + g2) / 2;
        double b = (b1 + b2) / 2;

        return new Color(r,g,b,1);
    }

    private Circle createCirle(){
        Circle circle = new Circle();
        double radius = CIRCLE_RADIUS;
        circle.setRadius(radius);

        this.gameContext.getRoot().getChildren().add(circle);

        circle.centerXProperty().bind(this.gameContext.getRoot().widthProperty().divide(2));
        circle.centerYProperty().bind(this.gameContext.getRoot().heightProperty().divide(2));


        return circle;
    }
}
