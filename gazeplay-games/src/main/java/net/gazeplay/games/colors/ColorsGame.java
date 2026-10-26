package net.gazeplay.games.colors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.components.GazeIndicator;
import net.gazeplay.components.GazeFollowerIndicator;
import net.gazeplay.components.ProgressButton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Game where you select a color in order to colorize a white and black draw.
 *
 * @author Thomas MEDARD
 */
@Slf4j
public class ColorsGame implements GameLifeCycle {


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
     * The game context provided.
     */
    @Getter
    private final IGameContext gameContext;

    /**
     * The root where to draw everything
     */
    private final Pane root;

    /**
     * Should we enableColorization.
     */
    @Getter
    private final BooleanProperty drawingEnable = new SimpleBooleanProperty(this, "isDrawingEnable", true);

    private final ColorsGamesStats stats;

    private final Deque<HorizontalZone> horiZones = new ArrayDeque<>();

    /**
     * The tool box object.
     */
    private ColorToolBox colorToolBox;

    /**
     * The gaze progress indicator to show time before colorization.
     */
    private GazeIndicator gazeProgressIndicator;

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
     * The colorization event handler
     */
    private CustomEventHandler colorizationEventHandler;
    private String imageToColor [] ={"draw-flower.png",
        "draw-cupcake.png",
        "draw-dolphin.png",
        "draw-apple.png",
        "draw-home.png",
        "draw-leaf.png",
        "draw-sun.png",
        "draw-turtle.png",
        "draw-nature.png",
        "draw-butterfly.png",
        "draw-cake.png",
        "draw-rabbit.png",
        "draw-aqua.png",
        "draw-grape.jpg",
        "draw-manuel.png",
        "draw-sunmoon.jpg",
        "draw-hellokitty.jpg"};
    private int index;
    private boolean defaultImg;
    private final ProgressButton nextButton;
    private final ProgressButton prevButton;

    ColorsGame(final IGameContext gameContext, final ColorsGamesStats stats, final Translator translator) {
        this.gameContext = gameContext;
        this.stats = stats;
        index = 0;

        defaultImg=true;
        final Dimension2D dimensions=gameContext.getGamePanelDimensionProvider().getDimension2D();
        nextButton = new ProgressButton();

        final ImageView nextImage = new ImageView("data/colors/images/next.png");
        nextImage.setFitHeight(dimensions.getHeight() / 8);
        nextImage.setFitWidth(dimensions.getHeight() / 8);
        nextButton.setLayoutX(dimensions.getWidth() -4.5*nextImage.getFitWidth());
        nextButton.setLayoutY(0.2 * nextImage.getFitHeight());
        nextButton.setImage(nextImage);
        nextButton.assignIndicatorUpdatable(event -> {changeImageNext();launch();}, gameContext);
        this.gameContext.getGazeDeviceManager().addEventFilter(nextButton);
        nextButton.active();

        prevButton= new ProgressButton();

        final ImageView prevImage = new ImageView("data/colors/images/prev.png");
        prevImage.setFitHeight(dimensions.getHeight() / 8);
        prevImage.setFitWidth(dimensions.getHeight() / 8);
        prevButton.setLayoutX(0.6 *prevImage.getFitWidth());
        prevButton.setLayoutY( 0.2 * prevImage.getFitHeight());
        prevButton.setImage(prevImage);
        prevButton.assignIndicatorUpdatable(event -> {changeImagePrev();launch();}, gameContext);
        this.gameContext.getGazeDeviceManager().addEventFilter(prevButton);
        prevButton.active();




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

    /**
     * Detect if a color is close enough to another one to be considered the same.
     *
     * @param color1 The first color to compare
     * @param color2 The second color to compare
     * @return true if considered same, false otherwise.
     */
    private static boolean isEqualColors(final Color color1, final Color color2) {
        // Scalar distance calculation
        final double dist = Math.sqrt(
            Math.pow(color1.getRed() - color2.getRed(), 2) + Math.pow(color1.getGreen() - color2.getGreen(), 2)
                + Math.pow(color1.getBlue() - color2.getBlue(), 2));

        return dist <= COLOR_EQUALITY_THRESHOLD;
    }

    @Override
    public void launch() {

        gameContext.getChildren().add(nextButton);
        gameContext.getChildren().add(prevButton);
        this.gazeProgressIndicator = new GazeFollowerIndicator(gameContext, root, root);

        this.root.getChildren().add(gazeProgressIndicator);

        final javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final double width = dimension2D.getWidth();
        final double height = dimension2D.getHeight();


        buildToolBox(width, height);

        buildDraw(gameContext.getConfiguration().getColorsDefaultImage(), width, height);

        colorToolBox.getColorBoxes().forEach(ColorBox::updateHeight);

        gameContext.onGameStarted(3000);
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void dispose() {

    }

    /**
     * Adds the tool box beside the drawing.
     */
    private void buildToolBox(final double width, final double height) {

        this.colorToolBox = new ColorToolBox(this.root, this, gameContext, height/12);
        this.root.getChildren().add(colorToolBox);

        // Add it here so it appears on top of the tool box
        final GazeIndicator progressIndicator = colorToolBox.getProgressIndicator();
        colorToolBox.getChildren().add(progressIndicator);
        progressIndicator.toFront();


        updateToolBox();

        colorToolBox.maxHeightProperty().bind(gameContext.getRoot().heightProperty());
        colorToolBox.prefHeightProperty().bind(gameContext.getRoot().heightProperty());
        colorToolBox.minHeightProperty().bind(gameContext.getRoot().heightProperty());
    }

    /**
     * Shows a drawing, turned into black and white, in the area left by the tool box.
     */
    private void buildDraw(final String imgURL, final double width, final double height) {

        rectangle = new Rectangle(width, height);

        // When size will be calculated, update size of rectangle, but do this only
        // once
        final ChangeListener<Number> listener = new ChangeListener<>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
                updateRectangle();
                updateToolBox();
                colorToolBox.widthProperty().removeListener(this);
            }
        };

        colorToolBox.widthProperty().addListener(listener);
        final Image img = getDrawingImage(imgURL);


        if (!img.isError()) {

            javaFXEditing(img, imgURL);
        }

        root.getChildren().add(rectangle);

        rectangle.toBack();
    }

