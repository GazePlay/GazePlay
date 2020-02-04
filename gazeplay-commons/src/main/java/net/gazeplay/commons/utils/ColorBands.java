package net.gazeplay.commons.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

@Slf4j
public class ColorBands {
    /**
     * Writable image used to create the fixation Sequence image
     */
    @Getter
    private final WritableImage image;

    public ColorBands(final int width, final int height, final LinkedList<FixationPoint> points) {
        this.image = new WritableImage(width, height);

    }

    /**
     * Saves the fixation Sequence to a PNG file
     *
     * @param outputFile The output file (Must be open and writable)
     */
    // creates a clear background image
    public void saveToFile(final File outputFile) {
        final BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
