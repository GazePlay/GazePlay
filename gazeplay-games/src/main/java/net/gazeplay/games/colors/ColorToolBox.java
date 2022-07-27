package net.gazeplay.games.colors;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.components.CssUtil;
import net.gazeplay.components.GazeFollowerIndicator;
import net.gazeplay.components.GazeIndicator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ColorToolBox extends Pane {

    /**
     * The size of the next and previous
     */
    public static final double PAGE_MOVING_BUTTONS_SIZE_PIXEL = 20;

    public static final Integer NB_COLORS_DISPLAYED = 5;

    public final double colorizeButtonsSizePx;

    public static final String COLORS_IMAGES_PATH = "data/colors/images/";

    /**
     * Credits
     * <div>Icons made by <a href="https://www.flaticon.com/authors/google" title="Google">Google</a> from <a
     * href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
     * href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
     * BY</a></div>
     */
    public static final String CUSTOM_BUTTON_IMAGE_PATH = COLORS_IMAGES_PATH + "add-button-inside-black-circle.png";

    public static final String COLORIZE_BUTTON_IMAGE_NAME = COLORS_IMAGES_PATH + "palette.png";

    public static final String STOP_COLORIZE_BUTTON_IMAGE_PATH = "data/common/images/error.png";

    private final VBox mainPane;

    private final GazeIndicator progressIndicator;

    @Getter
    @Setter
    private boolean dialogOpen = false;

    /**
     * All the color boxes
     */
    @Getter
    private final List<ColorBox> colorBoxes;

    @Getter
    private final ColorsGame colorsGame;

    private final Pane root;

    private final ColorBox customBox;

    private final ColorPicker colorPicker;

    private final Button customColorPickerButton;

    @Getter
    private final Pane imageManager;

    @Getter
    private final Pane colorziationPane;

    private final Stage customColorDialog;

    private final IGameContext gameContext;

    /**
     * The index of the first color displayed (then followed by the NB_COLORS_DISPLAYED next colors).
     */
    private int firstColorDisplayed;

    @Getter
    @Setter
    private ColorBox selectedColorBox;

    private EventHandler disableColorizeButton = null;

    private boolean previousEnableColor;

    public ColorToolBox(final Pane root, final ColorsGame colorsGame, final IGameContext gameContext, final double colorizeButtonsSizePx) {
        super();
        this.gameContext = gameContext;
        this.colorizeButtonsSizePx = colorizeButtonsSizePx;
        progressIndicator = new GazeIndicator(gameContext);
        progressIndicator.setMouseTransparent(true);
        this.prefWidthProperty().bind(gameContext.getPrimaryScene().widthProperty().divide(5));

        this.selectedColorBox = null;
        this.colorsGame = colorsGame;
        this.root = root;

        // the main pane for the tool box
        final BorderPane thisRoot = new BorderPane();
        this.getChildren().add(thisRoot);

        mainPane = new VBox();
        thisRoot.setCenter(mainPane);
        mainPane.setAlignment(Pos.CENTER);
        thisRoot.prefWidthProperty().bind(this.widthProperty());
        mainPane.prefWidthProperty().bind(thisRoot.widthProperty());
        mainPane.setSpacing(0);

        imageManager = buildImageManager();
        colorziationPane = buildColorizationPane();

        thisRoot.setBottom(imageManager);
        thisRoot.setTop(colorziationPane);

        ColorBox colorBox;
        EventHandler<Event> eventHandler;

        // COLORS

        final List<Color> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.BLUE);
        colors.add(Color.GREEN);
        colors.add(Color.WHITE);
        colors.add(Color.DARKGOLDENROD);
        // 5

        // Build color boxes
        colorBoxes = new ArrayList<>();
        final ToggleGroup group = new ToggleGroup();
        firstColorDisplayed = 0;
        Color color;
        for (int i = 0; i < colors.size(); ++i) {

            color = colors.get(i);

            colorBox = new ColorBox(gameContext, color, root, this, group, colorizeButtonsSizePx);
            colorBoxes.add(colorBox);

            if (i < NB_COLORS_DISPLAYED) {
                mainPane.getChildren().add(colorBox);
            }

            if (this.selectedColorBox == null) {
                colorBox.select();
                selectedColorBox = colorBox;
            }
        }

        colorPicker = new ColorPicker(Color.WHITE);

        Image buttonImg = null;
        try {
            buttonImg = new Image(CUSTOM_BUTTON_IMAGE_PATH, colorizeButtonsSizePx, colorizeButtonsSizePx, false,
                true);
        } catch (final IllegalArgumentException e) {
            log.warn(e.toString() + " : " + CUSTOM_BUTTON_IMAGE_PATH);
        }

        if (buttonImg != null) {
            customColorPickerButton = new Button("", new ImageView(buttonImg));
            customColorPickerButton.setPrefHeight(buttonImg.getHeight());
        } else {
            customColorPickerButton = new Button("Custom colors");
            customColorPickerButton.setPrefHeight(colorizeButtonsSizePx);
            customColorPickerButton.setPrefWidth(colorizeButtonsSizePx);
        }
        customBox = new ColorBox(gameContext, Color.WHITE, root, this, group, colorizeButtonsSizePx);

        customColorDialog = buildCustomColorDialog();

        final EventHandler<ActionEvent> customColorButtonHandler = (ActionEvent event) -> {
            customColorDialog.show();
            customColorDialog.sizeToScene();

            this.dialogOpen = true;

            previousEnableColor = colorsGame.getDrawingEnable().getValue();
            if (previousEnableColor) {
                colorsGame.setEnableColorization(false);
            }

            customColorDialog.toFront();
            customColorDialog.setAlwaysOnTop(true);
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            customColorDialog.requestFocus();
        };

        final GazeIndicator customColorButtonIndic = new GazeFollowerIndicator(gameContext, root, customColorPickerButton);
        customColorButtonIndic.setOnFinish(customColorButtonHandler);
        customColorButtonIndic.addNodeToListen(customColorPickerButton,
            colorsGame.getGameContext().getGazeDeviceManager());

        customColorPickerButton.setOnAction(customColorButtonHandler);
        customColorPickerButton.setOpacity(1);

        customColorDialog.setOnCloseRequest((event) -> {
            this.setDialogOpen(false);
            colorsGame.setEnableColorization(previousEnableColor);
        });

        colorPicker.setOnAction((event) -> customBox.setColor(colorPicker.getValue()));
        colorPicker.prefWidthProperty().bind(customBox.widthProperty());
        customColorPickerButton.prefWidthProperty().bind(customBox.widthProperty());

        final Button previousPallet = new Button("");
        previousPallet.setPrefHeight(PAGE_MOVING_BUTTONS_SIZE_PIXEL);
        previousPallet.setPrefWidth(PAGE_MOVING_BUTTONS_SIZE_PIXEL);
        final Button nextPallet = new Button("");
        nextPallet.setPrefHeight(PAGE_MOVING_BUTTONS_SIZE_PIXEL);
        nextPallet.setPrefWidth(PAGE_MOVING_BUTTONS_SIZE_PIXEL);

        nextPallet.setOnAction((event) -> {

            firstColorDisplayed += NB_COLORS_DISPLAYED;

            updatePallet(previousPallet, nextPallet);
        });

        if (firstColorDisplayed + NB_COLORS_DISPLAYED > colorBoxes.size()) {
            nextPallet.setDisable(true);
        }

        previousPallet.setOnAction((event) -> {

            firstColorDisplayed -= NB_COLORS_DISPLAYED;

            updatePallet(previousPallet, nextPallet);
        });

        if (firstColorDisplayed - NB_COLORS_DISPLAYED < 0) {
            previousPallet.setDisable(true);
        }

        this.updatePallet(previousPallet, nextPallet);

        root.getChildren().add(customColorButtonIndic);

        gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<Void>() {
            @Override
            public Void visitLight() {
                return null;
            }

            @Override
            public Void visitDark() {
                getStyleClass().add("bg-colored");
                return null;
            }
        });


    }

    /**
     * Write an image to a File using Java Swing.
     *
     * @param image  The image to write.
     * @param file   The file to write the image into.
     * @param format The image format to use ("png" is the only one working correctly).
     */
    private static void saveImageToFile(final Image image, final File file, final String format) throws IOException {

        final BufferedImage swingImg = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(swingImg, format, file);
    }

    private void printImage(final Image image) {
        final PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob.showPrintDialog(gameContext.getPrimaryStage()) && printerJob.showPageSetupDialog(gameContext.getPrimaryStage())) {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameContext.getPrimaryStage().requestFocus();

            ImageView iv = new ImageView();
            iv.setPreserveRatio(true);
            iv.setImage(image);
            PageLayout pageLayout = printerJob.getJobSettings().getPageLayout();
            double scaleX = pageLayout.getPrintableWidth() / iv.getBoundsInParent().getWidth();
            double scaleY = pageLayout.getPrintableHeight() / iv.getBoundsInParent().getHeight();
            iv.getTransforms().add(new Scale(scaleX, scaleY));
            if (printerJob.printPage(iv)) {
                printerJob.endJob();
            } else {
                log.debug("The printing fail");
            }
        } else {
            log.info("don't print because the user cancel it");
        }
    }

    private static void configureImageFileChooser(final FileChooser imageFileChooser) {
        imageFileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Images", "*.*"),
            new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("GIF", "*.gif"));
    }

    private static void configureImageFileSaver(final FileChooser imageFileChooser) {
        imageFileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
    }

    private static boolean checkFormat(final String format) {
        return "png".equals(format);
    }

    private void copyFile(final File imageFile) {
        if (imageFile != null) {
            final Path from = Paths.get(imageFile.toURI());
            final File to = new File(this.getColorsDirectory(), imageFile.getName());
            final CopyOption[] options = new CopyOption[]{
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
            };
            try {
                Files.copy(from, to.toPath(), options);
                gameContext.getConfiguration().getColorsDefaultImageProperty().setValue(to.getAbsolutePath());
            } catch (final IOException e) {
                log.debug("Impossible to copy file" + imageFile.getAbsolutePath() + " to " + to.getAbsolutePath());
            }
        }
    }

    private Pane buildImageManager() {
        final Translator translator = gameContext.getTranslator();
        final Stage primaryStage = gameContext.getPrimaryStage();

        final HBox bottomBox = new HBox(7);
        bottomBox.setPadding(new Insets(0, 5, 0, 5));

        final FileChooser imageChooser = new FileChooser();
        configureImageFileChooser(imageChooser);
        imageChooser.setTitle(translator.translate("imgChooserTitle"));

        final Button imageChooserButton = new Button(translator.translate("LoadImg"));
        imageChooserButton.setPrefHeight(colorizeButtonsSizePx / 2);
        imageChooserButton.setOnAction((event) -> {

            final File imageFile = imageChooser.showOpenDialog(primaryStage);
            if (imageFile != null) {

                copyFile(imageFile);

                final Image image = new Image(imageFile.toURI().toString());

                this.colorsGame.updateImage(image, imageFile.getName());
            }
        });

        final FileChooser imageSaveChooser = new FileChooser();
        configureImageFileSaver(imageSaveChooser);
        imageSaveChooser.setTitle(translator.translate("imgSaveChooserTitle"));
        final Button imageSaverButton = new Button(translator.translate("SaveImg"));
        imageSaverButton.setPrefHeight(colorizeButtonsSizePx / 2);
        imageSaverButton.setOnAction((event) -> {

            File imageFile = imageSaveChooser.showSaveDialog(primaryStage);
            if (imageFile != null) {

                try {
                    final String name = imageFile.getName();
                    String extension = name.substring(1 + name.lastIndexOf(".")).toLowerCase();

                    if (!checkFormat(extension)) {
                        extension = "png";
                        imageFile = new File(imageFile.getAbsolutePath() + "." + extension);
                    }

                    saveImageToFile(colorsGame.getWritableImg(), imageFile, extension);
                } catch (final IOException ex) {
                    log.error("Error while saving image : " + ex.toString());
                }
            }
        });

        final Button pImageButton = new Button(translator.translate("PrintImg"));
        pImageButton.setPrefHeight(colorizeButtonsSizePx / 2);
        pImageButton.setOnAction((event) -> {
            printImage(colorsGame.getWritableImg());
        });

        imageChooserButton.setOpacity(1);
        imageSaverButton.setOpacity(1);
        pImageButton.setOpacity(1);
        bottomBox.getChildren().add(imageChooserButton);
        bottomBox.getChildren().add(imageSaverButton);
        bottomBox.getChildren().add(pImageButton);

        return bottomBox;
    }

    private File getColorsDirectory() {
        final Configuration config = gameContext.getConfiguration();
        final String userName = config.getUserName();
        final File colorsDirectory = new File(GazePlayDirectories.getUserDataFolder(userName), "colors");
        final boolean mkDirSuccess = colorsDirectory.mkdirs();

        if (!mkDirSuccess) {
            log.debug(colorsDirectory.getAbsolutePath() + " can't be created");
        }

        return colorsDirectory;
    }

    private void updatePallet(final Button previousPallet, final Button nextPallet) {

        mainPane.getChildren().clear();

        this.buildAddCustomCostomColorButton();

        for (int i = firstColorDisplayed; i < firstColorDisplayed + NB_COLORS_DISPLAYED && i < colorBoxes.size(); ++i) {
            mainPane.getChildren().add(colorBoxes.get(i));
        }

        if (nextPallet.isDisable() && firstColorDisplayed + NB_COLORS_DISPLAYED < colorBoxes.size()) {
            nextPallet.setDisable(false);
        }

        if (previousPallet.isDisable() && firstColorDisplayed - NB_COLORS_DISPLAYED >= 0) {

            previousPallet.setDisable(false);
        }

        if (firstColorDisplayed + NB_COLORS_DISPLAYED >= colorBoxes.size()) {
            nextPallet.setDisable(true);
        } else if (firstColorDisplayed - NB_COLORS_DISPLAYED < 0) {
            previousPallet.setDisable(true);
        }
    }

    private void buildAddCustomCostomColorButton() {

        final Pane customColorPane = new VBox(customBox, customColorPickerButton);
        mainPane.getChildren().add(customColorPane);
    }

    private Pane buildColorizationPane() {

        Image buttonImg = null;
        try {
            buttonImg = new Image(COLORIZE_BUTTON_IMAGE_NAME, colorizeButtonsSizePx, colorizeButtonsSizePx, false,
                true);
        } catch (final IllegalArgumentException e) {
            log.warn(e.toString() + " : " + COLORIZE_BUTTON_IMAGE_NAME);
        }

        final Button colorize;
        if (buttonImg != null) {
            colorize = new Button("", new ImageView(buttonImg));
            colorize.setPrefHeight(buttonImg.getHeight());
        } else {
            colorize = new Button("C");
        }

        buttonImg = null;
        try {
            buttonImg = new Image(STOP_COLORIZE_BUTTON_IMAGE_PATH, colorizeButtonsSizePx, colorizeButtonsSizePx,
                false, true);
        } catch (final IllegalArgumentException e) {
            log.warn(e.toString() + " : " + STOP_COLORIZE_BUTTON_IMAGE_PATH);
        }

        final Button stopColorize;
        if (buttonImg != null) {
            stopColorize = new Button("", new ImageView(buttonImg));
            stopColorize.setPrefHeight(stopColorize.getHeight());
        } else {
            stopColorize = new Button("S");
        }

        final GazeIndicator colorizeButtonIndicator = new GazeIndicator(gameContext);
        colorizeButtonIndicator.setMouseTransparent(true);

        colorize.setOpacity(1);
        stopColorize.setOpacity(1);

        final Pane colorizeButtonPane = new StackPane(colorize);
        final Pane stopColorizeButtonPane = new StackPane(stopColorize);

        final EventHandler enableColorizeButton = (Event event1) -> colorsGame.setEnableColorization(false);

        disableColorizeButton = (Event event1) -> colorsGame.setEnableColorization(true);

        colorsGame.getDrawingEnable().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                colorizeButtonPane.setVisible(false);
                stopColorizeButtonPane.setVisible(true);
                colorizeButtonIndicator.setOnFinish(disableColorizeButton);
            } else {
                stopColorizeButtonPane.setVisible(false);
                colorizeButtonPane.setVisible(true);
                colorizeButtonIndicator.setOnFinish(enableColorizeButton);
            }
        });

        colorizeButtonIndicator.setOnFinish(enableColorizeButton);

        colorizeButtonIndicator.addNodeToListen(stopColorizeButtonPane,
            getColorsGame().getGameContext().getGazeDeviceManager());
        colorizeButtonIndicator.addNodeToListen(colorizeButtonPane,
            getColorsGame().getGameContext().getGazeDeviceManager());

        stopColorizeButtonPane.setVisible(false);

        return new StackPane(colorizeButtonPane, stopColorizeButtonPane, colorizeButtonIndicator);
    }

    private Stage buildCustomColorDialog() {
        final Translator translator = gameContext.getTranslator();
        final Stage primaryStage = gameContext.getPrimaryStage();

        final Stage dialog = new Stage();

        final net.gazeplay.games.colors.CustomColorPicker customColorPicker = new CustomColorPicker(gameContext, root, this, customBox, dialog, colorizeButtonsSizePx);

        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(
            windowEvent -> {
                primaryStage.getScene().getRoot().setEffect(null);
            });
        dialog.setTitle(translator.translate("customColorDialogTitle"));
        dialog.setAlwaysOnTop(true);

        final Scene scene = new Scene(customColorPicker, Color.TRANSPARENT);

        final Configuration config = gameContext.getConfiguration();
        CssUtil.setPreferredStylesheets(config, scene, gameContext.getCurrentScreenDimensionSupplier());

        dialog.setScene(scene);

        return dialog;
    }

    public GazeIndicator getProgressIndicator() {
        return progressIndicator;
    }
}
