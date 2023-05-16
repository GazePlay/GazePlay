package net.gazeplay.games.trainSwitches;

import javafx.scene.paint.Color;
import javafx.scene.shape.Path;

public class Section {

    private Path path;
    private double size;

    public Section(double size){
        this.size = size;
        path = new Path();
        path.setStroke(Color.GREEN);
        path.setStrokeWidth(20);
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }
}
