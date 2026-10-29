package net.gazeplay.games.colors;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.ResourceFileManager;
import net.gazeplay.components.GameStyle;
import net.gazeplay.components.GazeRoundButton;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;
import java.util.Deque;
import java.util.List;

/**
 * Game where you select a color in order to colorize a white and black draw: a fixation on a zone of the drawing (or a
 * click) fills it with the chosen color, one goal counted per filled zone. The drawings, browsed with the previous and
 * next buttons, are those of the variant: the coloring pages of a level (data/colors/images/&lt;level&gt;/draw-*.png), or
 * the images given by the user in the settings (custom palette, images/Coloring of the files folder).
 * <p>
 * The pixels are kept in an array (ARGB): the drawing turned into black and white, the fillings, and the copies kept to
 * undo them.
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
     * The drawings are loaded at most at this size: a large file (e.g. 6000 x 7500 pixels) used to make the game slow.
     */
    static final double MAX_DRAWING_SIZE = 1600;

    /**
     * Number of fillings that can be undone.
     */
    static final int UNDO_LEVELS = 10;

    static final String DRAWINGS_FOLDER = "data/colors/images";

    private static final int BLACK = 0xFF000000;

    private static final int WHITE = 0xFFFFFFFF;

    @Getter
    private final IGameContext gameContext;

    private final Pane root;

    /**
     * Whether a fixation on the drawing colors it.
     */
    @Getter
    private final BooleanProperty drawingEnable = new SimpleBooleanProperty(this, "isDrawingEnable", true);

    private final ColorsGamesStats stats;

    private final Translator translator;

    @Getter
    private final ColoringVariant variant;

    /**
     * The drawings of the variant, in the order of their names: resources of the coloring pages of the level, or URLs of
     * the files of the user.
     */
    @Getter
    private List<String> drawings;

    @Getter
    private int index;

    @Getter
    private WritableImage writableImg;

    private int imageWidth;

    private int imageHeight;

    private int[] pixels;

    private int[] blackAndWhite;

    private final Deque<int[]> undoStack = new ArrayDeque<>();

    private ColorToolBox colorToolBox;

    private GazeRoundButton previousButton;

    private GazeRoundButton nextButton;

    /**
     * The number of the drawing, on a rounded label between the previous and next buttons.
     */
    private Group counter;

    /**
     * Shown instead of a drawing when the custom palette has no image (game started without the menu, which asks
     * for images first).
     */
    private Group noDrawingMessage;

    private double counterCenterX;

    private double counterCenterY;

    private double counterFontSize;

    private Rectangle card;

    private Rectangle drawing;

    private FixationRing fixationRing;

    private final InvalidationListener resizeListener = observable -> layout();

    private double gazeXOrigin;
    private double gazeYOrigin;
    private double currentX;
    private double currentY;

    /**
     * Whether a fixation has filled a zone, and the gaze has not moved since: no other filling until it moves.
     */
    private boolean filledAtThisPoint;

    ColorsGame(final IGameContext gameContext, final ColorsGamesStats stats, final Translator translator,
               final ColoringVariant variant) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.translator = translator;
        this.variant = variant;
        this.root = gameContext.getRoot();
        this.drawings = findDrawings();

        drawingEnable.addListener((observable, oldValue, newValue) -> {
            if (!newValue && fixationRing != null) {
                fixationRing.stop();
            }
        });
    }

    @Override
    public void launch() {
        buildScreen();
        showFirstDrawing();

        root.widthProperty().addListener(resizeListener);
        root.heightProperty().addListener(resizeListener);

        gameContext.onGameStarted(3000);
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void dispose() {
        root.widthProperty().removeListener(resizeListener);
        root.heightProperty().removeListener(resizeListener);
        if (colorToolBox != null) {
            colorToolBox.dispose();
        }
        if (previousButton != null) {
            previousButton.dispose();
            nextButton.dispose();
        }
        if (drawing != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(drawing);
        }
    }

    /**
     * Creates the nodes of the screen once: the card of the drawing, the drawing, the previous and next buttons with the
     * number of the drawing, the tool box and the ring following the gaze on the drawing.
     */
    private void buildScreen() {
        card = new Rectangle();
        card.setStyle("-fx-fill: white; -fx-stroke: -gp-border; -fx-stroke-width: 2;");

        drawing = new Rectangle();
        // White until a drawing is shown (a rectangle is black by default)
        drawing.setFill(javafx.scene.paint.Color.WHITE);
        gameContext.getGazeDeviceManager().addEventFilter(drawing);
        drawing.addEventFilter(MouseEvent.ANY, this::onDrawingEvent);
        drawing.addEventFilter(GazeEvent.ANY, this::onDrawingEvent);

        counter = new Group();
        noDrawingMessage = new Group();
        fixationRing = new FixationRing();

        root.getChildren().addAll(card, drawing, noDrawingMessage, counter, fixationRing);
        layout();
    }

    /**
     * Places the nodes for the size of the game area; the tool box and the buttons are built again for it.
     */
    private void layout() {
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double width = dimension.getWidth();
        final double height = dimension.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        final double gap = Math.max(10, Math.min(width, height) * 0.015);

        // The tool box, on the right
        final double toolBoxWidth = Math.max(220, width * 0.2);
        final ColorToolBox previousToolBox = colorToolBox;
        colorToolBox = new ColorToolBox(gameContext, this, toolBoxWidth, height - 2 * gap);
        colorToolBox.setLayoutX(width - toolBoxWidth - gap);
        colorToolBox.setLayoutY(gap);
        if (previousToolBox != null) {
            colorToolBox.select(colorToolBox.getColorBoxes().get(
                Math.max(0, previousToolBox.getColorBoxes().indexOf(previousToolBox.getSelectedColorBox()))));
            colorToolBox.getCustomBox().setColor(previousToolBox.getCustomBox().getColor());
            previousToolBox.dispose();
            root.getChildren().remove(previousToolBox);
        }
        root.getChildren().add(colorToolBox);
        updateUndoButton();

        // The previous and next buttons, with the number of the drawing between them, above the drawing
        final double areaWidth = width - toolBoxWidth - 3 * gap;
        final double navigationHeight = height * 0.11;
        final double buttonSize = navigationHeight * 0.85;
        if (previousButton != null) {
            previousButton.dispose();
            nextButton.dispose();
            root.getChildren().removeAll(previousButton, nextButton);
        }
        previousButton = new GazeRoundButton(gameContext, buttonSize, "arrow_back", translator.translate("Previous"),
            () -> changeDrawing(-1));
        nextButton = new GazeRoundButton(gameContext, buttonSize, "arrow_forward", translator.translate("Next"),
            () -> changeDrawing(1));
        final double fontSize = navigationHeight * 0.32;
        final double counterWidth = fontSize * 5;
        final double rowWidth = 2 * buttonSize + counterWidth;
        final double rowX = gap + (areaWidth - rowWidth) / 2;
        previousButton.setLayoutX(rowX);
        nextButton.setLayoutX(rowX + buttonSize + counterWidth);
        previousButton.setLayoutY(gap);
        nextButton.setLayoutY(gap);
        counterCenterX = rowX + buttonSize + counterWidth / 2;
        counterCenterY = gap + buttonSize / 2;
        counterFontSize = fontSize;
        root.getChildren().addAll(previousButton, nextButton);
        updateCounter();

        fixationRing.setSize(Math.max(50, height * 0.08));
        placeDrawing();
    }

    /**
     * Places the drawing, on a white card, below the buttons, as large as the area left by the tool box allows.
     */
    private void placeDrawing() {
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double width = dimension.getWidth();
        final double height = dimension.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        final double gap = Math.max(10, Math.min(width, height) * 0.015);
        final double toolBoxWidth = Math.max(220, width * 0.2);
        final double areaWidth = width - toolBoxWidth - 3 * gap;
        final double areaTop = gap + height * 0.11 + gap;
        final double areaHeight = height - areaTop - gap;
        final double ratio = imageWidth > 0 ? (double) imageHeight / imageWidth : 1;
        final double cardWidth = Math.min(areaWidth, areaHeight / ratio);
        final double cardHeight = cardWidth * ratio;
        card.setWidth(cardWidth);
        card.setHeight(cardHeight);
        card.setX(gap + (areaWidth - cardWidth) / 2);
        card.setY(areaTop + (areaHeight - cardHeight) / 2);
        card.setArcWidth(Math.min(cardWidth, cardHeight) * 0.06);
        card.setArcHeight(card.getArcWidth());
        final double padding = Math.min(cardWidth, cardHeight) * 0.03;
        drawing.setX(card.getX() + padding);
        drawing.setY(card.getY() + padding);
        drawing.setWidth(cardWidth - 2 * padding);
        drawing.setHeight(cardHeight - 2 * padding);
        if (!noDrawingMessage.getChildren().isEmpty()) {
            showNoDrawing();
        }
        fixationRing.toFront();
    }

    /**
     * @return the drawings of the variant, in the order of their names: the coloring pages of its level, or the images
     * given by the user in the settings (custom palette)
     */
    private List<String> findDrawings() {
        if (variant.getUserImagesFolder() != null) {
            return UserImagesVariant.images(variant.getUserImagesFolder()).stream()
                .map(file -> file.toURI().toString()).toList();
        }
        return ResourceFileManager.getResourcePaths(DRAWINGS_FOLDER + "/" + variant.getFolder()).stream()
            .filter(path -> new File(path).getName().startsWith("draw-"))
            .sorted()
            .toList();
    }

    /**
     * Shows the first drawing of the variant (it used to start on the drawing of the settings, the 7th one).
     */
    private void showFirstDrawing() {
        if (drawings.isEmpty()) {
            showNoDrawing();
        } else {
            showDrawing(0);
        }
    }

    /**
     * Shows, instead of a drawing, that the custom palette has no image yet.
     */
    private void showNoDrawing() {
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double fontSize = Math.max(16, dimension.getHeight() / 26);
        final String text = translator.translate("NoUserImages");
        Group message = GameStyle.pill(text, fontSize);
        final double maxWidth = card.getWidth() * 0.9;
        if (maxWidth > 0 && message.getLayoutBounds().getWidth() > maxWidth) {
            message = GameStyle.pill(text, fontSize * maxWidth / message.getLayoutBounds().getWidth());
        }
        message.setLayoutX(card.getX() + (card.getWidth() - message.getLayoutBounds().getWidth()) / 2);
        message.setLayoutY(card.getY() + (card.getHeight() - message.getLayoutBounds().getHeight()) / 2);
        noDrawingMessage.getChildren().setAll(message);
        updateCounter();
    }

    /**
     * Shows the previous (-1) or next (+1) drawing.
     */
    void changeDrawing(final int step) {
        if (!drawings.isEmpty()) {
            showDrawing(Math.floorMod(index + step, drawings.size()));
        }
    }

    /**
     * Shows a coloring page.
     */
    void showDrawing(final int drawingIndex) {
        if (drawings.isEmpty()) {
            return;
        }
        index = drawingIndex;
        setImage(loadImage(drawings.get(drawingIndex)));
    }

    /**
     * @param url a resource of the classpath or the URL of a file
     * @return the image, reduced to {@link #MAX_DRAWING_SIZE} if it is larger, never enlarged (JavaFX enlarges an image
     * to the size asked for)
     */
    static Image loadImage(final String url) {
        double largest = 0;
        final URL location = url.contains(":") ? toUrl(url) : ColorsGame.class.getClassLoader().getResource(url);
        if (location != null) {
            try (InputStream input = location.openStream(); ImageInputStream stream = ImageIO.createImageInputStream(input)) {
                final Iterator<ImageReader> readers = stream == null ? Collections.emptyIterator() : ImageIO.getImageReaders(stream);
                if (readers.hasNext()) {
                    final ImageReader reader = readers.next();
                    reader.setInput(stream);
                    largest = Math.max(reader.getWidth(0), reader.getHeight(0));
                    reader.dispose();
                }
            } catch (final IOException e) {
                log.debug("Cannot read the size of {}", url, e);
            }
        }
        final double size = largest > MAX_DRAWING_SIZE || largest == 0 ? MAX_DRAWING_SIZE : 0;
        return new Image(url, size, size, true, true);
    }

    private static URL toUrl(final String url) {
        try {
            return URI.create(url).toURL();
        } catch (final IllegalArgumentException | MalformedURLException e) {
            return null;
        }
    }

    /**
     * Makes an image the drawing to color, turned into black and white.
     */
    private void setImage(final Image image) {
        if (image.isError() || image.getPixelReader() == null) {
            log.error("Colors : unable to read pixels from image {}", image.getUrl());
            return;
        }
        imageWidth = (int) image.getWidth();
        imageHeight = (int) image.getHeight();
        pixels = new int[imageWidth * imageHeight];
        image.getPixelReader().getPixels(0, 0, imageWidth, imageHeight, PixelFormat.getIntArgbInstance(), pixels, 0,
            imageWidth);
        toBlackAndWhite(pixels);
        blackAndWhite = pixels.clone();
        undoStack.clear();
        writableImg = new WritableImage(imageWidth, imageHeight);
        showPixels();
        updateUndoButton();
        if (fixationRing != null) {
            fixationRing.stop();
        }
        if (counter != null) {
            updateCounter();
        }
        placeDrawing();
    }

    private void showPixels() {
        writableImg.getPixelWriter().setPixels(0, 0, imageWidth, imageHeight, PixelFormat.getIntArgbInstance(), pixels,
            0, imageWidth);
        drawing.setFill(new ImagePattern(writableImg, 0, 0, 1, 1, true));
    }

    private void updateCounter() {
        if (counterFontSize <= 0) {
            return;
        }
        final String text = drawings.isEmpty() ? "0 / 0" : (index + 1) + " / " + drawings.size();
        final Group pill = GameStyle.pill(text, counterFontSize);
        if (previousButton != null) {
            final boolean browsable = drawings.size() > 1;
            previousButton.setEnabled(browsable);
            nextButton.setEnabled(browsable);
        }
        pill.setLayoutX(counterCenterX - pill.getLayoutBounds().getWidth() / 2);
        pill.setLayoutY(counterCenterY - pill.getLayoutBounds().getHeight() / 2);
        counter.getChildren().setAll(pill);
    }

    private void updateUndoButton() {
        if (colorToolBox != null) {
            colorToolBox.getUndoButton().setEnabled(!undoStack.isEmpty());
        }
    }

    /**
     * Fills the zone of a point of the drawing (coordinates of the game area) with the chosen color, if it is not a line
     * and not already of this color; a goal is counted.
     */
    void colorize(final double x, final double y) {
        if (!drawingEnable.get() || pixels == null) {
            return;
        }
        final int pixelX = (int) ((x - drawing.getX()) * imageWidth / drawing.getWidth());
        final int pixelY = (int) ((y - drawing.getY()) * imageHeight / drawing.getHeight());
        final int color = toArgb(colorToolBox.getSelectedColorBox().getColor());
        if (fill(pixelX, pixelY, color)) {
            stats.incrementNumberOfGoalsReached();
        }
    }

    /**
     * Fills the zone of a pixel with a color, the previous pixels kept to undo it.
     *
     * @return whether a zone was filled (not a line, not already of this color, inside the drawing)
     */
    boolean fill(final int pixelX, final int pixelY, final int color) {
        if (pixelX < 0 || pixelY < 0 || pixelX >= imageWidth || pixelY >= imageHeight) {
            return false;
        }
        final int old = pixels[pixelY * imageWidth + pixelX];
        if (isEqualColors(old, color) || isEqualColors(old, BLACK)) {
            return false;
        }
        pushUndo();
        floodFill(pixels, imageWidth, imageHeight, pixelX, pixelY, color);
        showPixels();
        return true;
    }

    /**
     * Cancels the last filling (or the last start again).
     */
    void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        pixels = undoStack.pop();
        showPixels();
        updateUndoButton();
    }

    /**
     * Makes the drawing white again; it can be undone.
     */
    void restartDrawing() {
        if (blackAndWhite == null) {
            return;
        }
        pushUndo();
        pixels = blackAndWhite.clone();
        showPixels();
    }

    private void pushUndo() {
        undoStack.push(pixels.clone());
        while (undoStack.size() > UNDO_LEVELS) {
            undoStack.removeLast();
        }
        updateUndoButton();
    }

    /**
     * Enables or disables the coloring by the gaze.
     */
    void setEnableColorization(final boolean enable) {
        this.drawingEnable.setValue(enable);
    }

    /**
     * Colors the zone of the drawing where the gaze stays during the fixation length of the settings: a ring follows
     * the gaze and fills meanwhile; the mouse colors at once with a click.
     */
    private void onDrawingEvent(final Event event) {
        if (!drawingEnable.get()) {
            return;
        }
        final double x;
        final double y;
        if (event instanceof MouseEvent mouseEvent) {
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
                fixationRing.stop();
                colorize(mouseEvent.getX(), mouseEvent.getY());
                return;
            }
            x = mouseEvent.getX();
            y = mouseEvent.getY();
        } else if (event instanceof GazeEvent gazeEvent) {
            x = gazeEvent.getX();
            y = gazeEvent.getY();
        } else {
            return;
        }

        if (event.getEventType() == MouseEvent.MOUSE_EXITED || event.getEventType() == GazeEvent.GAZE_EXITED) {
            fixationRing.stop();
        } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
            startFixation(x, y);
        } else if (event.getEventType() == MouseEvent.MOUSE_MOVED || event.getEventType() == GazeEvent.GAZE_MOVED) {
            currentX = x;
            currentY = y;
            fixationRing.moveTo(x, y);
            if (Math.abs(x - gazeXOrigin) >= GAZE_MOVING_THRESHOLD || Math.abs(y - gazeYOrigin) >= GAZE_MOVING_THRESHOLD) {
                startFixation(x, y);
            } else if (!filledAtThisPoint && !fixationRing.isRunning()) {
                startFixation(x, y);
            }
        }
    }

    private void startFixation(final double x, final double y) {
        gazeXOrigin = x;
        gazeYOrigin = y;
        currentX = x;
        currentY = y;
        filledAtThisPoint = false;
        fixationRing.start(x, y, gameContext.getConfiguration().getFixationLength(), () -> {
            filledAtThisPoint = true;
            colorize(currentX, currentY);
        });
    }

    /**
     * Turns a drawing into black and white: pixels darker than three quarters of the average gray become black, the
     * others white (transparent pixels count as white).
     */
    static void toBlackAndWhite(final int[] argb) {
        double sum = 0;
        for (int i = 0; i < argb.length; i++) {
            argb[i] = onWhite(argb[i]);
            sum += gray(argb[i]);
        }
        final double threshold = 3 * (sum / argb.length) / 4;
        for (int i = 0; i < argb.length; i++) {
            argb[i] = gray(argb[i]) > threshold ? WHITE : BLACK;
        }
    }

    /**
     * Fills with a color the zone of a pixel: its neighbours of the same color, up to other colors (scan line flood
     * fill, without recursion), up to the borders of the drawing.
     */
    static void floodFill(final int[] argb, final int width, final int height, final int x, final int y,
                          final int color) {
        final int old = argb[y * width + x];
        final Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{x, y});
        while (!stack.isEmpty()) {
            final int[] point = stack.pop();
            final int py = point[1];
            int left = point[0];
            if (!isEqualColors(argb[py * width + left], old) || argb[py * width + left] == color) {
                continue;
            }
            while (left > 0 && isEqualColors(argb[py * width + left - 1], old)) {
                left--;
            }
            int right = point[0];
            while (right < width - 1 && isEqualColors(argb[py * width + right + 1], old)) {
                right++;
            }
            boolean aboveOpen = false;
            boolean belowOpen = false;
            for (int px = left; px <= right; px++) {
                argb[py * width + px] = color;
                if (py > 0) {
                    final boolean match = isEqualColors(argb[(py - 1) * width + px], old);
                    if (match && !aboveOpen) {
                        stack.push(new int[]{px, py - 1});
                    }
                    aboveOpen = match;
                }
                if (py < height - 1) {
                    final boolean match = isEqualColors(argb[(py + 1) * width + px], old);
                    if (match && !belowOpen) {
                        stack.push(new int[]{px, py + 1});
                    }
                    belowOpen = match;
                }
            }
        }
    }

    /**
     * @return whether two colors are close enough to be considered the same
     */
    static boolean isEqualColors(final int argb1, final int argb2) {
        final double red = (((argb1 >> 16) & 0xFF) - ((argb2 >> 16) & 0xFF)) / 255d;
        final double green = (((argb1 >> 8) & 0xFF) - ((argb2 >> 8) & 0xFF)) / 255d;
        final double blue = ((argb1 & 0xFF) - (argb2 & 0xFF)) / 255d;
        return Math.sqrt(red * red + green * green + blue * blue) <= COLOR_EQUALITY_THRESHOLD;
    }

    static int toArgb(final javafx.scene.paint.Color color) {
        return 0xFF000000 | ((int) Math.round(color.getRed() * 255) << 16)
            | ((int) Math.round(color.getGreen() * 255) << 8) | (int) Math.round(color.getBlue() * 255);
    }

    private static double gray(final int argb) {
        return (((argb >> 16) & 0xFF) + ((argb >> 8) & 0xFF) + (argb & 0xFF)) / 3d;
    }

    /**
     * @return the color of a pixel put on white, without transparency
     */
    private static int onWhite(final int argb) {
        final double alpha = ((argb >>> 24) & 0xFF) / 255d;
        final int red = (int) Math.round(((argb >> 16) & 0xFF) * alpha + 255 * (1 - alpha));
        final int green = (int) Math.round(((argb >> 8) & 0xFF) * alpha + 255 * (1 - alpha));
        final int blue = (int) Math.round((argb & 0xFF) * alpha + 255 * (1 - alpha));
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
