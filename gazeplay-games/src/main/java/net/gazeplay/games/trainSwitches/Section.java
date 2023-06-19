package net.gazeplay.games.trainSwitches;

import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

public class Section {

    private final Path path;
    private double size;

    public Section(double size){
        this.size = size;
        path = new Path();
        path.setStroke(Color.GREEN);
        path.setStrokeWidth(20);
    }

    public Section(){
        size = 2;
        path = new Path();
        path.setStroke(Color.GREEN);
        path.setStrokeWidth(20);
    }

    public Path getPath() {
        return path;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void add(PathElement elem){
        path.getElements().add(elem);
    }
}
