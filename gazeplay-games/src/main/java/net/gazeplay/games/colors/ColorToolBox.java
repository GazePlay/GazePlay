package net.gazeplay.games.colors;

import javafx.animation.PauseTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.components.CssUtil;
import net.gazeplay.components.GameStyle;
import net.gazeplay.components.GazeRoundButton;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool box of the "Coloring" game, beside the drawing, in the look of the menus: a rounded panel with the tools (coloring
 * on or paused, undo, start again), the 12 colors of the palette, the custom color (chosen in a window of other colors),
 * and the saving and printing of the drawing. Each button and color works with a fixation or a click.
 */
@Slf4j
public class ColorToolBox extends Pane {

    /**
     * The colors of the palette, all shown: white erases.
     */
    static final Color[] PALETTE = {
        Color.web("#E53935"), Color.web("#FB8C00"), Color.web("#FDD835"),
        Color.web("#9CCC65"), Color.web("#43A047"), Color.web("#4FC3F7"),
        Color.web("#1E88E5"), Color.web("#8E24AA"), Color.web("#F06292"),
        Color.web("#8D6E63"), Color.web("#9E9E9E"), Color.WHITE};

    /**
     * Other colors, in the window of the custom color.
     */
    static final Color[] CUSTOM_COLORS = {
        Color.BURLYWOOD, Color.DARKCYAN, Color.BLUEVIOLET, Color.BROWN, Color.CADETBLUE,
        Color.DARKORANGE, Color.GOLD, Color.LIMEGREEN, Color.ROYALBLUE, Color.SIENNA,
        Color.YELLOWGREEN, Color.PEACHPUFF, Color.LIGHTPINK, Color.NAVY, Color.DARKGREEN};

    private static final int COLUMNS = 3;

    private static final int OTHER_COLORS_COLUMNS = 5;

    /**
     * The seconds the message "Drawing kept" shows.
     */
    static final double MESSAGE = 2.5;

    private static final DateTimeFormatter FILE_NAME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private Group message;
    private javafx.collections.ObservableList<Node> messageChildren;
    private PauseTransition messagePause;

    private final IGameContext gameContext;

    private final ColorsGame colorsGame;

    @Getter
    private final List<ColorBox> colorBoxes = new ArrayList<>();

    @Getter
    private final ColorBox customBox;

    @Getter
    private ColorBox selectedColorBox;

    @Getter
    private final GazeRoundButton coloringButton;

    @Getter
    private final GazeRoundButton undoButton;

    private final List<GazeRoundButton> buttons = new ArrayList<>();

    /**
     * The buttons keeping and printing the drawing, null when the settings hide them (points 74 and 75).
     */
    @Getter
    private GazeRoundButton saveButton;
    @Getter
    private GazeRoundButton printButton;

    private Stage customColorDialog;

    private final javafx.beans.value.ChangeListener<Boolean> coloringListener;

    /**
     * @param width  width of the tool box
     * @param height height of the tool box
     */
    public ColorToolBox(final IGameContext gameContext, final ColorsGame colorsGame, final double width,
                        final double height) {
        this.gameContext = gameContext;
        this.colorsGame = colorsGame;
        final Translator translator = gameContext.getTranslator();

        final double gap = Math.max(6, width * 0.04);
        final double buttonSize = Math.min((width - 4 * gap) / 3, height / 9);
        // Rows: tools, 4 rows of colors, the custom color, the drawings
        final double swatchSize = Math.min((width - (COLUMNS + 1) * gap) / COLUMNS,
            (height - 2 * buttonSize - 9 * gap) / 5);

        final Rectangle background = new Rectangle(width, height);
        background.setArcWidth(Math.min(48, width * 0.15));
        background.setArcHeight(background.getArcWidth());
        background.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        getChildren().add(background);

        double y = gap;

        // Tools
        coloringButton = button("palette", translator.translate("ColoringOnOff"), buttonSize,
            () -> colorsGame.setEnableColorization(!colorsGame.getDrawingEnable().get()));
        coloringButton.setActive(colorsGame.getDrawingEnable().get());
        coloringListener = (observable, oldValue, newValue) -> coloringButton.setActive(newValue);
        colorsGame.getDrawingEnable().addListener(coloringListener);
        undoButton = button("undo", translator.translate("Undo"), buttonSize, colorsGame::undo);
        undoButton.setEnabled(false);
        final GazeRoundButton restartButton = button("restart_alt", translator.translate("RestartDrawing"), buttonSize,
            colorsGame::restartDrawing);
        placeRow(List.of(coloringButton, undoButton, restartButton), buttonSize, width, y);
        y += buttonSize + 2 * gap;

        // The palette
        final double gridWidth = COLUMNS * swatchSize + (COLUMNS - 1) * gap;
        final double gridX = (width - gridWidth) / 2;
        for (int i = 0; i < PALETTE.length; i++) {
            final ColorBox colorBox = new ColorBox(gameContext, PALETTE[i], swatchSize, this::select);
            final int column = i % COLUMNS;
            final int line = i / COLUMNS;
            colorBox.setLayoutX(gridX + column * (swatchSize + gap));
            colorBox.setLayoutY(y + line * (swatchSize + gap));
            colorBoxes.add(colorBox);
            getChildren().add(colorBox);
        }
        final int lines = (PALETTE.length + COLUMNS - 1) / COLUMNS;
        y += lines * (swatchSize + gap) + gap;

        // The custom color, and the button choosing it
        customBox = new ColorBox(gameContext, CUSTOM_COLORS[0], swatchSize, this::select);
        colorBoxes.add(customBox);
        final GazeRoundButton customButton = button("add", translator.translate("customColorDialogTitle"),
            swatchSize * 0.8, this::openCustomColorDialog);
        customBox.setLayoutX(gridX);
        customBox.setLayoutY(y);
        customButton.setLayoutX(gridX + swatchSize + gap + swatchSize * 0.1);
        customButton.setLayoutY(y + swatchSize * 0.1);
        getChildren().add(customBox);
        y += swatchSize + 2 * gap;

        // The drawing: save, print, each one shown if the settings allow it (tester's feedback of 24/11/2026, points 74
        // and 75)
        final List<GazeRoundButton> drawingButtons = new ArrayList<>();
        if (gameContext.getConfiguration().isColoringSaveEnabled()) {
            saveButton = button("save", translator.translate("SaveImg"), buttonSize, this::saveDrawing);
            drawingButtons.add(saveButton);
        }
        if (gameContext.getConfiguration().isColoringPrintEnabled()) {
            printButton = button("print", translator.translate("PrintImg"), buttonSize,
                () -> {
                    if (colorsGame.getWritableImg() != null) {
                        printImage(colorsGame.getWritableImg());
                    }
                });
            drawingButtons.add(printButton);
        }
        if (!drawingButtons.isEmpty()) {
            placeRow(drawingButtons, buttonSize, width, Math.max(y, height - gap - buttonSize));
        }

        select(colorBoxes.get(0));
    }

