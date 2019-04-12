package net.gazeplay.commons.utils;

import net.gazeplay.commons.utils.FixationPoint;
import javafx.animation.Interpolator;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

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

    /**
     * Saves the fixation Sequence to a PNG file
     *
     * @param outputFile
     *            The output file (Must be open and writable)
     */

    public FixationSequence(LinkedList<FixationPoint> fixSeq){

    }

    public void saveToFile(File outputFile) { // changeeeeeeee
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
