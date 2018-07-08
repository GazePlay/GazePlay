package net.gazeplay.games.colors;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CssUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ColorToolBox extends StackPane {

    /**
     * Pourcents use to compute height and width.
     */
    public static final double WIDTH_POURCENT = 0.10;
    public static final double HEIGHT_POURCENT = 0.80;

    public static final double SPACING_PX = 10;

    public static final Insets MAIN_INSETS = new Insets(10, 15, 10, 15);

    /**
     * The size of the next and previous
     */
    public static final double PAGE_MOVING_BUTTONS_SIZE_PIXEL = 0;

    public static final Integer NB_COLORS_DISPLAYED = 5;

    public static final double COLORIZE_BUTTONS_SIZE_PX = 64;

    public static final String COLORS_IMAGES_PATH = "data" + File.separator + "colors" + File.separator + "images"
            + File.separator;

    // Credits
    // <div>Icons made by <a href="https://www.flaticon.com/authors/google" title="Google">Google</a> from <a
    // href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    public static final String CUSTOM_BUTTON_IMAGE_PATH = COLORS_IMAGES_PATH + "add-button-inside-black-circle.png";

    public static final String COLORIZE_BUTTON_IMAGE_NAME = COLORS_IMAGES_PATH + "palette.png";
    public static final String STOP_COLORIZE_BUTTON_IMAGE_PATH = "data/common/images/error.png";

    private final VBox mainPane;

    private final AbstractGazeIndicator progressIndicator;

    /**
     * All the color boxes
     */
    private final List<ColorBox> colorBoxes;

    /**
     * The index of the first color displayed (then followed by the NB_COLORS_DISPLAYED next colors).
     */
    private int firstColorDisplayed;

    @Getter
    private final ColorsGame colorsGame;

    private final Pane root;

    private final ColorBox customBox;

    private final ColorPicker colorPicker;
    private final Button customColorPickerButton;

    @Getter
    @Setter
    private ColorBox selectedColorBox;

    @Getter
    private final Pane imageManager;

    @Getter
    private final Pane colorziationPane;

    private EventHandler disableColorizeButton = null;

    private final Stage customColorDialog;
    private CustomColorPicker CustomColorPicker;

    private boolean previousEnableColor;

    public ColorToolBox(final Pane root, final ColorsGame colorsGame) {
        super();

        progressIndicator = new GazeFollowerIndicator(root);

        this.selectedColorBox = null;
        this.colorsGame = colorsGame;
        this.root = root;

        // the main pane for the tool box
        BorderPane thisRoot = new BorderPane();
        this.getChildren().add(thisRoot);

        mainPane = new VBox();
        thisRoot.setCenter(mainPane);
        mainPane.setSpacing(SPACING_PX);
        mainPane.setPadding(MAIN_INSETS);

        imageManager = buildImageManager();
        colorziationPane = buildColorizationPane();

        ColorBox colorBox;
        EventHandler<Event> eventHandler;

        // COLORS

        List<Color> colors = new ArrayList<Color>();
        colors.add(Color.RED);
        colors.add(Color.BLUE);
        colors.add(Color.GREEN);
        colors.add(Color.WHITE);
        colors.add(Color.DARKGOLDENROD);
        // 5

        // Build color boxes
        colorBoxes = new ArrayList<ColorBox>();
        ToggleGroup group = new ToggleGroup();
        firstColorDisplayed = 0;
        Color color;
        for (int i = 0; i < colors.size(); ++i) {

            color = colors.get(i);

            colorBox = new ColorBox(color, root, this, group);
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
            buttonImg = new Image(CUSTOM_BUTTON_IMAGE_PATH, COLORIZE_BUTTONS_SIZE_PX, COLORIZE_BUTTONS_SIZE_PX, false,
                    true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + CUSTOM_BUTTON_IMAGE_PATH);
        }

        if (buttonImg != null) {
            customColorPickerButton = new Button("", new ImageView(buttonImg));
            customColorPickerButton.setPrefHeight(buttonImg.getHeight());
        } else {
            customColorPickerButton = new Button("Custom colors");
            customColorPickerButton.setPrefHeight(COLORIZE_BUTTONS_SIZE_PX);
            customColorPickerButton.setPrefWidth(COLORIZE_BUTTONS_SIZE_PX);
        }
        customBox = new ColorBox(Color.WHITE, root, this, group);

        customColorDialog = buildCustomColorDialog();

        final EventHandler<ActionEvent> customColorButtonHandler = (ActionEvent event) -> {
            customColorDialog.show();
            customColorDialog.sizeToScene();

            previousEnableColor = colorsGame.getDrawingEnable().getValue();
            if (previousEnableColor) {
                colorsGame.setEnableColorization(false);
            }
        };

        final AbstractGazeIndicator customColorButtonIndic = new GazeFollowerIndicator(root);
        customColorButtonIndic.setOnFinish(customColorButtonHandler);
        customColorButtonIndic.addNodeToListen(customColorPickerButton,
                colorsGame.getGameContext().getGazeDeviceManager());

        customColorPickerButton.setOnAction(customColorButtonHandler);

        customColorDialog.setOnCloseRequest((event) -> {

            /*
             * log.info("custom indic min width = {}, min height = {}", customColorButtonIndic.getMinWidth(),
             * customColorButtonIndic.getMinHeight()); log.info("custom indic width = {}, height = {}",
             * customColorButtonIndic.getWidth(), customColorButtonIndic.getHeight());
             */
            colorsGame.setEnableColorization(previousEnableColor);

        });

        colorPicker.setOnAction((event) -> {
            customBox.setColor(colorPicker.getValue());
        });
        colorPicker.prefWidthProperty().bind(customBox.widthProperty());
        customColorPickerButton.prefWidthProperty().bind(customBox.widthProperty());

        Button previousPallet = new Button("");
        previousPallet.setPrefHeight(PAGE_MOVING_BUTTONS_SIZE_PIXEL);
        previousPallet.setPrefWidth(PAGE_MOVING_BUTTONS_SIZE_PIXEL);
        Button nextPallet = new Button("");
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

        /*
         * this.setRight(nextPallet); this.setLeft(previousPallet);
         */

        thisRoot.setBottom(imageManager);
        thisRoot.setTop(colorziationPane);
        root.getChildren().add(customColorButtonIndic);

        if (!Configuration.getInstance().isBackgroundWhite()) {

            this.getStyleClass().add("bg-colored");
        }
    }

    private Pane buildImageManager() {

        final HBox bottomBox = new HBox(7);
        bottomBox.setPadding(new Insets(0, 5, 2, 5));

        final Translator translator = GazePlay.getInstance().getTranslator();

        final FileChooser imageChooser = new FileChooser();
        configureImageFileChooser(imageChooser);
        imageChooser.setTitle(translator.translate("imgChooserTitle"));

        final Stage stage = GazePlay.getInstance().getPrimaryStage();

        Button imageChooserButton = new Button(translator.translate("LoadImg"));
        imageChooserButton.setOnAction((event) -> {

            final File imageFile = imageChooser.showOpenDialog(stage);
            if (imageFile != null) {

                Image image = new Image(imageFile.toURI().toString());

                this.colorsGame.updateImage(image, imageFile.getName());
            }
        });

        final FileChooser imageSaveChooser = new FileChooser();
        configureImageFileSaver(imageSaveChooser);
        imageSaveChooser.setTitle(translator.translate("imgSaveChooserTitle"));
        Button imageSaverButton = new Button(translator.translate("SaveImg"));
        imageSaverButton.setOnAction((event) -> {

            File imageFile = imageSaveChooser.showSaveDialog(stage);
            if (imageFile != null) {

                try {
                    String name = imageFile.getName();
                    String extension = name.substring(1 + name.lastIndexOf(".")).toLowerCase();

                    if (!checkFormat(extension)) {
                        extension = "png";
                        name += extension;
                        imageFile = new File(imageFile.getAbsolutePath() + "." + extension);
                    }

                    saveImageToFile(colorsGame.getWritableImg(), imageFile, extension);
                } catch (IOException ex) {
                    log.error("Error while saving image : " + ex.toString());
                }
            }
        });

        bottomBox.getChildren().add(imageChooserButton);
        bottomBox.getChildren().add(imageSaverButton);

        return bottomBox;
    }

    /**
     * Write an image to a File using Java Swing.
     * 
     * @param image
     *            The image to write.
     * @param file
     *            The file to write the image into.
     * @param format
     *            The image format to use ("png" is the only one working correctly).
     */
    private static void saveImageToFile(final Image image, final File file, final String format) throws IOException {

        BufferedImage swingImg = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(swingImg, format, file);
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

        switch (format) {
        case "png":
            return true;

        default:
            return false;
        }
    }

    private void updatePallet(Button previousPallet, Button nextPallet) {

        mainPane.getChildren().clear();

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

        this.buildAddCustomCostomColorButton();
    }

    private void buildAddCustomCostomColorButton() {

        Pane customColorPane = new VBox(customBox, customColorPickerButton);
        // Pane customColorPane = new VBox(customBox, colorPicker);
        mainPane.getChildren().add(customColorPane);
    }

    private Pane buildColorizationPane() {

        Image buttonImg = null;
        try {
            buttonImg = new Image(COLORIZE_BUTTON_IMAGE_NAME, COLORIZE_BUTTONS_SIZE_PX, COLORIZE_BUTTONS_SIZE_PX, false,
                    true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + COLORIZE_BUTTON_IMAGE_NAME);
        }

        Button colorize;
        if (buttonImg != null) {
            colorize = new Button("", new ImageView(buttonImg));
            colorize.setPrefHeight(buttonImg.getHeight());
        } else {
            colorize = new Button("C");
        }

        buttonImg = null;
        try {
            buttonImg = new Image(STOP_COLORIZE_BUTTON_IMAGE_PATH, COLORIZE_BUTTONS_SIZE_PX, COLORIZE_BUTTONS_SIZE_PX,
                    false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + STOP_COLORIZE_BUTTON_IMAGE_PATH);
        }

        Button stopColorize;
        if (buttonImg != null) {
            stopColorize = new Button("", new ImageView(buttonImg));
            stopColorize.setPrefHeight(stopColorize.getHeight());
        } else {
            stopColorize = new Button("S");
        }

        Configuration config = this.getColorsGame().getConfig();

        AbstractGazeIndicator colorizeButtonIndicator = new GazeFollowerIndicator(root);

        Pane colorizeButtonPane = new StackPane(colorize);
        Pane stopColorizeButtonPane = new StackPane(stopColorize);

        EventHandler enableColorizeButton = (EventHandler) (Event event1) -> {

            colorsGame.setEnableColorization(false);
        };

        disableColorizeButton = (EventHandler) (Event event1) -> {

            colorsGame.setEnableColorization(true);
        };

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
        root.getChildren().add(colorizeButtonIndicator);

        Pane colorPane = new StackPane(colorizeButtonPane, stopColorizeButtonPane);

        return colorPane;
    }

    private Stage buildCustomColorDialog() {

        final Stage dialog = new Stage();

        final Translator translator = GazePlay.getInstance().getTranslator();

        dialog.initOwner(GazePlay.getInstance().getPrimaryStage());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(
                windowEvent -> GazePlay.getInstance().getPrimaryStage().getScene().getRoot().setEffect(null));
        dialog.setTitle(translator.translate("customColorDialogTitle"));
        dialog.setAlwaysOnTop(true);

        CustomColorPicker = new CustomColorPicker(root, this, customBox, dialog);

        final Scene scene = new Scene(CustomColorPicker, Color.TRANSPARENT);

        final Configuration config = Configuration.getInstance();
        CssUtil.setPreferredStylesheets(config, scene);

        dialog.setScene(scene);

        return dialog;
    }

    public AbstractGazeIndicator getProgressIndicator() {
        return progressIndicator;
    }
}