    /**
     * Shows the next drawing of the "colors" folder.
     */
    private void changeImageNext ()
    {
        String imgURL;

        this.index++;
        if(index>imageToColor.length-1){
            index=0;
        }
        imgURL = imageToColor[index];

        final Image img = getDrawingImage(imgURL);


        if (!img.isError()) {

            javaFXEditing(img,imgURL);
        }
    }

    /**
     * Shows the previous drawing of the "colors" folder.
     */
    private void changeImagePrev ()
    {
        String imgURL;

        this.index--;
        if(index<0){
            index=imageToColor.length-1;
        }
        imgURL = imageToColor[index];

        final Image img = getDrawingImage(imgURL);


        if (!img.isError()) {

            javaFXEditing(img,imgURL);
        }
    }

    /**
     * @return the drawing of a file, or of a resource when it is not a file
     */
    private Image getDrawingImage(String imgURL) {
        Image img;
        try {
            img = new Image(Files.newInputStream(new File(imgURL).toPath()));
        } catch (final IOException e) {

            log.debug("Drawing image " + imgURL + " cannot be found");

            getGameContext().getConfiguration().getColorsDefaultImageProperty().set(Configuration.DEFAULT_VALUE_COLORS_DEFAULT_IMAGE);
            String defaultImgURL;
            if(defaultImg) {
                 defaultImgURL= Configuration.DEFAULT_VALUE_COLORS_DEFAULT_IMAGE;;
                defaultImg=false;
            }
            else {defaultImgURL ="data/colors/images/" + imageToColor[index];}
            img = new Image(defaultImgURL);
        }
        return img;
    }

