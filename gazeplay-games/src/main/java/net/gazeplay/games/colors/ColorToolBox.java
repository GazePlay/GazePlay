package net.gazeplay.games.colors;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.components.CssUtil;
import net.gazeplay.components.GameStyle;
import net.gazeplay.components.GazeRoundButton;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
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

        // The drawing: save, print
        final GazeRoundButton saveButton = button("save", translator.translate("SaveImg"), buttonSize,
            this::saveDrawing);
        final GazeRoundButton printButton = button("print", translator.translate("PrintImg"), buttonSize,
            () -> {
                if (colorsGame.getWritableImg() != null) {
                    printImage(colorsGame.getWritableImg());
                }
            });
        placeRow(List.of(saveButton, printButton), buttonSize, width,
            Math.max(y, height - gap - buttonSize));

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
        dialog.setOnHidden(event -> colorsGame.setEnableColorization(coloringWasEnabled));

        final double size = Math.max(60, gameContext.getGamePanelDimensionProvider().getDimension2D().getHeight() / 9);
        final FlowPane grid = new FlowPane(size * 0.2, size * 0.2);
        grid.setPrefWrapLength(5 * size + 4 * size * 0.2);
        grid.setAlignment(Pos.CENTER);
        for (final Color color : CUSTOM_COLORS) {
            grid.getChildren().add(new ColorBox(gameContext, color, size, chosen -> {
                customBox.setColor(chosen.getColor());
                select(customBox);
                dialog.close();
            }));
        }

        final Label title = new Label(gameContext.getTranslator().translate("customColorDialogTitle"));
        title.setStyle(GameStyle.titleFontStyle(size * 0.4) + " -fx-text-fill: -gp-text;");
        final GazeRoundButton closeButton = new GazeRoundButton(gameContext, size * 0.8, "close",
            gameContext.getTranslator().translate("Close"), dialog::close);

        final VBox card = new VBox(size * 0.25, title, grid, closeButton);
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
     * Saves the drawing as a PNG file chosen by the user.
     */
    private void saveDrawing() {
        if (colorsGame.getWritableImg() == null) {
            return;
        }
        final FileChooser imageSaveChooser = new FileChooser();
        imageSaveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        imageSaveChooser.setTitle(gameContext.getTranslator().translate("imgSaveChooserTitle"));
        File imageFile = imageSaveChooser.showSaveDialog(gameContext.getPrimaryStage());
        if (imageFile == null) {
            return;
        }
        if (!imageFile.getName().toLowerCase().endsWith(".png")) {
            imageFile = new File(imageFile.getAbsolutePath() + ".png");
        }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(colorsGame.getWritableImg(), null), "png", imageFile);
        } catch (final IOException ex) {
            log.error("Error while saving image : " + ex);
        }
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
        colorsGame.getDrawingEnable().removeListener(coloringListener);
        buttons.forEach(GazeRoundButton::dispose);
        colorBoxes.forEach(ColorBox::dispose);
        if (customColorDialog != null) {
            customColorDialog.close();
        }
    }
}
