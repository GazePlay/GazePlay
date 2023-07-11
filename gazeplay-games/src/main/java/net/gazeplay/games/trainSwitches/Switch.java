package net.gazeplay.games.trainSwitches;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.ArrayList;

public class Switch {

    private final ArrayList<Section> outputSections;
    private int outputSelected;
    private final ArrayList<QuadCurve> curves;
    private final IntegerProperty radius;
    private final Group group;
    private final DoubleProperty xcenter;
    private final DoubleProperty ycenter;

    public Switch() {
        outputSections = new ArrayList<>();
        curves = new ArrayList<>();
        group = new Group();
        outputSelected = 0;
        radius = new SimpleIntegerProperty(50);
        xcenter = new SimpleDoubleProperty(0);
        ycenter = new SimpleDoubleProperty(0);
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

    // Update the switch shape
    public void updateShape(){
        Circle circle = new Circle(50);
        circle.setFill(Color.GREEN);
        circle.centerXProperty().bind(xcenter);
        circle.centerYProperty().bind(ycenter);
        circle.radiusProperty().bind(radius);
        circle.setStroke(Color.RED);
        circle.setStrokeWidth(5);

        group.getChildren().clear();
        group.getChildren().add(circle);
        group.getChildren().add(curves.get(outputSelected));
    }

    // Return true if the coord are close enough(50 pixels) to the center of the switch
    public boolean isInside(double x, double y){
        return Math.abs(x - xcenter.get())<=50 && Math.abs(y - ycenter.get())<=50;
    }

    public Section getOutput(){
        return outputSections.get(outputSelected);
    }
    public Section getOutput(int index){
        return outputSections.get(index);
    }
    public DoubleProperty xProperty() {
        return xcenter;
    }
    public DoubleProperty yProperty() {
        return ycenter;
    }
    public IntegerProperty radiusProperty() {
        return radius;
    }
}
