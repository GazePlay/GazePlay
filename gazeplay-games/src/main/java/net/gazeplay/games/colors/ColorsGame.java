package net.gazeplay.games.colors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.Translator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    private final IGameContext gameContext;

    /**
     * The root where to draw everything
     */
    private final Pane root;

    /**
     * The tool box object.
     */
    private ColorToolBox colorToolBox;

    /**
     * On a [0, 1] scale, used to determine the threshold in the difference between two colors to consider that they are
     * equals.
     */
    private static final double COLOR_EQUALITY_THRESHOLD = 10d / 255d;

    /**
     * Distance in pixel between two gaze event to consider that the gaze is moving.
     */
    private static final double GAZE_MOVING_THRESHOLD = 25;

    /**
     * Distance in pixel of the current cursor or gaze position in x and y for the gaze indicator.
     */
    public static final double GAZE_INDICATOR_DISTANCE = 5;

    private static double AVG_THRESHOLD = 0.39;

    /**
     * The gaze progress indicator to show time before colorization.
     */
    private AbstractGazeIndicator gazeProgressIndicator;

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
    private CustomEventHandler colorizationEventHandler;

    private final ColorsGamesStats stats;

    private final Translator translator;

    ColorsGame(IGameContext gameContext, final ColorsGamesStats stats, final Translator translator) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.translator = translator;

        root = gameContext.getRoot();

        drawingEnable.addListener((observable, oldValue, newValue) -> {
            // If we want to stop colorization
            if (!newValue) {
                // Hide away the gazeProgressIndicator
                gazeProgressIndicator.stop();

                // Stop registering colorization events
                rectangle.removeEventFilter(MouseEvent.ANY, colorizationEventHandler.mouseEventEventHandler);
                rectangle.removeEventFilter(GazeEvent.ANY, colorizationEventHandler.gazeEventEventHandler);

            } else {
                rectangle.addEventFilter(MouseEvent.ANY, colorizationEventHandler.mouseEventEventHandler);
                rectangle.addEventFilter(GazeEvent.ANY, colorizationEventHandler.gazeEventEventHandler);
            }
        });
    }

    @Override
    public void launch() {

        this.gazeProgressIndicator = new GazeFollowerIndicator(gameContext, root);

        this.root.getChildren().add(gazeProgressIndicator);

        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double width = dimension2D.getWidth();
        double height = dimension2D.getHeight();


        buildToolBox(width, height);

        // log.info("Toolbox width = {}, height = {}", colorToolBox.getWidth(), colorToolBox.getHeight());
        buildDraw(gameContext.getConfiguration().getColorsDefaultImageProperty().getValue(), width, height);

        colorToolBox.getColorBoxes().forEach(ColorBox::updateHeight);

        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {

    }

    private void buildToolBox(double width, double height) {

        this.colorToolBox = new ColorToolBox(this.root, this, gameContext);
        this.root.getChildren().add(colorToolBox);

        // Add it here so it appears on top of the tool box
        final AbstractGazeIndicator progressIndicator = colorToolBox.getProgressIndicator();
        root.getChildren().add(progressIndicator);
        progressIndicator.toFront();



        updateToolBox();

        colorToolBox.maxHeightProperty().bind(gameContext.getRoot().heightProperty());
        colorToolBox.prefHeightProperty().bind(gameContext.getRoot().heightProperty());
        colorToolBox.minHeightProperty().bind(gameContext.getRoot().heightProperty());
    }

    private void buildDraw(String imgURL, double width, double height) {

        rectangle = new Rectangle(width, height);

        // When size will be calculated, update size of rectangle, but do this only
        // once
        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                updateRectangle();
                updateToolBox();
                colorToolBox.widthProperty().removeListener(this);
            }
        };

        colorToolBox.widthProperty().addListener(listener);
        Image img;
