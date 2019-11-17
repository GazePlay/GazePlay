package net.gazeplay.games.pianosight;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

class Tile extends Parent {

    int note;
    EventHandler<Event> tileEventEnter;
    EventHandler<Event> tileEventExited;
    Color color1;
    //Color color2;

    final Shape arc;

    Tile(
        double centerX,
        double centerY,
        double radiusX,
        double radiusY,
        double startAngle,
        double length,
        Circle c
    ) {
        super();
        //
        Arc arcTemp = new Arc(centerX, centerY, radiusX, radiusY, startAngle, length);
        arcTemp.setType(ArcType.ROUND);
        //
        arc = Shape.subtract(arcTemp, c);
        arc.setStroke(Color.BLACK);
        arc.setStrokeLineJoin(StrokeLineJoin.ROUND);
        arc.setStrokeType(StrokeType.CENTERED);
        this.getChildren().add(arc);
    }

}
