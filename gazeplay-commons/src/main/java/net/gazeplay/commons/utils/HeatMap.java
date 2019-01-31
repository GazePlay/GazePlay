package net.gazeplay.commons.utils;

import javafx.animation.Interpolator;
import javafx.embed.swing.SwingFXUtils;
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
     * Default colors, in case the default constructor is called
     */
    private static final Color[] defaultColors = { Color.DARKBLUE, Color.GREEN, Color.YELLOW, Color.RED };

    /**
     * Writable image used to create the heatmap image
     */
    private WritableImage image;
    /**
     * Array of the different colors used to interpolate
     */
    private Color[] colors;
    /**
     * Maximum value of the data
     */
    private double maxValue;
    /**
     * Minimum value of the data
     */
    private double minValue;
    /**
     * Value interval between each color
     */
    private double subdivisionValue;

    /**
     * Default constructor, uses dark blue, green, yellow, and red as color variants.
     * 
     * @param data
     *            monitor data
     */
    public HeatMap(double[][] data) {
        this(data, defaultColors);
    }

    /**
     * Custom colors constructor, builds a heatmap from the given data, by interpolating the values through the given
     * colors.
     * 
     * @param data
     *            monitor data
     * @param colors
     *            custom colors for the heatmap, must be on order from minimum to maximum.
     */
    public HeatMap(double[][] data, Color[] colors) {
        this.image = new WritableImage(data[0].length, data.length);
        this.colors = colors;

        // Computing max and min values
        minValue = Double.MAX_VALUE;
        maxValue = Double.MIN_VALUE;
        for (int x = 0; x < data.length; x++) {
            for (int y = 0; y < data[x].length; y++) {
                if (data[x][y] > maxValue) {
                    maxValue = data[x][y];
                }
                if (data[x][y] < minValue) {
                    minValue = data[x][y];
                }
            }
        }
        subdivisionValue = (maxValue - minValue) / (this.colors.length - 1);

        // Create heatmap pixel per pixel
        PixelWriter pxWriter = image.getPixelWriter();
        for (int x = 0; x < data.length; x++) {
            for (int y = 0; y < data[x].length; y++) {
                pxWriter.setColor(y, x, getColor(data[x][y]));
            }
        }
    }

    /**
     * Computes the correct color of the value, by interpolating between the 2 colors in the right subdivision.
     * 
     * @param value
     *            the value of the pixel (in data).
     * @return the resulting color after interpolation.
     */
    private Color getColor(double value) {
        double compValue = minValue + subdivisionValue;
        int i = 0; // Once out of the loop, will be the index of the starting color of the interpolation
        while (compValue < maxValue && value >= compValue) { // Finding the right subdivision, in order to get the
                                                             // colors between which the values is located
            i++;
            compValue += subdivisionValue;
        }
        double red = Interpolator.LINEAR.interpolate(colors[i].getRed(), colors[i + 1].getRed(),
                (value % subdivisionValue) / subdivisionValue);
        double green = Interpolator.LINEAR.interpolate(colors[i].getGreen(), colors[i + 1].getGreen(),
                (value % subdivisionValue) / subdivisionValue);
        double blue = Interpolator.LINEAR.interpolate(colors[i].getBlue(), colors[i + 1].getBlue(),
                (value % subdivisionValue) / subdivisionValue);
        return Color.color(red, green, blue);
    }

    /**
     * Saves the heatmap to a PNG file
     * 
     * @param outputFile
     *            The output file (Must be open and writable)
     */
    public void saveToFile(File outputFile) {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
