package net.gazeplay.games.oddshape;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

import javafx.stage.Screen;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class OddShapeGame implements GameLifeCycle {


    //liste faite avec le contenu de l'enum
    private final ArrayList<OddShapes> validShapes = OddShapes.getAllShapes();

    //les formes
    private OddShapes baseShape;
    private ArrayList<Shape> goodShapes;
    private Shape badShape;
    private ArrayList<Shape> hiddenShapes;

    //le nécessaire
    private final IGameContext gameContext;
    private final Stats stats;

    //pour dessiner
    private final Pane root;
    private final HBox hBox = new HBox();

    double width = (Screen.getPrimary().getVisualBounds().getWidth() - 20);
    double height = (Screen.getPrimary().getVisualBounds().getHeight() - 20);

    //constructeur du jeu
    //rien à rajouter, c'est tout ce qu'il te faut
    public OddShapeGame(final IGameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.root = this.gameContext.getRoot();
    }



    //méthode pour démarrer le jeu
    @Override
    public void launch() {

        //afficher background
        createBackground();
        //sélectionner une forme
        baseShape = selectShape();
        //puis faire le groupe de bonnes formes
        goodShapes = createShapeGroup(createShape(baseShape));
        //et génerer la mauvaise
        badShape = createBadShape();
        drawShapes();


    }

    //méthode pour tout nettoyer
    @Override
    public void dispose() {
        root.getChildren().removeAll();

    }

    //-----------------------------------------------------------------

//    //la vérification des formes
//    private void checkIfRight(Shape shapeEvaluated){
//        if(shapeEvaluated == badShape){
//
//        } else {
//
//        }
//    }
//
//    //si on gagne ça joue l'animation et on peut rejouer
//    private void win(){
//        gameContext.playWinTransition(500, actionEvent -> {
//
//            dispose();
//
//            gameContext.getGazeDeviceManager().clear();
//
//            gameContext.clear();
//
//            launch();
//        });
//    }

    //-----------------------------------------------------------------

    //dessiner les formes
    private void drawShapes() {
        Random rand = new Random();

        //on dessine les bonnes formes
        for(Shape shapes : goodShapes){
            //couleur
            shapes.setFill(Color.WHITE);
            shapes.setStroke(Color.BLACK);
            shapes.setStrokeWidth(20);
            //emplacement sur l'écran
            shapes.setLayoutX(rand.nextInt((int) width));
            shapes.setLayoutY(rand.nextInt((int) height));

            root.getChildren().add(shapes);
        }
        //et la mauvaise
        badShape.setFill(Color.WHITE);
        badShape.setStroke(Color.BLACK);
        badShape.setStrokeWidth(20);
        badShape.setLayoutX(rand.nextInt( (int) width-20));
        badShape.setLayoutY(rand.nextInt( (int) height-20));

        root.getChildren().add(badShape);
    }

    private void createBackground() {
        Background background = new Background(new BackgroundImage(
            new Image("data/oddshapes/1708774927070.png"),
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


    //créer une forme en fonction de l'enum utilisé
    //dans le cas du jeu, ça sera avec la selectedShape
    private Shape createShape(OddShapes shape) {
        switch(shape) {
            case CIRCLE -> {
                return new Circle(100); //rayon hasardeux
            }
            case RECTANGLE -> {
                return new Rectangle(200, 100);
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
                throw new RuntimeException("ça ne devrait pas arriver...");
            }
        }
    }

    //pour créer les groupes de "bonnes" shapes
    //on se base sur la SHAPE SELECTIONNEE puis on crée
    private ArrayList<Shape> createShapeGroup(Shape baseShape){
        //on en ajoute 9, la 10ème sera la mauvaise
        ArrayList<Shape> shapeList = new ArrayList<>();
        for(int i = 0; i <= 9; i++){
            shapeList.add(duplicateShape(baseShape));
        }
        return shapeList;
    }

    //pour créer la "mauvaise" shape
    //on se base sur la LISTE qui normalement n'a pas la shape sélectionnée
    //pour bien faire un intrus
    private Shape createBadShape(){
        //à changer si ça ne marche pas
        OddShapes randomShape = validShapes.get(new Random().nextInt(validShapes.size()-1));
        //on fait ça après avoir enlevé la shape des validshapes, du coup on ne peut pas tomber sur la même
        return createShape(randomShape);
    }

    //TODO: coder quelque chose de meilleur
    private Shape duplicateShape(Shape shape) {
        if (shape instanceof Circle) {
            Circle og = (Circle) shape;
            Circle copy = new Circle(og.getCenterX(), og.getCenterY(), og.getRadius());
            copy.setFill(og.getFill());
            return copy;
        } else if (shape instanceof Rectangle) {
            Rectangle og = (Rectangle) shape;
            Rectangle copy = new Rectangle(og.getX(), og.getY(), og.getWidth(), og.getHeight());
            copy.setFill(og.getFill());
            return copy;
        } else if (shape instanceof Polygon) {
            Polygon og = (Polygon) shape;
            Polygon copy = new Polygon();
            copy.getPoints().addAll(og.getPoints());
            copy.setFill(og.getFill());
//            copy.setStroke(og.getStroke());
//            copy.setStrokeWidth(og.getStrokeWidth());
            return copy;
        }
        else return shape;
    }



}
