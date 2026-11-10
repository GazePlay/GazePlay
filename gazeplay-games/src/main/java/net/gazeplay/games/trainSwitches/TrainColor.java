package net.gazeplay.games.trainSwitches;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The colors of the trains and the stations of "Aiguillages" (review of 10/11/2026): 8 colors far from each other, each
 * with its symbol, drawn on the train and on its station, for the colors that look alike and for the color-blind.
 */
@Getter
@RequiredArgsConstructor
enum TrainColor {
    RED(Color.rgb(222, 64, 64), "star"),
    BLUE(Color.rgb(52, 112, 222), "circle"),
    YELLOW(Color.rgb(246, 198, 40), "square"),
    GREEN(Color.rgb(46, 164, 84), "triangle"),
    PURPLE(Color.rgb(146, 84, 204), "heart"),
    ORANGE(Color.rgb(246, 132, 36), "diamond"),
    PINK(Color.rgb(240, 118, 182), "cross"),
    BLACK(Color.rgb(64, 66, 78), "hexagon");

    private final Color color;
    private final String symbol;

    /**
     * @return the color, darker, for the outlines
     */
    Color dark() {
        return color.deriveColor(0, 1, 0.68, 1);
    }

    /**
     * @return the symbol of the color, in the color, centered on (0, 0), of a size (its width and height)
     */
    Node symbol(final double size) {
        final double r = size / 2;
        final Shape shape = switch (symbol) {
            case "star" -> {
                final Polygon star = new Polygon();
                for (int point = 0; point < 10; point++) {
                    final double angle = -Math.PI / 2 + point * Math.PI / 5;
                    final double distance = point % 2 == 0 ? r : r * 0.45;
                    star.getPoints().addAll(distance * Math.cos(angle), distance * Math.sin(angle) + r * 0.06);
                }
                yield star;
            }
            case "circle" -> new Circle(r * 0.8);
            case "square" -> new Rectangle(-r * 0.72, -r * 0.72, r * 1.44, r * 1.44);
            case "triangle" -> new Polygon(0, -r * 0.86, r * 0.92, r * 0.72, -r * 0.92, r * 0.72);
            case "heart" -> {
                final SVGPath heart = new SVGPath();
                // A heart of 2 x 1.84 units, centered on (0, 0)
                heart.setContent("M0,0.92 C-0.4,0.6 -1,0.2 -1,-0.36 C-1,-0.74 -0.7,-0.92 -0.46,-0.92 "
                    + "C-0.22,-0.92 -0.06,-0.78 0,-0.62 C0.06,-0.78 0.22,-0.92 0.46,-0.92 "
                    + "C0.7,-0.92 1,-0.74 1,-0.36 C1,0.2 0.4,0.6 0,0.92 Z");
                heart.setScaleX(r * 0.95);
                heart.setScaleY(r * 0.95);
                yield heart;
            }
            case "diamond" -> new Polygon(0, -r, r * 0.78, 0, 0, r, -r * 0.78, 0);
            case "cross" -> {
                final double a = r * 0.3;
                final double b = r * 0.88;
                yield new Polygon(-a, -b, a, -b, a, -a, b, -a, b, a, a, a, a, b, -a, b, -a, a, -b, a, -b, -a, -a,
                    -a);
            }
            default -> {
                final Polygon hexagon = new Polygon();
                for (int point = 0; point < 6; point++) {
                    final double angle = Math.PI / 6 + point * Math.PI / 3;
                    hexagon.getPoints().addAll(r * 0.86 * Math.cos(angle), r * 0.86 * Math.sin(angle));
                }
                yield hexagon;
            }
        };
        shape.setFill(color);
        shape.setStroke(dark());
        shape.setStrokeWidth(Math.max(1, size * 0.06));
        if (shape instanceof SVGPath) {
            // The stroke is scaled with the heart
            shape.setStrokeWidth(Math.max(1, size * 0.06) / (r * 0.95));
        }
        return shape;
    }
}
