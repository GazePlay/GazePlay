package net.gazeplay.games.pianosight;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class Tile extends Parent {

    int note;
    EventHandler<Event> tileEventEnter;
    EventHandler<Event> tileEventExited;
    Color color1;
    Color color2;
    Shape arc;

    public Tile() {
        super();
        note = 0;
    }

    public Tile(double centerX, double centerY, double radiusX, double radiusY, double startAngle, double length,
            Circle c) {
        super();
        arc = new Arc(centerX, centerY, radiusX, radiusY, startAngle, length);
        ((Arc) arc).setType(ArcType.ROUND);
        arc = Shape.subtract(arc, c);
        arc.setStroke(Color.BLACK);
        arc.setStrokeLineJoin(StrokeLineJoin.ROUND);
        arc.setStrokeType(StrokeType.CENTERED);
        this.getChildren().add(arc);
    }
}
