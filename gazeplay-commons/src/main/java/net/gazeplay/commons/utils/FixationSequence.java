package net.gazeplay.commons.utils;

import com.google.common.graph.Graph;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.text.TextAlignment;
import net.gazeplay.commons.utils.FixationPoint;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;


import javax.imageio.ImageIO;
import javafx.scene.text.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class FixationSequence {

    /**
     * Writable image used to create the fixation Sequence image
     */
    private WritableImage image;

    private static Font sanSerifFont = new Font("SanSerif",10);

    public FixationSequence(int width, int height, LinkedList<FixationPoint> fixSeq) {

        this.image = new WritableImage(width, height);

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        //draw the line of the sequence
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(3);
        for (int i = 0; i < fixSeq.size() - 1; i++) {
            gc.strokeLine(fixSeq.get(i).getY(), fixSeq.get(i).getX(), fixSeq.get(i + 1).getY(),
                    fixSeq.get(i + 1).getX());
        }
        gc.setFont(sanSerifFont);

        //final FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(sanSerifFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);


        //draw the circles with the labels on top
        gc.setStroke(Color.RED);

        gc.setLineWidth(0.6);
        int label_count = 0;

        for (int j = 0; j < fixSeq.size(); j = j + 25) {
            label_count++;

            gc.setStroke(Color.RED);
            int x = fixSeq.get(j).getY();
            int y = fixSeq.get(j).getX();
            int radius = 20;
            gc.strokeOval(x-radius/2, y-radius/2, radius, radius);
            gc.setFill(Color.rgb(255, 255, 0, 0.5));//yellow 50% transparency
            gc.fillOval(x-radius/2, y-radius/2, radius, radius);

            gc.setFill(Color.BLACK);
            gc.fillText(Integer.toString(label_count), fixSeq.get(j).getY(), fixSeq.get(j).getX(),40);
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
}
