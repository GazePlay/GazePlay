package net.gazeplay.games.cooperativeGame;

import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class Interrupteur {

    private Rectangle interrupteur;
    private ArrayList<Rectangle> portes;


    public Interrupteur(Rectangle interrupteur, ArrayList<Rectangle> portes){
        this.interrupteur = interrupteur;
        this.portes = portes;
    }

    public Interrupteur(){
        this.portes = new ArrayList<>();
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
