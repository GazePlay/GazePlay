package net.gazeplay.games.colors;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;

/**
 * Game where you select a color in order to colorize a white and black draw.
 *
 * @author Thomas MEDARD
 */
@Slf4j
public class ColorsGame implements GameLifeCycle {

    private final GameContext gameContext;

    private final Pane root;

    private ColorToolBox colorToolBox;

    public static final String DEFAULT_IMAGE_URL = "http://www.supercoloring.com/sites/default/files/styles/coloring_full/public/cif/2015/07/hatsune-miku-coloring-page.png";

    public ColorsGame(GameContext gameContext) {

        this.gameContext = gameContext;

        root = gameContext.getRoot();

    }

    @Override
    public void launch() {

        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double width = dimension2D.getWidth();
        double height = dimension2D.getHeight();

        buildToolBox(width, height);
        buildDraw(DEFAULT_IMAGE_URL, width, height);
    }

    @Override
    public void dispose() {

    }

    private void buildToolBox(double width, double height) {

        this.colorToolBox = new ColorToolBox();
        Node colorToolBoxPane = new TitledPane("Colors", colorToolBox);

        this.root.getChildren().add(colorToolBoxPane);

        double x = 0;
        double y = height * 0.8;
        colorToolBox.relocate(x, y);
    }

    private void buildDraw(String imgURL, double width, double height) {

        // TODO

        Rectangle rectangle = new Rectangle(width, height);

        Image img = new Image(imgURL, width, height, false, true);
        
        //awtEditing(img, rectangle);
        javaFXEditing(img, rectangle);
        
        root.getChildren().add(rectangle);

        rectangle.toBack();
    }
    
