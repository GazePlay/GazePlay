package net.gazeplay.games.colorblend;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

import java.awt.*;

/**
 * Choose between multiple colors and blend them together
 * @author Yanis HARKATI
 */
@Slf4j
public class ColorBlend implements GameLifeCycle {

    private IGameContext gameContext;
    private Stats stats;

    public ColorBlend(final IGameContext gameContext, Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;
        final Dimension2D dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        createBackground();
    }
    @Override
    public void launch() {
    createPalette();
    }

    @Override
    public void dispose() {

    }

    private void createBackground() {
        Background background = new Background(new BackgroundImage(
            new Image("data/colorblend/images/park.png"),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
        ));
        gameContext.getRoot().setBackground(background);
    }

    private void createPalette(){
        // Création des rectangles pour afficher les couleurs spécifiées
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
        Rectangle boundingRect = createBoundingRectangle(colors.length / 2);

        // Empilement du rectangle englobant et de la grille dans une StackPane
        StackPane root = new StackPane(boundingRect, colorGrid);

        // Création de la scène
       gameContext.getChildren().add(root);
    }

    // Méthode pour créer un rectangle de couleur avec une taille spécifique
    private Rectangle createColorRectangle(Color color) {
        Rectangle rectangle = new Rectangle(50, 50);
        rectangle.setFill(color);
        rectangle.setStroke(color.darker());
        rectangle.setStrokeWidth(1);
        return rectangle;
    }


    private Rectangle createBoundingRectangle(int numPairs) {
        double height = numPairs * 53 + (numPairs - 1) * 10; // Hauteur = (hauteur d'un rectangle + espacement) * nombre de paires de couleurs - espacement
        Rectangle rectangle = new Rectangle(120, height); // Largeur fixe pour plus de visibilité
        rectangle.setFill(Color.BEIGE);
        rectangle.setStroke(Color.BEIGE.darker());
        rectangle.setStrokeWidth(2);
        return rectangle;
    }
}
