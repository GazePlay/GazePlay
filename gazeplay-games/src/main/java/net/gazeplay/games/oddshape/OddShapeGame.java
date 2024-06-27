package net.gazeplay.games.oddshape;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

import javafx.stage.Screen;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

@Slf4j
public class OddShapeGame implements GameLifeCycle {


    //liste faite avec le contenu de l'enum
    private ArrayList<OddShapes> validShapes = OddShapes.getAllShapes();

    //les formes
    private OddShapes baseShape;
    private ArrayList<Shape> badShapes;
    private Shape goodShape;
    private ArrayList<Shape> hiddenShapes;

    //les couleurs
    private ArrayList<Color> validColors = new ArrayList<>();

    //le nécessaire
    private final IGameContext gameContext;
    private final Stats stats;
    private final OddShapeVariant gameVariant;

    //eye-tracker
    private final ProgressIndicator progressIndicator;
    private Timeline progressTimeline;

    //pour dessiner
    private final Pane root;
    private final HBox hBox = new HBox();

    private double width = Screen.getPrimary().getVisualBounds().getWidth();
    private double height = Screen.getPrimary().getVisualBounds().getHeight();

    //constructeur du jeu
    //rien à rajouter, c'est tout ce qu'il te faut
    public OddShapeGame(final IGameContext gameContext, final Stats stats, OddShapeVariant gameVariant) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.root = this.gameContext.getRoot();
        this.gameVariant = gameVariant;

        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setOpacity(0);
        progressIndicator.setMouseTransparent(true);

        this.gameContext.getGazeDeviceManager().addStats(stats);
    }



    //méthode pour démarrer le jeu
    @Override
    public void launch() {

        //afficher background
        createBackground();
        //initialiser les couleurs
        initialiseColors();
        //sélectionner une forme
        baseShape = selectShape();
        //puis faire le groupe de bonnes formes
        badShapes = createShapeGroup(createShape(baseShape));
        //et génerer la mauvaise
        goodShape = createGoodShape();
        drawShapes();


    }

    //méthode pour tout nettoyer
    @Override
    public void dispose() {
        //on réinitialise les formes possibles
        validShapes = OddShapes.getAllShapes();
        //et on enlève tout le monde
        root.getChildren().removeAll();

    }

    //-----------------------------------------------------------------


    //si on gagne ça joue l'animation et on peut rejouer
    private void win(){
        gameContext.playWinTransition(500, actionEvent -> {

            dispose();

            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            launch();
        });
    }

    //-----------------------------------------------------------------
   //recréer les couleurs après chaque partie
    private void initialiseColors(){
        //vider les couleurs
        validColors.clear();
        //et tout remettre
        validColors.add(Color.CRIMSON);
        validColors.add(Color.LIGHTSKYBLUE);
        validColors.add(Color.LIMEGREEN);
        validColors.add(Color.ORANGE);
        validColors.add(Color.HOTPINK);
        validColors.add(Color.GOLD);
        validColors.add(Color.PURPLE);
    }
    //-----------------------------------------------------------------

    //dessiner les formes
    private void drawShapes() {
        Random rand = new Random();

        //on dessine les bonnes formes
        for(Shape shapes : badShapes){
            //couleur
            shapes.setStroke(Color.BLACK);
            shapes.setStrokeWidth(20);
            //emplacement sur l'écran
            shapes.setLayoutX(rand.nextInt((int) width-100));
            shapes.setLayoutY(rand.nextInt((int) height-100));

            //quand on clique sur ces formes la c'est pas bon
            //! event pour les mauvaises shapes
            shapes.setOnMouseClicked(mouseEvent -> win());

            root.getChildren().add(shapes);
        }
        //et la mauvaise
        goodShape.setStroke(Color.BLACK);
        goodShape.setStrokeWidth(20);
        goodShape.setLayoutX(rand.nextInt( (int) width-100));
        goodShape.setLayoutY(rand.nextInt( (int) height-100));

        //! event pour la bonne shape
        goodShape.setOnMouseClicked(mouseEvent -> win());

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

    //prendre une shape au hasard dans la liste
    private OddShapes selectShape() {
        OddShapes selectedShape = validShapes.get(new Random().nextInt(validShapes.size()));
        //et l'enlever
        validShapes.remove(selectedShape);

        return selectedShape;
    }

    //la même pour les couleurs
    private Color selectColor(){
        Color selectedColor;
        if(this.gameVariant == OddShapeVariant.NORMAL) { //si on joue en difficulté "normal"
            selectedColor = validColors.get(new Random().nextInt(validShapes.size())); //on rend le jeu un peu plus simple en ne pouvant pas avoir de formes bonnes et mauvaises de la même couleur
            validColors.remove(selectedColor);
            return selectedColor;
        } else { // si le jeu est un peu plus dur, alors on laisse les formes avoir la même couleur
            selectedColor = validColors.get(new Random().nextInt(validShapes.size()));
        }
        return selectedColor;
    }


    //créer une forme en fonction de l'enum utilisé
    //dans le cas du jeu, ça sera avec la selectedShape
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

    //pour créer les groupes de "bonnes" shapes
    //on se base sur la SHAPE SELECTIONNEE puis on crée
    private ArrayList<Shape> createShapeGroup(Shape baseShape){
        //on en ajoute 9, la 10ème sera la mauvaise
        ArrayList<Shape> shapeList = new ArrayList<>();
        Color selectedColor = selectColor();
        for(int i = 0; i <= 9; i++){
            shapeList.add(duplicateShape(baseShape));
            shapeList.get(i).setFill(selectedColor);
        }
        return shapeList;
    }


    //pour créer la "mauvaise" shape
    //on se base sur la LISTE qui normalement n'a pas la shape sélectionnée
    //pour bien faire un intrus qui n'en fait pas partie
    private Shape createGoodShape(){
        //on fait ça après avoir enlevé la shape des validshapes, du coup on ne peut pas tomber sur la même
        OddShapes randomShape = validShapes.get(new Random().nextInt(validShapes.size()-1));
        //on sélectionne une couleur au hasard
        Color randomColor = selectColor();
        //on crée la forme
        Shape finalShape = createShape(randomShape);
        //et on la colore avant de la donner
        finalShape.setFill(randomColor);
        return finalShape;
    }
    
    //cloner les shapes
    //pour chaque type possible
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
