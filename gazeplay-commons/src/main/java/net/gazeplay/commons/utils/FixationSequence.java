package net.gazeplay.commons.utils;

import com.google.common.graph.Graph;
import javafx.scene.SnapshotParameters;
import net.gazeplay.commons.utils.FixationPoint;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;


public class FixationSequence {

    /**
     * Writable image used to create the fixation Sequence image
     */
    private WritableImage image;


    public FixationSequence(int width,int height, LinkedList<FixationPoint> fixSeq) {

        this.image = new WritableImage(width, height);

        Canvas canvas = new Canvas(width,height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(4);
        for(int i = 0 ; i < fixSeq.size()-1; i++){
            gc.strokeLine(fixSeq.get(i).getY(),fixSeq.get(i).getX(),fixSeq.get(i+1).getY(),fixSeq.get(i+1).getX());
        }
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        canvas.snapshot(params, image);

//        PixelWriter pxWriter = image.getPixelWriter();
//
//        for(int i = 0 ; i < fixSeq.size(); i ++){
//            pxWriter.setColor(fixSeq.get(i).getY(), fixSeq.get(i).getX(), Color.ORANGE);
//        }
    }
    /**
     * Saves the fixation Sequence to a PNG file
     *
     * @param outputFile
     *            The output file (Must be open and writable)
     */
    //creates a clear background image
    public void saveToFile(File outputFile) {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