    private GazeRoundButton button(final String icon, final String tooltip, final double size, final Runnable action) {
        final GazeRoundButton button = new GazeRoundButton(gameContext, size, icon, tooltip, action);
        buttons.add(button);
        getChildren().add(button);
        return button;
    }

    /**
     * Places buttons on a row, centered in the width of the tool box.
     */
    private static void placeRow(final List<GazeRoundButton> row, final double size, final double width, final double y) {
        final double spacing = (width - row.size() * size) / (row.size() + 1);
        for (int i = 0; i < row.size(); i++) {
            row.get(i).setLayoutX(spacing + i * (size + spacing));
            row.get(i).setLayoutY(y);
        }
    }

    /**
     * Makes a color the one of the coloring.
     */
    void select(final ColorBox colorBox) {
        if (selectedColorBox != null) {
            selectedColorBox.setSelected(false);
        }
        selectedColorBox = colorBox;
        colorBox.setSelected(true);
    }

    /**
     * Opens the window of the other colors; the coloring pauses while it is open.
     */
    private void openCustomColorDialog() {
        if (customColorDialog != null && customColorDialog.isShowing()) {
            return;
        }
        final boolean coloringWasEnabled = colorsGame.getDrawingEnable().get();
        colorsGame.setEnableColorization(false);

        final Stage dialog = new Stage();
        dialog.initOwner(gameContext.getPrimaryStage());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setAlwaysOnTop(true);

        final double size = Math.max(60, gameContext.getGamePanelDimensionProvider().getDimension2D().getHeight() / 9);
        final List<ColorBox> otherColors = new ArrayList<>();
        for (final Color color : CUSTOM_COLORS) {
            otherColors.add(new ColorBox(gameContext, color, size, chosen -> {
                customBox.setColor(chosen.getColor());
                select(customBox);
                dialog.close();
            }));
        }
        final Pane grid = otherColorsGrid(otherColors, size);

        final Label title = new Label(gameContext.getTranslator().translate("customColorDialogTitle"));
        title.setStyle(GameStyle.titleFontStyle(size * 0.4) + " -fx-text-fill: -gp-text;");
        final GazeRoundButton closeButton = new GazeRoundButton(gameContext, size * 0.8, "close",
            gameContext.getTranslator().translate("Close"), dialog::close);
        dialog.setOnHidden(event -> {
            // Closed, the window no longer follows the gaze (a fixation started in it could still choose a color)
            otherColors.forEach(ColorBox::dispose);
            closeButton.dispose();
            colorsGame.setEnableColorization(coloringWasEnabled);
        });

        final VBox card = new VBox(size * 0.25, title, grid, fixedPane(size * 0.8, size * 0.8, closeButton));
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(size * 0.3));
        card.setStyle("-fx-background-color: -gp-surface; -fx-background-radius: 24; -fx-border-color: -gp-border;"
            + " -fx-border-width: 2; -fx-border-radius: 24;");

