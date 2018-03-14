package net.gazeplay.games.colors;

import java.util.ArrayDeque;
import java.util.Deque;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

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

    // public static final String DEFAULT_IMAGE_URL =
    // "https://www.publicdomainpictures.net/pictures/190000/velka/outlnes-katze.jpg";
    // public static final String DEFAULT_IMAGE_URL =
    // "https://www.publicdomainpictures.net/pictures/160000/velka/girl-and-dog-coloring-page.jpg";
    public static final String DEFAULT_IMAGE_URL = "http://www.supercoloring.com/sites/default/files/styles/coloring_full/public/cif/2015/07/hatsune-miku-coloring-page.png";

    /**
     * On a [0, 1] scale, determine the threshold in the difference between two colors to consider that they are equals.
     */
    public static final double COLOR_EQUALITY_THRESHOLD = 10 / 255;

    /**
     * Distance between two gaze event to consider that the gaze is moving.
     */
    public static final double GAZE_MOVING_THRESHOLD = 25;

    private GazeProgressIndicator gazeProgressIndicator;

    private Configuration config;

    private PixelWriter pixelWriter;

    private PixelReader pixelReader;

    private WritableImage writableImg;

    private Rectangle rectangle;

    public ColorsGame(GameContext gameContext) {

        this.gameContext = gameContext;

        root = gameContext.getRoot();
    }

    @Override
    public void launch() {

        config = ConfigurationBuilder.createFromPropertiesResource().build();

        this.gazeProgressIndicator = new GazeProgressIndicator(15, 15,
                config.getFixationlength());

        // TODO : translate somewhere appropriate, or maybe direclty on the gaze
        gazeProgressIndicator.setTranslateX(250);
        gazeProgressIndicator.setTranslateY(250);
        gazeProgressIndicator.toFront();
        this.root.getChildren().add(gazeProgressIndicator);

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

        rectangle = new Rectangle(width, height);

        Image img = new Image(imgURL, width, height, false, true);

        if (!img.isError()) {
            // awtEditing(img, rectangle);
            javaFXEditing(img, rectangle);
        }

        root.getChildren().add(rectangle);

        rectangle.toBack();
    }

    public void javaFXEditing(Image image, Rectangle rectangle) {

        rectangle.setFill(new ImagePattern(image));

        final PixelReader tmpPixelReader = image.getPixelReader();

        if (tmpPixelReader == null) {
            log.info("Error in image loading : ");
            log.error("Colors : unable to read pixels from image");
            return;
        }

        writableImg = new WritableImage(tmpPixelReader, (int) rectangle.getWidth(), (int) rectangle.getHeight());
        pixelWriter = writableImg.getPixelWriter();
        pixelReader = writableImg.getPixelReader();
        
        final Stage stage = GazePlay.getInstance().getPrimaryStage();

        // Resizing not really working
        stage.widthProperty().addListener((observable) -> {

            rectangle.setWidth(stage.getWidth());
            rectangle.setFill(new ImagePattern(writableImg));
            
        });

        stage.heightProperty().addListener((observable) -> {
            rectangle.setHeight(stage.getHeight());
            rectangle.setFill(new ImagePattern(writableImg));
            
        });

        EventHandler<Event> eventHandler = buildEventHandler(pixelReader, pixelWriter, writableImg, rectangle);

        rectangle.addEventFilter(MouseEvent.ANY, eventHandler);
        rectangle.addEventFilter(GazeEvent.ANY, eventHandler);

    }

    public EventHandler<Event> buildEventHandler(final PixelReader pixelReader, final PixelWriter pixelWriter,
            final WritableImage writableImg, final Rectangle rectangle) {

        return new EventHandler<Event>() {

            private Double gazeXOrigin = 0.;
            private Double gazeYOrigin = 0.;
            
            private Double currentX = 0.;
            private Double currentY = 0.;

            @Override
            public void handle(Event event) {

                if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    MouseEvent mouseEvent = (MouseEvent) event;
                    log.info("clicked at x= {}, y = {}", currentX, currentY);
                    colorize(currentX, currentY);

                } else if (event.getEventType() == GazeEvent.GAZE_ENTERED) {

                    GazeEvent gazeEvent = (GazeEvent) event;
                    
                    gazeXOrigin = gazeEvent.getX();
                    gazeYOrigin = gazeEvent.getY();
                    currentX = gazeXOrigin;
                    currentY = gazeYOrigin;

                    gazeProgressIndicator.setOnFinish((ActionEvent event1) -> {

                        colorize(currentX, currentY);
                    });

                    gazeProgressIndicator.play();
                } else if (event.getEventType() == GazeEvent.GAZE_MOVED) {

                    GazeEvent gazeEvent = (GazeEvent) event;
                    currentX = gazeEvent.getX();
                    currentY = gazeEvent.getY();

                    // If gaze still around first point
                    if (gazeXOrigin - GAZE_MOVING_THRESHOLD < currentX
                            && gazeXOrigin + GAZE_MOVING_THRESHOLD > currentX
                            && gazeYOrigin - GAZE_MOVING_THRESHOLD < currentY
                            && gazeYOrigin + GAZE_MOVING_THRESHOLD > currentY) {

                        // Do nothin
                    }
                    // If gaze move far away
                    else {

                        gazeXOrigin = currentX;
                        gazeYOrigin = currentY;

                        gazeProgressIndicator.stop();
                        gazeProgressIndicator.setOnFinish((ActionEvent event1) -> {

                            colorize(currentX, currentY);
                        });

                        gazeProgressIndicator.play();
                    }
                }
                // If gaze quit
                else if (event.getEventType() == GazeEvent.GAZE_EXITED) {
                    gazeProgressIndicator.stop();
                } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {

                    MouseEvent mouseEvent = (MouseEvent) event;
                    gazeXOrigin = mouseEvent.getX();
                    gazeYOrigin = mouseEvent.getY();
                    currentX = gazeXOrigin;
                    currentY = gazeYOrigin;

                    gazeProgressIndicator.setOnFinish((ActionEvent event1) -> {

                        colorize(currentX, currentY);
                    });

                    gazeProgressIndicator.play();
                } else if (event.getEventType() == MouseEvent.MOUSE_MOVED) {

                    MouseEvent mouseEvent = (MouseEvent) event;
                    currentX = mouseEvent.getX();
                    currentY = mouseEvent.getY();

                    // If mouse still around first point
                    if (gazeXOrigin - GAZE_MOVING_THRESHOLD < currentX
                            && gazeXOrigin + GAZE_MOVING_THRESHOLD > currentX
                            && gazeYOrigin - GAZE_MOVING_THRESHOLD < currentY
                            && gazeYOrigin + GAZE_MOVING_THRESHOLD > currentY) {

                        // Do nothin
                    }
                    // If mouse move far away
                    else {

                        gazeXOrigin = currentX;
                        gazeYOrigin = currentY;

                        gazeProgressIndicator.stop();
                        gazeProgressIndicator.setOnFinish((ActionEvent event1) -> {

                            colorize(currentX, currentY);
                        });

                        gazeProgressIndicator.play();
                    }
                }
                else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
                    gazeProgressIndicator.stop();
                }
            }

        };

    }

    /**
     * Fill a zone with the current selected color.
     * @param x The x coordinates of the point to fill from.
     * @param y The y coordinates of the point to fill from.
     */
    public void colorize(final double x, final double y) {
        
        
        int pixelX = (int) (x * writableImg.getWidth() / rectangle.getWidth());
        int pixelY = (int) (y * writableImg.getHeight()/ rectangle.getHeight());
        //log.info("pixel at x= {}, y = {}", pixelX, pixelY);

        Color color = pixelReader.getColor((int) pixelX, (int) pixelY);
        // log.info("R = {}, G = {}, B = {}, A = {}", color.getRed(), color.getGreen(), color.getBlue(),
        // color.getOpacity());

        if (!isEqualColors(color, colorToolBox.getSelectedColorBox().getColor())) {
            javaFXFloodFill(pixelWriter, pixelReader, colorToolBox.getSelectedColorBox().getColor(), pixelX, pixelY,
                    (int) writableImg.getWidth(), (int) writableImg.getHeight());
        }

        rectangle.setFill(new ImagePattern(writableImg));
        rectangle.toBack();
    }

    public void javaFXFloodFill(final PixelWriter pixelWriter, final PixelReader pixelReader, Color newColor, int x,
            int y, int width, int height) {

        final Color oldColor = pixelReader.getColor(x, y);

        // floodInColumnAndLineRec(pixelWriter, pixelReader, newColor, x, y, width, height, oldColor);
        floodInColumnAndLine(pixelWriter, pixelReader, newColor, x, y, width, height, oldColor);
    }

    /*
     * public void floodInColumnAndLineRec(final PixelWriter pixelWriter, final PixelReader pixelReader, final Color
     * newColor, final int x, final int y, final int width, final int height, final Color oldColor) {
     * 
     * int fillL = floodInLine(pixelWriter, pixelReader, newColor, x, y, width, true, oldColor); int fillR =
     * floodInLine(pixelWriter, pixelReader, newColor, x, y, width, false, oldColor);
     * 
     * // log.info("fillL = {}, fillR = {}", fillL, fillR);
     * 
     * // checks if applicable up or down for (int i = fillL; i <= fillR; i++) { if (y > 0 &&
     * isEqualColors(pixelReader.getColor(i, y - 1), oldColor)) floodInColumnAndLineRec(pixelWriter, pixelReader,
     * newColor, i, y - 1, width, height, oldColor); if (y < height - 1 && isEqualColors(pixelReader.getColor(i, y + 1),
     * oldColor)) floodInColumnAndLineRec(pixelWriter, pixelReader, newColor, i, y + 1, width, height, oldColor); } }
     */

    private class HorizontalZone {
        public int leftX;
        public int rightX;
        public int y;

        public HorizontalZone(int lX, int rX, int y) {
            this.leftX = lX;
            this.rightX = rX;
            this.y = y;
        }

        @Override
        public String toString() {
            return "HorizontalZone{" + "leftX=" + leftX + ", rightX=" + rightX + ", y=" + y + '}';
        }
    }

    private final Deque<HorizontalZone> horiZones = new ArrayDeque<HorizontalZone>();

    public void floodInColumnAndLine(final PixelWriter pixelWriter, final PixelReader pixelReader, final Color newColor,
            final int x, final int y, final int width, final int height, final Color oldColor) {

        int leftX = floodInLine(pixelWriter, pixelReader, newColor, x, y, width, true, oldColor);
        int rightX = floodInLine(pixelWriter, pixelReader, newColor, x, y, width, false, oldColor);

        HorizontalZone firstZone = new HorizontalZone(leftX, rightX, y);
        searchZone(firstZone, oldColor, pixelReader, pixelWriter, newColor, width, height);

        while (horiZones.size() > 0) {

            HorizontalZone zone = horiZones.pop();
            //log.info("zone : {}", zone.toString());
            searchZone(zone, oldColor, pixelReader, pixelWriter, newColor, width, height);
        }
    }

    private void searchZone(final HorizontalZone zone, final Color oldColor, final PixelReader pixelReader,
            final PixelWriter pixelWriter, final Color newColor, final int width, final int height) {

        // Search for left and right of the zone
        int leftX = floodInLine(pixelWriter, pixelReader, newColor, zone.leftX, zone.y, width, true, oldColor);
        int rightX = floodInLine(pixelWriter, pixelReader, newColor, zone.rightX, zone.y, width, false, oldColor);

        for (int i = leftX; i <= rightX; ++i) {

            // Search for available zone to colorize upward
            if (zone.y > 0 && isEqualColors(pixelReader.getColor(i, zone.y - 1), oldColor)) {

                int newLeftX = floodInLine(pixelWriter, pixelReader, newColor, i, zone.y - 1, width, true, oldColor);
                int newRightX = floodInLine(pixelWriter, pixelReader, newColor, i, zone.y - 1, width, false, oldColor);

                horiZones.add(new HorizontalZone(newLeftX, newRightX, zone.y - 1));
            }
            // Search for available zone to colorize downward
            if (zone.y < height - 1 && isEqualColors(pixelReader.getColor(i, zone.y + 1), oldColor)) {

                int newLeftX = floodInLine(pixelWriter, pixelReader, newColor, i, zone.y + 1, width, true, oldColor);
                int newRightX = floodInLine(pixelWriter, pixelReader, newColor, i, zone.y + 1, width, false, oldColor);

                horiZones.add(new HorizontalZone(newLeftX, newRightX, zone.y + 1));
            }
        }
    }

    private int floodInLine(final PixelWriter pixelWriter, final PixelReader pixelReader, final Color newColor,
            final int x, final int y, final int width, final boolean isLeftFIll, final Color oldColor) {

        int currentX = x;

        // fill
        do {

            pixelWriter.setColor(currentX, y, newColor);

            if (isLeftFIll)
                currentX--;
            else
                currentX++;
        } while (currentX >= 0 && currentX < width - 1 && isEqualColors(pixelReader.getColor(currentX, y), oldColor));

        if (isLeftFIll)
            currentX++;
        else {

            currentX--;
        }

        return currentX;
    }

    /*
     * public void awtEditing(Image img, Rectangle rectangle) { BufferedImage buffImg = SwingFXUtils.fromFXImage(img,
     * null);
     * 
     * if (buffImg == null) { log.info("Unable to write into image"); } rectangle.setFill(new ImagePattern(img)); //
     * testRect.setFill(Color.RED); rectangle.addEventFilter(MouseEvent.MOUSE_CLICKED, (event) -> {
     * 
     * Point loc = new Point((int) event.getSceneX(), (int) event.getSceneY());
     * 
     * log.info("clicked : {}", loc.getX(), loc.getY());
     * 
     * awtFloodFill(buffImg, colorToolBox.getSelectedColorBox().getColor(), loc);
     * 
     * Image newImg = SwingFXUtils.toFXImage(buffImg, null); rectangle.setFill(new ImagePattern(newImg)); });
     * 
     * }
     */

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
    /*
     * public static void awtFloodFill(BufferedImage img, Color fillColor, Point loc) { if (loc.x < 0 || loc.x >=
     * img.getWidth() || loc.y < 0 || loc.y >= img.getHeight()) throw new IllegalArgumentException();
     * 
     * WritableRaster raster = img.getRaster(); int[] fill = new int[] { (int) (fillColor.getRed() * 255), (int)
     * (fillColor.getGreen() * 255), (int) (fillColor.getBlue() * 255), (int) fillColor.getOpacity() * 255 }; int[] old
     * = raster.getPixel(loc.x, loc.y, new int[4]); old[3] = (int) fillColor.getOpacity() * 255;
     * 
     * // log.info("Color = {}", fill); // log.info("R = {}, G={},B={}", (int) (fillColor.getRed() * 255),
     * fillColor.getGreen(), fillColor.getBlue());
     * 
     * // Checks trivial case where loc is of the fill color if (isEqualRgba(fill, old)) return;
     * 
     * floodLoop(raster, loc.x, loc.y, fill, old); }
     */

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
    /*
     * private static void floodLoop(WritableRaster raster, int x, int y, int[] fill, int[] old) { java.awt.Rectangle
     * bounds = raster.getBounds(); int[] aux = { 255, 255, 255, 255 };
     * 
     * // finds the left side, filling along the way int fillL = x; do { raster.setPixel(fillL, y, fill); fillL--;
     * 
     * } while (fillL >= 0 && isEqualRgba(raster.getPixel(fillL, y, aux), old)); fillL++;
     * 
     * // find the right right side, filling along the way int fillR = x; do { raster.setPixel(fillR, y, fill); fillR++;
     * } while (fillR < bounds.width - 1 && isEqualRgba(raster.getPixel(fillR, y, aux), old)); fillR--;
     * 
     * // checks if applicable up or down for (int i = fillL; i <= fillR; i++) { if (y > 0 &&
     * isEqualRgba(raster.getPixel(i, y - 1, aux), old)) floodLoop(raster, i, y - 1, fill, old); if (y < bounds.height -
     * 1 && isEqualRgba(raster.getPixel(i, y + 1, aux), old)) floodLoop(raster, i, y + 1, fill, old); } }
     */

    /**
     * Returns true if RGBA arrays are equivalent, false otherwise Could use Arrays.equals(int[], int[]), but this is
     * probably a little faster...
     * 
     * @param pix1
     * @param pix2
     * @return
     * @see http://www.codecodex.com/wiki/Implementing_the_flood_fill_algorithm
     */
    /*
     * private static boolean isEqualRgba(int[] pix1, int[] pix2) {
     * 
     * return pix1[0] == pix2[0] && pix1[1] == pix2[1] && pix1[2] == pix2[2] && pix1[3] == pix2[3]; }
     */

    /**
     * Detect if a color is close enough to another one to be considered the same.
     * 
     * @param color1
     *            The first color to compare
     * @param color2
     *            The second color to compare
     * @return true if considered same, false otherwise.
     */
    private static boolean isEqualColors(final Color color1, final Color color2) {

        boolean redEq = false;
        boolean greEq = false;
        boolean bluEq = false;

        if (color1.getRed() <= color2.getRed() + COLOR_EQUALITY_THRESHOLD
                && color1.getRed() >= color2.getRed() - COLOR_EQUALITY_THRESHOLD) {
            redEq = true;
        }

        if (color1.getGreen() <= color2.getGreen() + COLOR_EQUALITY_THRESHOLD
                && color1.getGreen() >= color2.getGreen() - COLOR_EQUALITY_THRESHOLD) {
            greEq = true;
        }

        if (color1.getBlue() <= color2.getBlue() + COLOR_EQUALITY_THRESHOLD
                && color1.getBlue() >= color2.getBlue() - COLOR_EQUALITY_THRESHOLD) {
            bluEq = true;
        }

        return redEq && greEq && bluEq;
    }
}