try {
    log.info(imgURL);
     img = new Image(new FileInputStream(imgURL));
} catch(FileNotFoundException e ){
    getGameContext().getConfiguration().getColorsDefaultImageProperty().set(Configuration.DEFAULT_VALUE_COLORS_DEFAULT_IMAGE);
    getGameContext().getConfiguration().saveConfigIgnoringExceptions();

    imgURL = Configuration.DEFAULT_VALUE_COLORS_DEFAULT_IMAGE;
    img = new Image(imgURL);
}

        if (!img.isError()) {

            javaFXEditing(img, imgURL);
        }

        root.getChildren().add(rectangle);

        rectangle.toBack();
    }

    private void javaFXEditing(Image image, final String imageName) {

        this.updateImage(image, imageName);

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

        colorizationEventHandler = new CustomEventHandler();

        rectangle.addEventFilter(MouseEvent.ANY, colorizationEventHandler.mouseEventEventHandler);
        rectangle.addEventFilter(GazeEvent.ANY, colorizationEventHandler.gazeEventEventHandler);

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

        double ToolBoxWidth = colorToolBox.getWidth();
        double x = width - ToolBoxWidth;
        log.debug("translated tool box to : {}, x toolBoxWidth : {}", x, ToolBoxWidth);
        colorToolBox.setTranslateX(x);
    }

    /**
     * Change the current image to display and update everything so it can be colorized.
     *
     * @param image     The new image to colorize.
     * @param imageName The name of the image
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


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(gameContext.getPrimaryStage());
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initStyle(StageStyle.UTILITY);
        alert.setContentText(translator.translate("confirmBWText") + imageName);
        alert.setTitle(translator.translate("confirmBWTitle"));
        alert.setHeaderText(translator.translate("confirmBWHeader"));

        // Make sure the alert is on top
        alert.initOwner(gameContext.getPrimaryStage());
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
        double sum = 0;
        for (int i = 0; i < writableImg.getWidth(); ++i) {
            for (int j = 0; j < writableImg.getHeight(); ++j) {

                Color pixCol = pixelReader.getColor(i, j);
                sum = sum + (pixCol.getRed() + pixCol.getGreen() + pixCol.getBlue()) / 3;
            }
        }

        sum = sum / (writableImg.getWidth() * writableImg.getHeight());

        AVG_THRESHOLD = sum / 2;

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

    /**
     * Fill a zone with the current selected color.
     *
     * @param x The x coordinates of the point to fill from.
     * @param y The y coordinates of the point to fill from.
     */
    private void colorize(final double x, final double y) {

        int pixelX = (int) (x * writableImg.getWidth() / rectangle.getWidth());
        int pixelY = (int) (y * writableImg.getHeight() / rectangle.getHeight());
        // log.info("pixel at x= {}, y = {}", pixelX, pixelY);

        Color color = pixelReader.getColor(pixelX, pixelY);
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
    private static class HorizontalZone {
        int leftX;
        int rightX;
        int y;

        HorizontalZone(int lX, int rX, int y) {
            this.leftX = lX;
            this.rightX = rX;
            this.y = y;
        }

        @Override
        public String toString() {
            return "HorizontalZone{" + "leftX=" + leftX + ", rightX=" + rightX + ", y=" + y + '}';
        }
    }

    private final Deque<HorizontalZone> horiZones = new ArrayDeque<>();

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
     * @param color1 The first color to compare
     * @param color2 The second color to compare
     * @return true if considered same, false otherwise.
     */
    private static boolean isEqualColors(final Color color1, final Color color2) {
        // Scalar distance calculation
        double dist = Math.sqrt(
            Math.pow(color1.getRed() - color2.getRed(), 2) + Math.pow(color1.getGreen() - color2.getGreen(), 2)
                + Math.pow(color1.getBlue() - color2.getBlue(), 2));

        return dist <= COLOR_EQUALITY_THRESHOLD;
    }

    void setEnableColorization(boolean enable) {
        this.drawingEnable.setValue(enable);
    }

    private class CustomEventHandler {

        private Double gazeXOrigin = 0.;
        private Double gazeYOrigin = 0.;

        private Double currentX = 0.;
        private Double currentY = 0.;
        
        @Getter
        private final EventHandler<MouseEvent> mouseEventEventHandler = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                onMouseClicked(event);
            } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                onMouseEntered(event);
            } else if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
                onMouseMoved(event);
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
                onMouseExited(event);
            }
        };

        @Getter
        private final EventHandler<GazeEvent> gazeEventEventHandler = event -> {
            if (event.getEventType() == GazeEvent.GAZE_ENTERED) {
                onGazeEntered(event);
            } else if (event.getEventType() == GazeEvent.GAZE_MOVED) {
                onGazeMove(event);
            } else if (event.getEventType() == GazeEvent.GAZE_EXITED) {
                onGazeExited(event);
            }
        };

        private void onMouseExited(MouseEvent event) {
            // log.info("mouse exited = ({},{})", currentX, currentY);
            gazeProgressIndicator.stop();
        }

        private void onMouseMoved(MouseEvent event) {
            MouseEvent mouseEvent = event;
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
                gazeProgressIndicator.setOnFinish((ActionEvent event1) -> colorize(currentX, currentY));

                gazeProgressIndicator.start();
            }
        }

        private void onMouseEntered(MouseEvent event) {
            gazeXOrigin = event.getX();
            gazeYOrigin = event.getY();
            currentX = gazeXOrigin;
            currentY = gazeYOrigin;

            gazeProgressIndicator.setOnFinish((ActionEvent event1) -> colorize(currentX, currentY));

            gazeProgressIndicator.start();
        }

        private void onGazeExited(GazeEvent event) {
            gazeProgressIndicator.stop();
        }

        private void onGazeMove(GazeEvent event) {
            currentX = event.getX();
            currentY = event.getY();

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
                gazeProgressIndicator.setOnFinish((ActionEvent event1) -> colorize(currentX, currentY));

                gazeProgressIndicator.start();
            }
        }

        private void onGazeEntered(GazeEvent event) {
            currentX = event.getX();
            currentY = event.getY();

            Point2D eventCoord = new Point2D(currentX, currentY);
            Point2D localCoord = root.screenToLocal(eventCoord);

            if (localCoord != null) {
                currentX = localCoord.getX();
                currentY = localCoord.getY();
            }

            gazeXOrigin = currentX;
            gazeYOrigin = currentY;

            gazeProgressIndicator.setOnFinish((ActionEvent event1) -> colorize(currentX, currentY));

            gazeProgressIndicator.start();
        }

        private void onMouseClicked(MouseEvent event) {
            colorize(currentX, currentY);
        }

    }
}