    /**
     * Makes a drawing editable and turns it into black and white.
     */
    private void javaFXEditing(final Image image, final String imageName) {

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

    /**
     * Resizes the drawing for the game area.
     */
    private void updateRectangle() {

        final javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final double width = dimension2D.getWidth() - colorToolBox.getWidth();
        final double height = dimension2D.getHeight();

        rectangle.setWidth(width);
        rectangle.setHeight(height);

        rectangle.setFill(this.createImagePattern(writableImg, rectangle));

        final double ratio = writableImg.getHeight() / writableImg.getWidth();
        rectangle.setWidth(rectangle.getHeight() / ratio);

        rectangle.setX((width - rectangle.getHeight() / ratio) / 2);

    }

    /**
     * Places the tool box for the game area.
     */
    private void updateToolBox() {

        final javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final double width = dimension2D.getWidth();

        final double toolBoxWidth = colorToolBox.getWidth();
        final double x = width - toolBoxWidth;
        log.debug("translated tool box to : {}, x toolBoxWidth : {}", x, toolBoxWidth);
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

        rectangle.setFill(this.createImagePattern(image, rectangle));

        writableImg = new WritableImage(tmpPixelReader, (int) image.getWidth(), (int) image.getHeight());
        pixelWriter = writableImg.getPixelWriter();
        pixelReader = writableImg.getPixelReader();

        toBlackAndWhite();
    }

    /**
     * @return the average gray level of the drawing, used as threshold of the black and white conversion
     */
    private double getAvgThresholdValue() {
        double sum = 0;
        for (int i = 0; i < writableImg.getWidth(); ++i) {
            for (int j = 0; j < writableImg.getHeight(); ++j) {
                final Color pixCol = pixelReader.getColor(i, j);
                sum = sum + (pixCol.getRed() + pixCol.getGreen() + pixCol.getBlue()) / 3;
            }
        }
        sum = sum / (writableImg.getWidth() * writableImg.getHeight());
        return 3 * sum / 4;
    }

    /**
     * Turns the drawing into black and white: darker pixels than the average become black, the others white.
     */
    private void toBlackAndWhite() {


        final double avgThresholdValue = getAvgThresholdValue();

        for (int i = 0; i < writableImg.getWidth(); ++i) {
            for (int j = 0; j < writableImg.getHeight(); ++j) {

                final Color pixCol = pixelReader.getColor(i, j);
                final double avg = (pixCol.getRed() + pixCol.getGreen() + pixCol.getBlue()) / 3;

                final Color newCol = avg > avgThresholdValue ? Color.WHITE : Color.BLACK;
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

        final int pixelX = (int) ((x - rectangle.getX()) * writableImg.getWidth() / rectangle.getWidth());
        final int pixelY = (int) ((y - rectangle.getY()) * writableImg.getHeight() / rectangle.getHeight());

        final Color color = pixelReader.getColor(pixelX, pixelY);

        /*
         * Don't fill the zone if the pixel selected is already of the same color. Also don't fill black zones
         */
        if (!isEqualColors(color, colorToolBox.getSelectedColorBox().getColor()) && !isEqualColors(color, Color.BLACK)
            && drawingEnable.getValue()) {
            javaFXFloodFill(pixelWriter, pixelReader, colorToolBox.getSelectedColorBox().getColor(), pixelX, pixelY,
                (int) writableImg.getWidth(), (int) writableImg.getHeight());
            rectangle.setFill(this.createImagePattern(writableImg, rectangle));
            rectangle.toBack();

            stats.incrementNumberOfGoalsReached();
        }
    }

    /**
     * Fills with a color the zone of the pixel at (x, y): its neighbours of the same color, up to other colors.
     */
    private void javaFXFloodFill(final PixelWriter pixelWriter, final PixelReader pixelReader, final Color newColor, final int x,
                                 final int y, final int width, final int height) {

        final Color oldColor = pixelReader.getColor(x, y);

        floodInColumnAndLine(pixelWriter, pixelReader, newColor, x, y, width, height, oldColor);
    }

    /**
     * @return a pattern filling a rectangle with an image
     */
    public ImagePattern createImagePattern(final Image img, final Rectangle r) {
        return new ImagePattern(img, 0, 0, 1, 1, true);
    }

    /**
     * Fills the line of a pixel to the left and right, then searches the zones above and below to fill (scan line flood
     * fill, without recursion).
     */
    private void floodInColumnAndLine(final PixelWriter pixelWriter, final PixelReader pixelReader,
                                      final Color newColor, final int x, final int y, final int width, final int height, final Color oldColor) {

        final int leftX = floodInLine(pixelWriter, pixelReader, newColor, x, y, width, true, oldColor);
        final int rightX = floodInLine(pixelWriter, pixelReader, newColor, x, y, width, false, oldColor);

        final HorizontalZone firstZone = new HorizontalZone(leftX, rightX, y);
        searchZone(firstZone, oldColor, pixelReader, pixelWriter, newColor, width, height);

        while (horiZones.size() > 0) {

            final HorizontalZone zone = horiZones.pop();
            searchZone(zone, oldColor, pixelReader, pixelWriter, newColor, width, height);
        }
    }

    /**
     * Searches the horizontal zones of the old color above and below a filled zone, and fills them.
     */
    private void searchZone(final HorizontalZone zone, final Color oldColor, final PixelReader pixelReader,
                            final PixelWriter pixelWriter, final Color newColor, final int width, final int height) {

        // Search for left and right of the zone
        final int leftX = floodInLine(pixelWriter, pixelReader, newColor, zone.leftX, zone.y, width, true, oldColor);
        final int rightX = floodInLine(pixelWriter, pixelReader, newColor, zone.rightX, zone.y, width, false, oldColor);

        for (int i = leftX; i <= rightX; ++i) {

            // Search for available zone to colorize upward
            if (zone.y > 0 && isEqualColors(pixelReader.getColor(i, zone.y - 1), oldColor)) {

                final int newLeftX = floodInLine(pixelWriter, pixelReader, newColor, i, zone.y - 1, width, true, oldColor);
                final int newRightX = floodInLine(pixelWriter, pixelReader, newColor, i, zone.y - 1, width, false, oldColor);

                horiZones.add(new HorizontalZone(newLeftX, newRightX, zone.y - 1));
            }
            // Search for available zone to colorize downward
            if (zone.y < height - 1 && isEqualColors(pixelReader.getColor(i, zone.y + 1), oldColor)) {

                final int newLeftX = floodInLine(pixelWriter, pixelReader, newColor, i, zone.y + 1, width, true, oldColor);
                final int newRightX = floodInLine(pixelWriter, pixelReader, newColor, i, zone.y + 1, width, false, oldColor);

                horiZones.add(new HorizontalZone(newLeftX, newRightX, zone.y + 1));
            }
        }
    }

    /**
     * Fills a line from a pixel to the left or the right, up to another color.
     *
     * @return the last x filled
     */
    private int floodInLine(final PixelWriter pixelWriter, final PixelReader pixelReader, final Color newColor,
                            final int x, final int y, final int width, final boolean isLeftFIll, final Color oldColor) {

        int currentX = x;

        // fill
        do {

            pixelWriter.setColor(currentX, y, newColor);

            if (isLeftFIll) {
                currentX--;
            } else {
                currentX++;
            }
        } while (currentX >= 0 && currentX < width - 1 && isEqualColors(pixelReader.getColor(currentX, y), oldColor));

        if (isLeftFIll) {
            currentX++;
        } else {

            currentX--;
        }

        return currentX;
    }

    /**
     * Enables or disables the coloring by the gaze.
     */
    void setEnableColorization(final boolean enable) {
        this.drawingEnable.setValue(enable);
    }

    /**
     * Objects used internally that represents a horizontal line of pixels
     */
    private static class HorizontalZone {
        final int leftX;
        final int rightX;
        final int y;

        /**
         * A horizontal zone of pixels, from {@code lX} to {@code rX} on line {@code y}, waiting to be searched.
         */
        HorizontalZone(final int lX, final int rX, final int y) {
            this.leftX = lX;
            this.rightX = rX;
            this.y = y;
        }

        @Override
        public String toString() {
            return "HorizontalZone{" + "leftX=" + leftX + ", rightX=" + rightX + ", y=" + y + '}';
        }
    }

    /**
     * Colors the zone of the drawing where the gaze stays during the fixation length of the settings (the mouse colors
     * at a click).
     */
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

        private void onMouseExited(final MouseEvent event) {
            gazeProgressIndicator.stop();
        }

        private void onMouseMoved(final MouseEvent event) {
            currentX = event.getX();
            currentY = event.getY();

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

        private void onMouseEntered(final MouseEvent event) {
            gazeXOrigin = event.getX();
            gazeYOrigin = event.getY();
            currentX = gazeXOrigin;
            currentY = gazeYOrigin;

            gazeProgressIndicator.setOnFinish((ActionEvent event1) -> colorize(currentX, currentY));

            gazeProgressIndicator.start();
        }

        private void onGazeExited(final GazeEvent event) {
            gazeProgressIndicator.stop();
        }

        private void onGazeMove(final GazeEvent event) {
            currentX = event.getX();
            currentY = event.getY();

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

        private void onGazeEntered(final GazeEvent event) {
            currentX = event.getX();
            currentY = event.getY();

            gazeXOrigin = currentX;
            gazeYOrigin = currentY;

            gazeProgressIndicator.setOnFinish((ActionEvent event1) -> colorize(currentX, currentY));

            gazeProgressIndicator.start();
        }

        /**
         * Colors the zone at the position of the mouse.
         */
        private void onMouseClicked(final MouseEvent event) {
            colorize(currentX, currentY);
        }

    }
}
