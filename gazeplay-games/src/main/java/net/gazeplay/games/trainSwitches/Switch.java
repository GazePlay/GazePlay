package net.gazeplay.games.trainSwitches;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.ArrayList;

public class Switch {

    private final ArrayList<Section> outputSections;
    private int outputSelected;
    private final ArrayList<QuadCurve> curves;

    public IntegerProperty radius;
    private final Group group;

    public Switch() {
        outputSections = new ArrayList<>();
        curves = new ArrayList<>();
        group = new Group();
        outputSelected = 0;
        radius = new SimpleIntegerProperty(50);
    }

    public void addCurve(QuadCurve curve){
        curves.add(curve);
    }

    public void addOutput(Section section){
        outputSections.add(section);
    }

    public void changeOutputSelected(){
        outputSelected = (outputSelected+1)%curves.size();
    }

    public Group getGroup(){
        return group;
    }

    public void updateShape(){
        Circle circle = new Circle(50);
        circle.setFill(Color.GREEN);
        circle.centerXProperty().bind(curves.get(outputSelected).controlXProperty());
        circle.centerYProperty().bind(curves.get(outputSelected).controlYProperty());
        circle.radiusProperty().bind(radius);

        group.getChildren().clear();
        group.getChildren().add(circle);
        group.getChildren().add(curves.get(outputSelected));
    }

    public Point2D getCenter(){
        return new Point2D(curves.get(outputSelected).controlXProperty().get(), curves.get(outputSelected).controlYProperty().get());
    }

    public boolean isInside(double x, double y){
        return Math.abs(x - curves.get(outputSelected).controlXProperty().get())<=50 && Math.abs(y - curves.get(outputSelected).controlYProperty().get())<=50;
    }

    public Section getOutput(){
        return outputSections.get(outputSelected);
    }

}
