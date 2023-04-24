package net.gazeplay.games.cooperativeGame;

import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;

import java.util.ArrayList;

public class Interrupteur {

    private Rectangle interrupteur;
    private ArrayList<Rectangle> portes;
    private boolean isInterrupteurActivated;
    private IGameContext gameContext;
    private CooperativeGame gameInstance;


    public Interrupteur(Rectangle interrupteur, ArrayList<Rectangle> portes, IGameContext gameContext, CooperativeGame gameInstance){
        this.interrupteur = interrupteur;
        this.portes = portes;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.isInterrupteurActivated = false;
    }

    public Interrupteur(IGameContext gameContext, CooperativeGame gameInstance){
        this.portes = new ArrayList<>();
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.isInterrupteurActivated = false;


    }



    public void createDoorAroundAnObject(Rectangle object){

        double width = 50;
        double height = 50;

        Rectangle leftDoor = new Rectangle(object.getX()-width*2, object.getY()-height/2, width,object.getHeight()+height);
        Rectangle rightDoor = new Rectangle(object.getX()+object.getWidth()+width, object.getY()-height/2, width,object.getHeight()+height);

        Rectangle upDoor = new Rectangle(object.getX()-width*2, object.getY()-height-height/2, object.getWidth()+width*3+width, height);
        Rectangle downDoor = new Rectangle(object.getX()-width*2, object.getY()+object.getHeight()+height/2, object.getWidth()+width*3+width, height);


        this.portes.add(upDoor);
        this.portes.add(leftDoor);
        this.portes.add(rightDoor);
        this.portes.add(downDoor);
    }








    public Rectangle getInterrupteur() {
        return interrupteur;
    }

    public void setInterrupteur(Rectangle interrupteur) {
        this.interrupteur = interrupteur;
    }

    public ArrayList<Rectangle> getPortes() {
        return portes;
    }

    public void setPortes(ArrayList<Rectangle> portes) {
        this.portes = portes;
    }


}
