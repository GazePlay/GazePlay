package net.gazeplay.commons.utils;

import javafx.animation.Interpolator;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Creates a heatmap image from a given 2D array
 */
public class HeatMap {

    /**
     * Max value in the data.
     * Used for interpolation.
     */
    private double maxValue;
    /**
     * Min value in the data.
     * Used for interpolation.
     */
    private double minValue;
    /**
     * Mid-point value in the data.
     * Used for interpolation (3 colors).
     */
    private double midValue;

    private WritableImage image;

    public HeatMap(double[][] data) {
        image = new WritableImage(data[0].length, data.length);

        //Computing max and min values
        minValue = Double.MAX_VALUE;
        maxValue = Double.MIN_VALUE;
        for(int x = 0; x < data.length; x++){
            for(int y = 0; y < data[x].length; y++){
                if(data[x][y] > maxValue){
                    maxValue = data[x][y];
                }
                if(data[x][y] < minValue){
                    minValue = data[x][y];
                }
            }
        }
        midValue = (minValue + maxValue) / 2.0;

        PixelWriter pxWriter = image.getPixelWriter();

        for(int x = 0; x < data.length; x++) {
            for (int y = 0; y < data[x].length; y++) {
                pxWriter.setColor(y, x, getColor(data[x][y]));
            }
        }


    }

    private Color getColor(double value){
        if(value < maxValue * 0.1){
            return Color.BLUE;
        }else {
            double red = value >= midValue ? 1.0 : Interpolator.LINEAR.interpolate(0.0, 1.0, value / midValue);
            double green = value < midValue ? 1.0 : Interpolator.LINEAR.interpolate(0.0, 1.0, (value - midValue) / midValue);
            return Color.color(red, green, 0.0);
        }
    }

    public void saveToFile(File outputFile){
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
