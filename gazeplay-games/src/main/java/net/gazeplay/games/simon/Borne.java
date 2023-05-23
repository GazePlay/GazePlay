package net.gazeplay.games.simon;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;

import java.util.ArrayList;

public class Borne extends Parent {
    private IGameContext gameContext;
    private Simon gameInstance;
    private ArrayList<Bouton> boutons;
    private double radius;
    private int nbNotes;


    public Borne(IGameContext gameContext, Simon gameInstance){
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.boutons = new ArrayList<>();
        this.radius = 400;
        nbNotes = 0;
        initBorne();
    }


    public void initBorne(){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        //Circle that contains buttons
        Circle circle = new Circle(dimension2D.getWidth()/2, dimension2D.getHeight()/2,radius,Color.TRANSPARENT);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(15);

        initBoutons();

        Rectangle rectangleCenter = new Rectangle(dimension2D.getWidth()/2-150,(dimension2D.getHeight()/2)-150,300,300);
        rectangleCenter.setArcWidth(200);
        rectangleCenter.setArcHeight(200);

        Rectangle lineVertical = new Rectangle(dimension2D.getWidth()/2-50,dimension2D.getHeight()/2-400,100,800);
        Rectangle lineHorizontal = new Rectangle(dimension2D.getWidth()/2-400,dimension2D.getHeight()/2-50,800,100);

        gameContext.getChildren().add(circle);

        for (Bouton bouton : boutons){
            this.gameContext.getChildren().add(bouton);
        }
        gameContext.getChildren().addAll(rectangleCenter,lineVertical,lineHorizontal);

    }

    private void initBoutons(){
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Bouton topRight = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius,radius,0,90,gameContext,gameInstance, "topright");
        topRight.setFill(Color.RED);

        Bouton topLeft = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius,radius,90,90,gameContext,gameInstance, "topleft");
        topLeft.setFill(Color.web("2FDF3C"));

        Bouton bottomLeft = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius,radius,180,90,gameContext,gameInstance,"bottomleft");
        bottomLeft.setFill(Color.YELLOW);

        Bouton bottomRight = new Bouton(dimension2D.getWidth()/2,dimension2D.getHeight()/2,radius,radius,270,90,gameContext,gameInstance,"bottomright");
        bottomRight.setFill(Color.web("2FB4DF"));

        this.boutons.add(topRight);
        this.boutons.add(topLeft);
        this.boutons.add(bottomRight);
        this.boutons.add(bottomLeft);
    }

}
