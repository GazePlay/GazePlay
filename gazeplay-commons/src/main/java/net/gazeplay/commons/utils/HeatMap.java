package net.gazeplay.commons.utils;

import javafx.animation.Interpolator;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a heatmap image from a given 2D array
 */
@Slf4j
public class HeatMap {

    /**
     * Writable image used to create the heatmap image
     */
    @Getter
    private final WritableImage image;
    /**
     * Array of the different colors used to interpolate
     */
    private final List<Color> colors;
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
    private final double subdivisionValue;

    /**
     * Opacity of each pixel
     */
    private final double opacity;

    /**
     * Custom colors constructor, builds a heatmap from the given data, by interpolating the values through the given
     * colors.
     *
     * @param data   monitor data
     * @param colors custom colors for the heatmap, must be on order from minimum to maximum.
     */
    public HeatMap(final double[][] data, final double opacity, final List<Color> colors) {

        this.image = new WritableImage(data[0].length, data.length);
        this.colors = colors;
        this.opacity = opacity;

        // Computing max and min values
        minValue = Double.MAX_VALUE;
        maxValue = Double.MIN_VALUE;
        for (final double[] datum : data) {
            for (final double v : datum) {
                if (v > maxValue) {
                    maxValue = v;
                }
                if (v < minValue && v != 0) {
                    minValue = v;
                }
            }
        }
        subdivisionValue = (maxValue - minValue) / (this.colors.size() - 1);

        // Create heatmap pixel per pixel
        final PixelWriter pxWriter = image.getPixelWriter();
        for (int x = 0; x < data.length; x++) {
            for (int y = 0; y < data[x].length; y++) {
                pxWriter.setColor(y, x, getColor(data[x][y]));
            }
        }
    }

    /**
     * Computes the correct color of the value, by interpolating between the 2 colors in the right subdivision.
     *
     * @param value the value of the pixel (in data).
     * @return the resulting color after interpolation.
     */
    private Color getColor(final double value) {
        if (value == 0) {
            return Color.TRANSPARENT;
        } else {
            double compValue = minValue + subdivisionValue;
            int i = 0; // Once out of the loop, will be the index of the starting color of the interpolation
            while (i < colors.size() - 2 && value >= compValue) { // Finding the right subdivision, in order to get the
                // colors between which the values is located
                i++;
                compValue += subdivisionValue;
            }
            final double red = Interpolator.LINEAR.interpolate(colors.get(i).getRed(), colors.get(i + 1).getRed(),
                (value % subdivisionValue) / subdivisionValue);
            final double green = Interpolator.LINEAR.interpolate(colors.get(i).getGreen(), colors.get(i + 1).getGreen(),
                (value % subdivisionValue) / subdivisionValue);
            final double blue = Interpolator.LINEAR.interpolate(colors.get(i).getBlue(), colors.get(i + 1).getBlue(),
                (value % subdivisionValue) / subdivisionValue);
            return Color.color(red, green, blue, opacity);
        }
    }

    public WritableImage getColorKey(final int width, final int height) {
        final WritableImage keyImage = new WritableImage(width, height);
        final Canvas canvas = new Canvas(width, height);
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        final ArrayList<Stop> stops = new ArrayList<>();
        for (int i = 0; i < colors.size(); i++) {
            stops.add(new Stop((double) i / (double) (colors.size() - 1), colors.get(colors.size() - 1 - i)));
        }
        final LinearGradient heatGradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

        final double margin = height / 20d;
        final double barHeight = height - 2 * margin;
        gc.setFont(new Font(margin));
        gc.setFill(heatGradient);
        gc.fillRect(0, margin, width / 3d, barHeight);

        gc.setStroke(Color.BLACK);
        gc.setFill(Color.WHITE);

        final DecimalFormat numberFormat = new DecimalFormat("#.00");

        for (int i = 0; i < colors.size(); i++) {
            final double y = margin + (double) i / (double) (colors.size() - 1) * barHeight;
            gc.strokeLine(0, y, width / 3d, y);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(numberFormat.format(maxValue - (i * subdivisionValue)) + "", width / 3d + 5, y,
                2 * width / 3d - 5);
        }

        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        canvas.snapshot(params, keyImage);
        return keyImage;
    }
}
