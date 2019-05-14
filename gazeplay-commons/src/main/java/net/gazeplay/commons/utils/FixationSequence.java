package net.gazeplay.commons.utils;

import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.text.TextAlignment;
import net.gazeplay.commons.utils.FixationPoint;
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
import java.util.Vector;
import java.lang.Math;

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
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        //draw the circles with the labels on top
        gc.setStroke(Color.RED);
        gc.setLineWidth(0.6);

        int label_count = 1;// for the labels of the fixation sequence
        double alpha = -100.0; // angle between 2 lines

        gc.setStroke(Color.RED);
        int x = fixSeq.get(0).getY();
        int y = fixSeq.get(0).getX();

        int radius = 20;

        gc.strokeOval(x-radius/2, y-radius/2, radius, radius);
        gc.setFill(Color.rgb(255, 255, 0, 0.5));//yellow 50% transparency
        gc.fillOval(x-radius/2, y-radius/2, radius, radius);
        gc.setFill(Color.BLACK);
        gc.fillText(Integer.toString(label_count), x, y,40);

//        double[] u = new double[2];
//        double[] v = new double[2];
        double m1; // slope of line between points j-1 and j
        double m2; // slope of line between points j and j+1

        for (int j = 1; j < fixSeq.size()-1; j ++) {

            gc.setStroke(Color.RED);
            x = fixSeq.get(j).getY();
            y = fixSeq.get(j).getX();

            if(x - fixSeq.get(j-1).getY() == 0)
                m1 = (y - fixSeq.get(j-1).getX())/(0.0001);
            else m1 = (y - fixSeq.get(j-1).getX())/(x - fixSeq.get(j-1).getY());

            if(fixSeq.get(j+1).getY() - x == 0)
                m2 = (fixSeq.get(j+1).getX() - y )/(0.0001);
            else m2 = (fixSeq.get(j+1).getX() - y )/(fixSeq.get(j+1).getY() - x);

            alpha = Math.atan(Math.abs((m2 - m1)/(1 + m2*m1)));


            //ideja me cos alpha , kendi midis 2 vectoreve
//            u[0] = ((x - fixSeq.get(j-1).getY()));
//            u[1] = ((y - fixSeq.get(j-1).getX()));
//
//            v[0] = ((fixSeq.get(j+1).getY() - x));
//            v[1] = ((fixSeq.get(j+1).getX() - y));
//
//            double lengthU = Math.sqrt(Math.pow(u[0],2) + Math.pow(u[1],2));
//            double lengthV = Math.sqrt(Math.pow(v[0],2) + Math.pow(v[1],2));
//
//            alpha = Math.acos((u[0] * v[0] + u[1] * v[1])/(lengthU * lengthV));

            // ideja me koeficient kendor
//            if(x - fixSeq.get(j-1).getY() == 0)
//                alpha = Math.atan(Math.PI/2);
//            else
//                alpha = Math.atan((y - fixSeq.get(j+1).getX())/(x - fixSeq.get(j-1).getY()));
//
//            if(fixSeq.get(j+1).getY() - x == 0)
//                alpha1 = Math.atan(Math.PI/2);
//            else
//                alpha1 = Math.atan((fixSeq.get(j+1).getX() - y)/(fixSeq.get(j+1).getY() - x));


            radius = 20; // radius depends on time spent on a position .
            // if alpha < 120 degrees
            //if(alpha < Math.acos((2*Math.PI)/3)  && alpha > Math.acos(0)/*&& alpha1 <  Math.abs(alpha-Math.acos((2*Math.PI)/3))*/ ){
            if(alpha < Math.atan(Math.PI/2)){
                label_count++;
                gc.strokeOval(x-radius/2, y-radius/2, radius, radius);
                gc.setFill(Color.rgb(255, 255, 0, 0.5));//yellow 50% transparency
                gc.fillOval(x-radius/2, y-radius/2, radius, radius);
                gc.setFill(Color.BLACK);
                gc.fillText(Integer.toString(label_count), fixSeq.get(j).getY(), fixSeq.get(j).getX(),40);
            }
            else
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
}
