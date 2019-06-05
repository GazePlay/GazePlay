package net.gazeplay.commons.utils;

import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.text.TextAlignment;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.lang.Math;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FixationSequence {

    /**
     * Writable image used to create the fixation Sequence image
     */
    private WritableImage image;

    private static Font sanSerifFont = new Font("SanSerif", 10);

    public FixationSequence(int width, int height, LinkedList<FixationPoint> fixSeq) {

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
            log.info("Point nb :" + i + ", firstGaze = " + fixSeq.get(i).getFirstGaze() + ", gazeDuration = "
                    + fixSeq.get(i).getGazeDuration() + ", x = " + fixSeq.get(i).getY() + " , y = "
                    + fixSeq.get(i).getX());
        }
        gc.setEffect(null);
        gc.setFont(sanSerifFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        // draw the circles with the labels on top
        gc.setStroke(Color.RED);
        gc.setLineWidth(1);

        int label_count = 1;// for the labels of the fixation sequence

        gc.setStroke(Color.RED);
        int x = fixSeq.get(0).getY();
        int y = fixSeq.get(0).getX();

        int radius = 45; // central fixation bias . Read more about it at
        // https://imotions.com/blog/7-terms-metrics-eye-tracking/

        gc.strokeOval(x - radius / 2, y - radius / 2, radius, radius);
        gc.setFill(Color.rgb(255, 255, 0, 0.5));// yellow 50% transparency
        gc.fillOval(x - radius / 2, y - radius / 2, radius, radius);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Verdana", 25));
        gc.fillText(Integer.toString(label_count), x, y, 90);

        double duration;

        for (int j = 1; j < fixSeq.size() - 1; j++) {

            gc.setStroke(Color.RED);
            x = fixSeq.get(j).getY();
            y = fixSeq.get(j).getX();
            duration = fixSeq.get(j).getGazeDuration();

            if (duration > 20) {
                label_count++;
                radius = 45 + (int) duration / 100;
                gc.strokeOval(x - radius / 2, y - radius / 2, radius, radius);
                gc.setFill(Color.rgb(255, 255, 0, 0.5));// yellow 50% transparency
                gc.fillOval(x - radius / 2, y - radius / 2, radius, radius);
                gc.setFill(Color.BLACK);
                gc.fillText(Integer.toString(label_count), x, y, 80);

            } else
                continue;
        }
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        canvas.snapshot(params, image);
    }

    /**
     * Saves the fixation Sequence to a PNG file
     *
     * @param outputFile
     *            The output file (Must be open and writable)
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
    // Vertex Cluster Reduction -- successive vertices that are clustered too closely are reduced to a single vertex

    public LinkedList<FixationPoint> vertexReduction(LinkedList<FixationPoint> allPoints, double tolerance) {

        int accepted = 0;
        double distance = 0.0;
        FixationPoint pivotVertex = allPoints.get(accepted);

        LinkedList<FixationPoint> reducedPolyline = new LinkedList<FixationPoint>();
        reducedPolyline.add(pivotVertex);

        for (int i = 1; i < allPoints.size() - 1; i++) {
            distance = Math.sqrt(Math.pow(pivotVertex.getY() - allPoints.get(i).getY(), 2)
                    + Math.pow(pivotVertex.getX() - allPoints.get(i).getX(), 2));

            if (distance <= tolerance) {
                // add to the accepted vertex the duration of the reduced vertices -- to adapt the radius
                pivotVertex.setGazeDuration(pivotVertex.getGazeDuration() + allPoints.get(i).getGazeDuration());
                continue;
            }

            else {
                reducedPolyline.add(allPoints.get(i));

                accepted = i;

                pivotVertex = allPoints.get(accepted);
            }
        }
        return reducedPolyline;
    }
}