    public void javaFXEditing(Image image, Rectangle rectangle) {
        
        rectangle.setFill(new ImagePattern(image));
        
        final PixelReader tmpPixelReader = image.getPixelReader();
        
        if(tmpPixelReader == null) {
            log.info("Error in image loading : ");
            log.error("Colors : unable to read pixels from image");
            return;
        }
        
        final WritableImage writableImg = new WritableImage(tmpPixelReader, (int)image.getWidth(), (int)image.getHeight());
        final PixelWriter pixelWriter = writableImg.getPixelWriter();
        final PixelReader pixelReader = writableImg.getPixelReader();
        
        log.info("Pixel format = {}", pixelReader.getPixelFormat());
        
         rectangle.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
             
            log.info("clicked : {}", event.getSceneX(), event.getSceneY());

            Color color = pixelReader.getColor((int)event.getSceneX(), (int)event.getSceneY());
            log.info("R = {}, G = {}, B = {}, A = {}", color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity());
            
             javaFXFloodFill(pixelWriter, pixelReader, colorToolBox.getSelectedColorBox().getColor(), 
                     (int)event.getSceneX(), (int)event.getSceneY(), (int)image.getWidth(), (int) image.getHeight());
             
             rectangle.setFill(new ImagePattern(writableImg));
        });
    }
    
    public void javaFXFloodFill(final PixelWriter pixelWriter, final PixelReader pixelReader, 
            Color newColor, int x, int y, int width, int height) {
        
        final Color oldColor = pixelReader.getColor(x, y);
        
        floodInColumnAndLine(pixelWriter, pixelReader, newColor, x, y, width,
                height, oldColor);
        //floodInColumnAndLine(pixelWriter, pixelReader, newColor, x, y, width, height, false);
    }
    
    public void floodInColumnAndLine(final PixelWriter pixelWriter, 
            final PixelReader pixelReader, final Color newColor, final int x, 
            final int y, final int width, final int height, 
            final Color oldColor) {

        int fillL = floodInLine(pixelWriter, pixelReader, newColor, x, y, width, true, oldColor);
        int fillR = floodInLine(pixelWriter, pixelReader, newColor, x, y, width, false, oldColor);
        
        log.info("fillL = {}, fillR = {}", fillL, fillR);

        // checks if applicable up or down
        for (int i = fillL; i <= fillR; i++) {
            if (y > 0 && isEqualColors(pixelReader.getColor(i, y - 1), oldColor))
                floodInColumnAndLine(pixelWriter, pixelReader, newColor, i, y - 1, width, height, oldColor);
            if (y < height - 1 && isEqualColors(pixelReader.getColor(i, y + 1), oldColor))
                floodInColumnAndLine(pixelWriter, pixelReader, newColor, i, y + 1, width, height, oldColor);
        }
    }
    
    public int floodInLine(final PixelWriter pixelWriter, 
            final PixelReader pixelReader, final Color newColor, final int x, 
            final int y, final int width, final boolean isLeftFIll, 
            final Color oldColor) {
        
            int currentX = x;

            // Right fill
            do {
                
                pixelWriter.setColor(currentX, y, newColor);
                
                if(isLeftFIll) 
                    currentX--;
                else 
                    currentX++;
            } while(currentX >= 0 && currentX < width - 1 && 
                    isEqualColors(pixelReader.getColor(currentX, y), oldColor));
            
            if(isLeftFIll) 
                currentX++;
            else {

                currentX--;
            }
            
            return currentX;
    }
    
    public void awtEditing(Image img, Rectangle rectangle) {
        BufferedImage buffImg = SwingFXUtils.fromFXImage(img, null);
        
        if (buffImg == null) {
            log.info("Unable to write into image");
        }
        rectangle.setFill(new ImagePattern(img));
        // testRect.setFill(Color.RED);
        rectangle.addEventFilter(MouseEvent.MOUSE_CLICKED, (event) -> {

            Point loc = new Point((int) event.getSceneX(), (int) event.getSceneY());

            log.info("clicked : {}", loc.getX(), loc.getY());

            awtFloodFill(buffImg, colorToolBox.getSelectedColorBox().getColor(), loc);

            Image newImg = SwingFXUtils.toFXImage(buffImg, null);
            rectangle.setFill(new ImagePattern(newImg));
        });
        
    }

    /**
     * Fills the selected pixel and all surrounding pixels of the same color with the fill color.
     * 
     * @param img
     *            image on which operation is applied
     * @param fillColor
     *            color to be filled in
     * @param loc
     *            location at which to start fill
     * @throws IllegalArgumentException
     *             if loc is out of bounds of the image
     * @see http://www.codecodex.com/wiki/Implementing_the_flood_fill_algorithm
     */
    public static void awtFloodFill(BufferedImage img, Color fillColor, Point loc) {
        if (loc.x < 0 || loc.x >= img.getWidth() || loc.y < 0 || loc.y >= img.getHeight())
            throw new IllegalArgumentException();

        WritableRaster raster = img.getRaster();
        int[] fill = new int[] { (int) (fillColor.getRed() * 255), (int) (fillColor.getGreen() * 255),
                (int) (fillColor.getBlue() * 255), (int) fillColor.getOpacity() * 255 };
        int[] old = raster.getPixel(loc.x, loc.y, new int[4]);
        old[3] = (int) fillColor.getOpacity() * 255;

        // log.info("Color = {}", fill);
        // log.info("R = {}, G={},B={}", (int) (fillColor.getRed() * 255), fillColor.getGreen(), fillColor.getBlue());

        // Checks trivial case where loc is of the fill color
        if (isEqualRgba(fill, old))
            return;

        floodLoop(raster, loc.x, loc.y, fill, old);
    }

    /**
     * Recursively fills surrounding pixels of the old color
     * 
     * @param raster
     * @param x
     * @param y
     * @param fill
     * @param old
     * @see http://www.codecodex.com/wiki/Implementing_the_flood_fill_algorithm
     */
    private static void floodLoop(WritableRaster raster, int x, int y, int[] fill, int[] old) {
        java.awt.Rectangle bounds = raster.getBounds();
        int[] aux = { 255, 255, 255, 255 };

        // finds the left side, filling along the way
        int fillL = x;
        do {
            raster.setPixel(fillL, y, fill);
            fillL--;
            /*
             * log.info("FillL = {}, pixel = {}", fillL, raster.getPixel(fillL, y, aux)); log.info("OldPixel = {}",
             * old); log.info("IsEqual = {}", isEqualRgba(raster.getPixel(fillL, y, aux), old));
             */

        } while (fillL >= 0 && isEqualRgba(raster.getPixel(fillL, y, aux), old));
        fillL++;

        // find the right right side, filling along the way
        int fillR = x;
        do {
            raster.setPixel(fillR, y, fill);
            fillR++;
        } while (fillR < bounds.width - 1 && isEqualRgba(raster.getPixel(fillR, y, aux), old));
        fillR--;

        // checks if applicable up or down
        for (int i = fillL; i <= fillR; i++) {
            if (y > 0 && isEqualRgba(raster.getPixel(i, y - 1, aux), old))
                floodLoop(raster, i, y - 1, fill, old);
            if (y < bounds.height - 1 && isEqualRgba(raster.getPixel(i, y + 1, aux), old))
                floodLoop(raster, i, y + 1, fill, old);
        }
    }

    /**
     * Returns true if RGBA arrays are equivalent, false otherwise Could use Arrays.equals(int[], int[]), but this is
     * probably a little faster...
     * 
     * @param pix1
     * @param pix2
     * @return
     * @see http://www.codecodex.com/wiki/Implementing_the_flood_fill_algorithm
     */
    private static boolean isEqualRgba(int[] pix1, int[] pix2) {

        return pix1[0] == pix2[0] && pix1[1] == pix2[1] && pix1[2] == pix2[2] && pix1[3] == pix2[3];
    }
    
    private static boolean isEqualColors(final Color color1, final Color color2) {
        
        if(color1.equals(color2)) {
            return true;
        }
        
        return false;
    }
}
