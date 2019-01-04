package net.gazeplay.games.colors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.Translator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

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
     * The default image to display
     */
    // public static final String DEFAULT_IMAGE =
    // "http://pre07.deviantart.net/c66f/th/pre/i/2016/195/f/8/hatsune_miku_v4x_render_by_katrinasantiago0627-da9y7yr.png";
    public static final String DEFAULT_IMAGE = "data/colors/images/coloriage-dauphins-2.gif";

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

    public static final double AVG_THRESHOLD = 0.39;

    /**
     * The gaze progress indicator to show time before colorization.
     */
    private AbstractGazeIndicator gazeProgressIndicator;

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
    @Getter
    private final BooleanProperty drawingEnable = new SimpleBooleanProperty(this, "isDrawingEnable", true);

    /**
     * The colorization event handler
     */
    private EventHandler<Event> colorizationEventHandler;

    private final ColorsGamesStats stats;

    private final TitledPane toolBoxPane;

    public ColorsGame(GameContext gameContext, final ColorsGamesStats stats) {

        this.gameContext = gameContext;
        this.stats = stats;

        root = gameContext.getRoot();

        final Translator translator = GazePlay.getInstance().getTranslator();
        toolBoxPane = new TitledPane(translator.translate("Colors!"), colorToolBox);

        drawingEnable.addListener((observable, oldValue, newValue) -> {
            // If we want to stop colorization
            if (!newValue) {
                // Hide away the gazeProgressIndicator
                gazeProgressIndicator.stop();

                // Stop registering colorization events
                rectangle.removeEventFilter(MouseEvent.ANY, colorizationEventHandler);
                rectangle.removeEventFilter(GazeEvent.ANY, colorizationEventHandler);

            } else {
                rectangle.addEventFilter(MouseEvent.ANY, colorizationEventHandler);
                rectangle.addEventFilter(GazeEvent.ANY, colorizationEventHandler);
            }
        });
    }

    @Override
    public void launch() {

        this.gazeProgressIndicator = new GazeFollowerIndicator(root);

        this.root.getChildren().add(gazeProgressIndicator);

        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double width = dimension2D.getWidth();
        double height = dimension2D.getHeight();

        buildToolBox(width, height);

        // log.info("Toolbox width = {}, height = {}", colorToolBox.getWidth(), colorToolBox.getHeight());
        buildDraw(DEFAULT_IMAGE, width, height);
    }

    @Override
    public void dispose() {

    }

    private void buildToolBox(double width, double height) {

        this.colorToolBox = new ColorToolBox(this.root, this);
        toolBoxPane.setContent(colorToolBox);

        toolBoxPane.setCollapsible(false);
        toolBoxPane.setAnimated(false);

        this.root.getChildren().add(toolBoxPane);

        // Add it here so it appears on top of the tool box
        final AbstractGazeIndicator progressIndicator = colorToolBox.getProgressIndicator();
        root.getChildren().add(progressIndicator);
        progressIndicator.toFront();

        updateToolBox();

        toolBoxPane.maxHeightProperty().bind(gameContext.getRoot().heightProperty());
        toolBoxPane.prefHeightProperty().bind(gameContext.getRoot().heightProperty());
    }

    private void buildDraw(String imgURL, double width, double height) {

        rectangle = new Rectangle(width, height);

        // When size will be calculated, update size of rectangle, but do this only
        // once
        ChangeListener listener = (ChangeListener) new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                updateRectangle();
                updateToolBox();
                colorToolBox.widthProperty().removeListener(this);
            }
        };

        colorToolBox.widthProperty().addListener(listener);

        Image img = new Image(imgURL);

        if (!img.isError()) {

            javaFXEditing(img, imgURL);
        }

        root.getChildren().add(rectangle);

        rectangle.toBack();
    }

    private void javaFXEditing(Image image, final String imageName) {

        this.updateImage(image, imageName);

        final Stage stage = GazePlay.getInstance().getPrimaryStage();

        // Resizing is working but not immediatly
        root.widthProperty().addListener((observable) -> {

            updateRectangle();
            updateToolBox();

        });

        root.heightProperty().addListener((observable) -> {

            updateRectangle();
            updateToolBox();

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

        // rectangle.setTranslateX(colorToolBox.getWidth());

        rectangle.setWidth(width);
        rectangle.setHeight(height);

        rectangle.setFill(new ImagePattern(writableImg));
    }

    private void updateToolBox() {

        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double width = dimension2D.getWidth();

        double ToolBoxWidth = toolBoxPane.getWidth();
        double x = width - ToolBoxWidth;
        log.debug("translated tool box to : {}, x toolBoxWidth : {}", x, ToolBoxWidth);
        toolBoxPane.setTranslateX(x);
    }

    /**
     * Change the current image to display and update everything so it can be colorized.
     * 
     * @param image
     *            The new image to colorize.
     * @param imageName
     *            The name of the image
     */
    public void updateImage(final Image image, final String imageName) {

        final PixelReader tmpPixelReader = image.getPixelReader();

        if (tmpPixelReader == null) {
            log.error("Error in image loading : ");
            log.error("Colors : unable to read pixels from image");
            return;
        }

        rectangle.setFill(new ImagePattern(image));

        writableImg = new WritableImage(tmpPixelReader, (int) image.getWidth(), (int) image.getHeight());
        pixelWriter = writableImg.getPixelWriter();
        pixelReader = writableImg.getPixelReader();

        final Translator translator = GazePlay.getInstance().getTranslator();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(GazePlay.getInstance().getPrimaryStage());
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initStyle(StageStyle.UTILITY);
        alert.setContentText(translator.translate("confirmBWText") + imageName);
        alert.setTitle(translator.translate("confirmBWTitle"));
        alert.setHeaderText(translator.translate("confirmBWHeader"));

        // Make sure the alert is on top
        alert.initOwner(GazePlay.getInstance().getPrimaryStage());
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");

        alert.getButtonTypes().setAll(yesButton, noButton);

        stage.toFront();
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == yesButton) {
            toBlackAndWhite();
        }
    }

    private void toBlackAndWhite() {

        for (int i = 0; i < writableImg.getWidth(); ++i) {
            for (int j = 0; j < writableImg.getHeight(); ++j) {

                Color pixCol = pixelReader.getColor(i, j);
                double avg = (pixCol.getRed() + pixCol.getGreen() + pixCol.getBlue()) / 3;

                Color newCol = avg > AVG_THRESHOLD ? Color.WHITE : Color.BLACK;
                pixelWriter.setColor(i, j, newCol);
            }
        }

        updateRectangle();

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

                GazePlay gazePlay = GazePlay.getInstance();

                double gameWidth = gazePlay.getPrimaryStage().getWidth();
                double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
                double widthDiff = 0;/* screenWidth - gameWidth; */

                if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    MouseEvent mouseEvent = (MouseEvent) event;
                    // log.info("clicked at x= {}, y = {}", currentX, currentY);
                    colorize(currentX, currentY);

                } else if (event.getEventType() == GazeEvent.GAZE_ENTERED) {

                    GazeEvent gazeEvent = (GazeEvent) event;

                    // log.info("Gaze event : {}", gazeEvent);

                    currentX = gazeEvent.getX();
                    currentY = gazeEvent.getY();

                    Point2D eventCoord = new Point2D(currentX, currentY);
                    Point2D localCoord = root.screenToLocal(eventCoord);

                    if (localCoord != null) {
                        currentX = localCoord.getX();
                        currentY = localCoord.getY();
                    }

                    gazeXOrigin = currentX;
                    gazeYOrigin = currentY;

                    gazeProgressIndicator.setOnFinish((ActionEvent event1) -> {

                        colorize(currentX, currentY);
                    });

                    gazeProgressIndicator.start();
                } else if (event.getEventType() == GazeEvent.GAZE_MOVED) {

                    GazeEvent gazeEvent = (GazeEvent) event;

                    currentX = gazeEvent.getX();
                    currentY = gazeEvent.getY();

                    Point2D eventCoord = new Point2D(currentX, currentY);
                    Point2D localCoord = root.screenToLocal(eventCoord);

                    if (localCoord != null) {
                        currentX = localCoord.getX();
                        currentY = localCoord.getY();
                    }

                    // If gaze still around first point
                    if (gazeXOrigin - GAZE_MOVING_THRESHOLD < currentX && gazeXOrigin + GAZE_MOVING_THRESHOLD > currentX
                            && gazeYOrigin - GAZE_MOVING_THRESHOLD < currentY
                            && gazeYOrigin + GAZE_MOVING_THRESHOLD > currentY) {

                        // Do nothing
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

                    gazeProgressIndicator.setOnFinish((ActionEvent event1) -> {

                        colorize(currentX, currentY);
                    });

                    gazeProgressIndicator.start();
                } else if (event.getEventType() == MouseEvent.MOUSE_MOVED) {

                    MouseEvent mouseEvent = (MouseEvent) event;
                    currentX = mouseEvent.getX();
                    currentY = mouseEvent.getY();

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

        /*
         * Don't fill the zone if the pixel selected is already of the same color. Also don't fill black zones
         */
        if (!isEqualColors(color, colorToolBox.getSelectedColorBox().getColor()) && !isEqualColors(color, Color.BLACK)
                && drawingEnable.getValue()) {
            javaFXFloodFill(pixelWriter, pixelReader, colorToolBox.getSelectedColorBox().getColor(), pixelX, pixelY,
                    (int) writableImg.getWidth(), (int) writableImg.getHeight());
            rectangle.setFill(new ImagePattern(writableImg));
            rectangle.toBack();

            stats.incNbGoals();
        }
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

        // Scalar distance calculation
        double dist = Math.sqrt(
                Math.pow(color1.getRed() - color2.getRed(), 2) + Math.pow(color1.getGreen() - color2.getGreen(), 2)
                        + Math.pow(color1.getBlue() - color2.getBlue(), 2));

        return dist <= COLOR_EQUALITY_THRESHOLD;
    }

    public void setEnableColorization(boolean enable) {

        this.drawingEnable.setValue(enable);
    }
}
