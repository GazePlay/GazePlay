package net.gazeplay.commons.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class FixationSequence {

    private static final Font sanSerifFont = new Font("SanSerif", 10);

    public static final int MOUSE_FIXATION_SEQUENCE = 0;
    public static final int GAZE_FIXATION_SEQUENCE = 1;

    private final Color[][] colors = {
        {Color.INDIANRED, Color.DARKRED},
        {Color.LIGHTBLUE, Color.DARKBLUE}
    };

    /**
     * Writable image used to create the fixation Sequence image
     */
    @Getter
    private final WritableImage image;
    @Getter
    private final List<FixationPoint> sequence;

    public FixationSequence(final int width, final int height, List<List<FixationPoint>> fixationPoints, int sequenceIndex) {
        this.image = new WritableImage(width, height);
        final Canvas canvas = new Canvas(width, height);

        sequence = vertexReduction(fixationPoints.get(sequenceIndex), 15);

        final GraphicsContext gc = drawFixationLines(canvas, sequence, colors[sequenceIndex][0]);
        drawFixationCircles(gc, sequence, sequenceIndex);

        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        try {
            canvas.snapshot(params, image);
        } catch (final Exception e) {
            log.error("Can't take snapshot of Fixation Sequence: ", e);
        }

        sequence.removeIf(fixationPoint -> fixationPoint.getDuration() == -1);
    }

    private GraphicsContext drawFixationLines(Canvas canvas, List<FixationPoint> sequence, Color color) {
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        // draw the line of the sequence
        final GaussianBlur gaussianBlur = new GaussianBlur();
        gaussianBlur.setRadius(2.5);
        gc.setEffect(gaussianBlur);
        gc.setStroke(color);
        gc.setLineWidth(4);

        for (int i = 0; i < sequence.size() - 1; i++) {
            gc.strokeLine(
                sequence.get(i).getY(),
                sequence.get(i).getX(),
                sequence.get(i + 1).getY(),
                sequence.get(i + 1).getX()
            );
        }

        return gc;
    }

    private void drawFixationCircles(GraphicsContext gc, List<FixationPoint> sequence, int sequenceIndex) {
        gc.setEffect(null);
        gc.setFont(sanSerifFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        // draw the circles with the labels on top
        gc.setLineWidth(1);

        int labelCount = 0;// for the labels of the fixation sequence
        int x, y;
        double radius, duration;

        gc.setFill(colors[sequenceIndex][0]);
        gc.setFont(Font.font("Verdana", 25));

        double maxDuration = 0;
        for (final FixationPoint point : sequence) {
            if (maxDuration < Math.sqrt(point.getDuration())) {
                maxDuration = Math.sqrt(point.getDuration());
            }
        }

        for (final FixationPoint point : sequence) {

            gc.setStroke(colors[sequenceIndex][1]);
            x = point.getY();
            y = point.getX();
            duration = point.getDuration();

            // modify this value in order to change the number of fixation points (Johanna put 20 ; Didier 100)
            if (duration > 100) {
                labelCount++;
                // fixation circle size
                radius = 40d + 25d * Math.sqrt(duration) / maxDuration;
                gc.setLineWidth(25d * Math.sqrt(duration) / maxDuration);
                gc.strokeOval(x - radius / 2d, y - radius / 2d, radius, radius);
                gc.setFill(
                    Color.color(
                        Math.sqrt(duration) / maxDuration * colors[sequenceIndex][1].getRed(),
                        Math.sqrt(duration) / maxDuration * colors[sequenceIndex][1].getGreen(),
                        Math.sqrt(duration) / maxDuration * colors[sequenceIndex][1].getBlue(),
                        1));
                gc.fillOval(x - radius / 2d, y - radius / 2d, radius, radius);
                gc.setFill(colors[sequenceIndex][0]);
                gc.fillText(Integer.toString(labelCount), x, y, 80);
            } else {
                point.setDuration(-1);
            }
        }
    }

    /**
     * Saves the fixation Sequence to a PNG file
     *
     * @param outputFile The output file (Must be open and writable)
     */
    public void saveToFile(final File outputFile) {
        final BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<FixationPoint> vertexReduction(final List<FixationPoint> allPoints, final double tolerance) {
        double distance;
        FixationPoint pivotVertex = allPoints.get(0);

        final List<FixationPoint> reducedPolyline = new LinkedList<>();
        reducedPolyline.add(pivotVertex);

        for (int i = 1; i < allPoints.size() - 1; i++) {
            distance = Math.sqrt(Math.pow(pivotVertex.getY() - allPoints.get(i).getY(), 2)
                + Math.pow(pivotVertex.getX() - allPoints.get(i).getX(), 2));

            if (distance <= tolerance) {
                // add to the accepted vertex the duration of the reduced vertices -- to adapt the radius
                pivotVertex.setDuration(pivotVertex.getDuration() + allPoints.get(i).getDuration());
            } else {
                reducedPolyline.add(allPoints.get(i));
                pivotVertex = allPoints.get(i);
            }
        }
        return reducedPolyline;
    }
}
