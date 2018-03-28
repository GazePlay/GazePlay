package net.gazeplay.games.colors;

import java.util.ArrayDeque;
import java.util.Deque;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import lombok.Getter;
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

    /**
     * The game context provided.
     */
    @Getter
    private final GameContext gameContext;

    /**
     * The root where to draw everything
     */
    private final Pane root;

    /**
     * The tool box object.
     */
    private ColorToolBox colorToolBox;

    /**
     * The default image url to display
     */
    public static final String DEFAULT_IMAGE_URL = "http://www.supercoloring.com/sites/default/files/styles/coloring_full/public/cif/2015/07/hatsune-miku-coloring-page.png";

    /**
     * On a [0, 1] scale, used to determine the threshold in the difference between two colors to consider that they are
     * equals.
     */
    public static final double COLOR_EQUALITY_THRESHOLD = 10 / 255;

    /**
     * Distance in pixel between two gaze event to consider that the gaze is moving.
     */
    public static final double GAZE_MOVING_THRESHOLD = 25;

    /**
     * Distance in pixel of the current cursor or gaze position in x and y for the gaze indicator.
     */
    public static final double GAZE_INDICATOR_DISTANCE = 5;

    /**
     * The gaze progress indicator to show time before colorization.
     */
    private GazeProgressIndicator gazeProgressIndicator;

    /**
     * The configuration
     */
    @Getter
    private Configuration config;

    /**
     * The pixel writer to into wich we modify pixels
     */
    private PixelWriter pixelWriter;

    /**
     * The pixel reader into wich we read pixels
     */
    private PixelReader pixelReader;

    /**
     * The image linked to the pixelReader and pixelWriter
     */
    @Getter
    private WritableImage writableImg;

    /**
     * The rectangle in which the writableImg is painted
     */
    private Rectangle rectangle;

    /**
     * Should we enableColorization.
     */
    private boolean enableColorization = true;

    /**
     * The colorization event handler
     */
    private EventHandler<Event> colorizationEventHandler;

    public ColorsGame(GameContext gameContext) {

        this.gameContext = gameContext;

        root = gameContext.getRoot();
    }

    @Override
    public void launch() {

        config = ConfigurationBuilder.createFromPropertiesResource().build();

        this.gazeProgressIndicator = new GazeProgressIndicator(15, 15, config.getFixationlength());

        gazeProgressIndicator.toFront();
        this.root.getChildren().add(gazeProgressIndicator);

        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double width = dimension2D.getWidth();
        double height = dimension2D.getHeight();

        buildToolBox(width, height);

        log.info("Toolbox width = {}, height = {}", colorToolBox.getWidth(), colorToolBox.getHeight());
        buildDraw(DEFAULT_IMAGE_URL, width, height);
    }

    @Override
    public void dispose() {

    }

    private void buildToolBox(double width, double height) {

        this.colorToolBox = new ColorToolBox(this.root, this);
        TitledPane colorToolBoxPane = new TitledPane("Colors", colorToolBox);
        colorToolBoxPane.setCollapsible(false);
        colorToolBoxPane.setAnimated(false);

        this.root.getChildren().add(colorToolBoxPane);

        double x = 0;
        double y = height * 0.8;
        colorToolBox.relocate(x, y);
    }

    private void buildDraw(String imgURL, double width, double height) {

        rectangle = new Rectangle(width, height);

        // When size will be calculated, update size of rectangle, but do this only
        // once
        ChangeListener listener = (ChangeListener) new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                updateRectangle();
                colorToolBox.widthProperty().removeListener(this);
            }
        };

        colorToolBox.widthProperty().addListener(listener);

        Image img = new Image(imgURL);

        if (!img.isError()) {
            // awtEditing(img, rectangle);
            javaFXEditing(img);
        }

        root.getChildren().add(rectangle);

        rectangle.toBack();
    }

    private void javaFXEditing(Image image) {

        this.updateImage(image);

        final Stage stage = GazePlay.getInstance().getPrimaryStage();

        // Resizing not really working
        root.widthProperty().addListener((observable) -> {

            updateRectangle();

        });

        root.heightProperty().addListener((observable) -> {

            updateRectangle();

        });

        gameContext.getGazeDeviceManager().addEventFilter(rectangle);

        colorizationEventHandler = buildEventHandler();

        rectangle.addEventFilter(MouseEvent.ANY, colorizationEventHandler);
        rectangle.addEventFilter(GazeEvent.ANY, colorizationEventHandler);

    }

    private void updateRectangle() {

        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double width = dimension2D.getWidth() - colorToolBox.getWidth();
        double height = dimension2D.getHeight();

        rectangle.setTranslateX(colorToolBox.getWidth());

        rectangle.setWidth(width);
        rectangle.setHeight(height);

        rectangle.setFill(new ImagePattern(writableImg));
    }

    /**
     * Change the current image to displat and update everything so it can be colorized.
     * 
     * @param image
     *            The new image to colorize.
     */
    public void updateImage(final Image image) {

        final PixelReader tmpPixelReader = image.getPixelReader();

        if (tmpPixelReader == null) {
            log.info("Error in image loading : ");
            log.error("Colors : unable to read pixels from image");
            return;
        }

        rectangle.setFill(new ImagePattern(image));

        writableImg = new WritableImage(tmpPixelReader, (int) image.getWidth(), (int) image.getHeight());
        pixelWriter = writableImg.getPixelWriter();
        pixelReader = writableImg.getPixelReader();
    }

    private EventHandler<Event> buildEventHandler() {

        final ColorsGame game = this;

        return new EventHandler<Event>() {

            private Double gazeXOrigin = 0.;
            private Double gazeYOrigin = 0.;

            private Double currentX = 0.;
            private Double currentY = 0.;

            @Override
            public void handle(Event event) {

                if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    MouseEvent mouseEvent = (MouseEvent) event;
                    // log.info("clicked at x= {}, y = {}", currentX, currentY);
                    colorize(currentX, currentY);

                } else if (event.getEventType() == GazeEvent.GAZE_ENTERED) {

                    GazeEvent gazeEvent = (GazeEvent) event;

                    // log.info("Gaze event : {}", gazeEvent);

                    gazeXOrigin = gazeEvent.getX();
                    gazeYOrigin = gazeEvent.getY();
                    currentX = gazeXOrigin;
                    currentY = gazeYOrigin;

                    moveGazeIndicator(currentX + GAZE_INDICATOR_DISTANCE, currentY + GAZE_INDICATOR_DISTANCE);

                    gazeProgressIndicator.setOnFinish((ActionEvent event1) -> {

                        colorize(currentX, currentY);
                    });

                    gazeProgressIndicator.start();
                } else if (event.getEventType() == GazeEvent.GAZE_MOVED) {

                    GazeEvent gazeEvent = (GazeEvent) event;
                    currentX = gazeEvent.getX();
                    currentY = gazeEvent.getY();

                    moveGazeIndicator(currentX + GAZE_INDICATOR_DISTANCE, currentY + GAZE_INDICATOR_DISTANCE);

                    // If gaze still around first point
                    if (gazeXOrigin - GAZE_MOVING_THRESHOLD < currentX && gazeXOrigin + GAZE_MOVING_THRESHOLD > currentX
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

                        gazeProgressIndicator.start();
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

                    moveGazeIndicator(currentX + GAZE_INDICATOR_DISTANCE, currentY + GAZE_INDICATOR_DISTANCE);

                    gazeProgressIndicator.setOnFinish((ActionEvent event1) -> {

                        colorize(currentX, currentY);
                    });

                    gazeProgressIndicator.start();
                } else if (event.getEventType() == MouseEvent.MOUSE_MOVED) {

                    MouseEvent mouseEvent = (MouseEvent) event;
                    currentX = mouseEvent.getX();
                    currentY = mouseEvent.getY();

                    moveGazeIndicator(currentX + GAZE_INDICATOR_DISTANCE, currentY + GAZE_INDICATOR_DISTANCE);

                    // If mouse still around first point
                    if (gazeXOrigin - GAZE_MOVING_THRESHOLD < currentX && gazeXOrigin + GAZE_MOVING_THRESHOLD > currentX
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

                        gazeProgressIndicator.start();
                    }
                } else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
                    // log.info("mouse exited = ({},{})", currentX, currentY);
                    gazeProgressIndicator.stop();
                }
            }

        };
    }

    private void moveGazeIndicator(final double x, final double y) {

        if (x < 0 || y < 0 || x > rectangle.getWidth() || y > rectangle.getHeight()) {
            return;
        }

        // We need to take into account that the 0 from event is top left corner of rectangle
        // but not actually the top left corner of screen.
        double newX = x + colorToolBox.getWidth();

        gazeProgressIndicator.setTranslateX(newX);
        gazeProgressIndicator.setTranslateY(y);

    }

    /**
     * Fill a zone with the current selected color.
     * 
     * @param x
     *            The x coordinates of the point to fill from.
     * @param y
     *            The y coordinates of the point to fill from.
     */
    public void colorize(final double x, final double y) {

        int pixelX = (int) (x * writableImg.getWidth() / rectangle.getWidth());
        int pixelY = (int) (y * writableImg.getHeight() / rectangle.getHeight());
        // log.info("pixel at x= {}, y = {}", pixelX, pixelY);

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

    private void javaFXFloodFill(final PixelWriter pixelWriter, final PixelReader pixelReader, Color newColor, int x,
            int y, int width, int height) {

        final Color oldColor = pixelReader.getColor(x, y);

        // floodInColumnAndLineRec(pixelWriter, pixelReader, newColor, x, y, width, height, oldColor);
        floodInColumnAndLine(pixelWriter, pixelReader, newColor, x, y, width, height, oldColor);
    }

    /**
     * Objects used internally that represents a horizontal line of pixels
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

    private void floodInColumnAndLine(final PixelWriter pixelWriter, final PixelReader pixelReader,
            final Color newColor, final int x, final int y, final int width, final int height, final Color oldColor) {

        int leftX = floodInLine(pixelWriter, pixelReader, newColor, x, y, width, true, oldColor);
        int rightX = floodInLine(pixelWriter, pixelReader, newColor, x, y, width, false, oldColor);

        HorizontalZone firstZone = new HorizontalZone(leftX, rightX, y);
        searchZone(firstZone, oldColor, pixelReader, pixelWriter, newColor, width, height);

        while (horiZones.size() > 0) {

            HorizontalZone zone = horiZones.pop();
            // log.info("zone : {}", zone.toString());
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

    public void setEnableColorization(boolean enable) {

        // If we want to stop colorization
        if (!enable) {
            // Hide away the gazeProgressIndicator
            gazeProgressIndicator.stop();

            // Stop registering colorization events
            rectangle.removeEventFilter(MouseEvent.ANY, colorizationEventHandler);
            rectangle.removeEventFilter(GazeEvent.ANY, colorizationEventHandler);

        } else {
            rectangle.addEventFilter(MouseEvent.ANY, colorizationEventHandler);
            rectangle.addEventFilter(GazeEvent.ANY, colorizationEventHandler);
        }

        this.enableColorization = enable;
    }
}