        final Scene scene = new Scene(card, Color.TRANSPARENT);
        CssUtil.setPreferredStylesheets(gameContext.getConfiguration(), scene);
        dialog.setScene(scene);
        customColorDialog = dialog;
        dialog.show();
        dialog.centerOnScreen();
    }

    /**
     * The other colors, {@value #OTHER_COLORS_COLUMNS} on a row, each at its own place: the ring of a fixation being
     * filled goes a little out of its swatch, and a layout (FlowPane) placed them all again meanwhile, the one looked at
     * leaving the gaze and its fixation starting again from zero (tester's feedback of 23/11/2026, point 73).
     */
    static Pane otherColorsGrid(final List<ColorBox> colors, final double size) {
        final double step = size * 1.2;
        for (int i = 0; i < colors.size(); i++) {
            final int row = i / OTHER_COLORS_COLUMNS;
            colors.get(i).setLayoutX(i % OTHER_COLORS_COLUMNS * step);
            colors.get(i).setLayoutY(row * step);
        }
        final int rows = (colors.size() + OTHER_COLORS_COLUMNS - 1) / OTHER_COLORS_COLUMNS;
        return fixedPane((OTHER_COLORS_COLUMNS - 1) * step + size, (rows - 1) * step + size,
            colors.toArray(new Node[0]));
    }

    /**
     * @return a pane of a fixed size, whatever the rings of the fixations in it
     */
    private static Pane fixedPane(final double width, final double height, final Node... nodes) {
        final Pane pane = new Pane(nodes);
        pane.setMinSize(width, height);
        pane.setPrefSize(width, height);
        pane.setMaxSize(width, height);
        return pane;
    }

    /**
     * Keeps the drawing as a picture (PNG) in the folder "dessins" of GazePlay, as "Gribouille" does, under the date
     * and the time, with a message (tester's feedback of 24/11/2026, point 75; maintainer's choice of 26/11/2026): the
     * window of the system to choose a file is no longer opened, which a child alone could not leave.
     *
     * @return the file written, null when nothing was
     */
    File saveDrawing() {
        if (colorsGame.getWritableImg() == null) {
            return null;
        }
        final File folder = GazePlayDirectories.getDrawingsDirectory();
        final String base = "coloriage_" + LocalDateTime.now().format(FILE_NAME);
        File imageFile = new File(folder, base + ".png");
        for (int i = 2; imageFile.exists(); i++) {
            imageFile = new File(folder, base + "-" + i + ".png");
        }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(colorsGame.getWritableImg(), null), "png", imageFile);
            say("ScribbleSaved");
            return imageFile;
        } catch (final IOException | RuntimeException ex) {
            log.warn("The drawing could not be saved in {}", imageFile, ex);
            say("ScribbleNotSaved");
            return null;
        }
    }

    /**
     * A short message at the top of the game area, for {@link #MESSAGE} seconds.
     */
    private void say(final String key) {
        final Dimension2D area = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate(key), area.getHeight() * 0.035);
        pill.setMouseTransparent(true);
        pill.setLayoutX((area.getWidth() - pill.getLayoutBounds().getWidth()) / 2);
        pill.setLayoutY(area.getHeight() * 0.03);
        removeMessage();
        // The list itself, not the game context: the pause may end after the game (stopped by dispose, though)
        final javafx.collections.ObservableList<Node> children = gameContext.getChildren();
        message = pill;
        messageChildren = children;
        children.add(pill);
        messagePause = new PauseTransition(Duration.seconds(MESSAGE));
        messagePause.setOnFinished(event -> removeMessage());
        messagePause.play();
    }

    private void removeMessage() {
        if (messagePause != null) {
            messagePause.stop();
            messagePause = null;
        }
        if (message != null && messageChildren != null) {
            messageChildren.remove(message);
        }
        message = null;
        messageChildren = null;
    }

    /**
     * @return the message shown now (for the tests), null when none
     */
    Group getMessage() {
        return message;
    }

    /**
     * Prints the drawing, after the print and page setup dialogs of the system.
     */
    private void printImage(final Image image) {
        final PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob == null) {
            log.info("No printer");
            return;
        }
        if (printerJob.showPrintDialog(gameContext.getPrimaryStage())
            && printerJob.showPageSetupDialog(gameContext.getPrimaryStage())) {
            gameContext.getPrimaryStage().requestFocus();

            final ImageView iv = new ImageView(image);
            iv.setPreserveRatio(true);
            final PageLayout pageLayout = printerJob.getJobSettings().getPageLayout();
            final double scale = Math.min(pageLayout.getPrintableWidth() / image.getWidth(),
                pageLayout.getPrintableHeight() / image.getHeight());
            iv.getTransforms().add(new Scale(scale, scale));
            if (printerJob.printPage(iv)) {
                printerJob.endJob();
            } else {
                log.debug("The printing fail");
            }
        } else {
            log.info("don't print because the user cancel it");
        }
    }

    /**
     * Stops the buttons and colors following the gaze, at the end of the game.
     */
    void dispose() {
        // The message "Drawing kept" and its pause go with the game: the pause ran on after it (CI of 26/11/2026)
        removeMessage();
        colorsGame.getDrawingEnable().removeListener(coloringListener);
        buttons.forEach(GazeRoundButton::dispose);
        colorBoxes.forEach(ColorBox::dispose);
        if (customColorDialog != null) {
            customColorDialog.close();
        }
    }
}
