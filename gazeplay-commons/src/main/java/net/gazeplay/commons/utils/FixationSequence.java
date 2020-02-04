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

@Slf4j
public class FixationSequence {

    private static Font sanSerifFont = new Font("SanSerif", 10);
    /**
     * Writable image used to create the fixation Sequence image
     */
    @Getter
    private WritableImage image;
    @Getter
    private LinkedList<FixationPoint> sequence;

    public FixationSequence(int width, int height, LinkedList<FixationPoint> fixSeq) {

        sequence = new LinkedList<>();
        this.image = new WritableImage(width, height);
        Canvas canvas = new Canvas(width, height);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // draw the line of the sequence
        GaussianBlur gaussianBlur = new GaussianBlur();
        gaussianBlur.setRadius(2.5);
        gc.setEffect(gaussianBlur);
        gc.setStroke(Color.rgb(255, 157, 6, 1));
        gc.setLineWidth(4);

        fixSeq = vertexReduction(fixSeq, 15);

        for (int i = 0; i < fixSeq.size() - 1; i++) {
            gc.strokeLine(fixSeq.get(i).getY(), fixSeq.get(i).getX(), fixSeq.get(i + 1).getY(),
                fixSeq.get(i + 1).getX());
        }
        gc.setEffect(null);
        gc.setFont(sanSerifFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        // draw the circles with the labels on top
        gc.setLineWidth(1);

        int label_count = 0;// for the labels of the fixation sequence
        int x = fixSeq.get(0).getY();
        int y = fixSeq.get(0).getX();

        double radius = 45;

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Verdana", 25));

        double duration;

        for (int j = 0; j < fixSeq.size(); j++) {

            gc.setStroke(Color.RED);
            x = fixSeq.get(j).getY();
            y = fixSeq.get(j).getX();
            duration = fixSeq.get(j).getGazeDuration();

            // modify this value in order to change the number of fixation points (Johanna put 20 ; Didier 100)
            if (duration > 100) {
                label_count++;
                // fixation circle size
                radius = 20d + Math.sqrt(duration);
                gc.strokeOval(x - radius / 2d, y - radius / 2d, radius, radius);
                gc.setFill(Color.rgb(255, 255, 0, 0.5));// yellow 50% transparency
                gc.fillOval(x - radius / 2d, y - radius / 2d, radius, radius);
                gc.setFill(Color.BLACK);
                gc.fillText(Integer.toString(label_count), x, y, 80);

            } else {
                fixSeq.get(j).setGazeDuration(-1);
            }
        }

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        try {
            canvas.snapshot(params, image);
        } catch (Exception e) {

            log.error("Can\'t make properly Snapshot in Fixation Sequence");
        }

        fixSeq.removeIf(fixationPoint -> fixationPoint.getGazeDuration() == -1);
        sequence = fixSeq;
    }

    /**
     * Saves the fixation Sequence to a PNG file
     *
     * @param outputFile The output file (Must be open and writable)
     */
    // creates a clear background image
    public void saveToFile(File outputFile) {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LinkedList<FixationPoint> vertexReduction(LinkedList<FixationPoint> allPoints, double tolerance) {

        int accepted = 0;
        double distance;
        FixationPoint pivotVertex = allPoints.get(accepted);

        LinkedList<FixationPoint> reducedPolyline = new LinkedList<>();
        reducedPolyline.add(pivotVertex);

        for (int i = 1; i < allPoints.size() - 1; i++) {
            distance = Math.sqrt(Math.pow(pivotVertex.getY() - allPoints.get(i).getY(), 2)
                + Math.pow(pivotVertex.getX() - allPoints.get(i).getX(), 2));

            if (distance <= tolerance) {
                // add to the accepted vertex the duration of the reduced vertices -- to adapt the radius
                pivotVertex.setGazeDuration(pivotVertex.getGazeDuration() + allPoints.get(i).getGazeDuration());
                // continue;
            } else {
                reducedPolyline.add(allPoints.get(i));

                accepted = i;

                pivotVertex = allPoints.get(accepted);
            }
        }
        return reducedPolyline;
    }
}
