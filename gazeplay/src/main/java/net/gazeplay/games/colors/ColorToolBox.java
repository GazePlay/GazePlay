/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.colors;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;

@Slf4j
public class ColorToolBox extends BorderPane {

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

    public static final String COLORS_IMAGES_PATH = "data/colors/images/";

    public static final String COLORIZE_BUTTON_IMAGE_NAME = COLORS_IMAGES_PATH + "palette.png";
    public static final String STOP_COLORIZE_BUTTON_IMAGE_NAME = "data/common/images/error.png";

    private final VBox mainPane;

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

    @Getter
    @Setter
    private ColorBox selectedColorBox;

    @Getter
    private final Pane imageManager;

    @Getter
    private final Pane colorziationPane;

    public ColorToolBox(final Pane root, final ColorsGame colorsGame) {
        super();

        this.selectedColorBox = null;
        this.colorsGame = colorsGame;
        this.root = root;

        // this.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));

        mainPane = new VBox();
        this.setCenter(mainPane);
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

        customBox = new ColorBox((colorPicker.getValue()), root, this, group);

        colorPicker.setOnAction((event) -> {
            customBox.setColor(colorPicker.getValue());
        });
        colorPicker.prefWidthProperty().bind(customBox.widthProperty());

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

        this.setBottom(imageManager);
        this.setTop(colorziationPane);

        this.getStyleClass().add("bg-colored");
    }

    private Pane buildImageManager() {

        final HBox bottomBox = new HBox(7);
        bottomBox.setPadding(new Insets(0, 5, 2, 5));

        final FileChooser imageChooser = new FileChooser();
        configureImageFileChooser(imageChooser);
        imageChooser.setTitle("Choose an image to load");

        final Stage stage = GazePlay.getInstance().getPrimaryStage();

        Button imageChooserButton = new Button("Load image");
        imageChooserButton.setOnAction((event) -> {

            final File imageFile = imageChooser.showOpenDialog(stage);
            if (imageFile != null) {

                Image image = new Image(imageFile.toURI().toString());

                this.colorsGame.updateImage(image);
            }
        });

        final FileChooser imageSaveChooser = new FileChooser();
        configureImageFileSaver(imageSaveChooser);
        imageSaveChooser.setTitle("Save your image");
        Button imageSaverButton = new Button("Save image");
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
                new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("PNG", "*.png"));
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

        Pane customColorPane = new VBox(customBox, colorPicker);
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
        } else {
            colorize = new Button();
        }

        buttonImg = null;
        try {
            buttonImg = new Image(STOP_COLORIZE_BUTTON_IMAGE_NAME, COLORIZE_BUTTONS_SIZE_PX, COLORIZE_BUTTONS_SIZE_PX,
                    false, true);
        } catch (IllegalArgumentException e) {
            log.warn(e.toString() + " : " + STOP_COLORIZE_BUTTON_IMAGE_NAME);
        }

        Button stopColorize;
        if (buttonImg != null) {
            stopColorize = new Button("", new ImageView(buttonImg));
        } else {
            stopColorize = new Button("");
        }

        colorize.setOnAction((event) -> {
            colorsGame.setEnableColorization(false);
            colorize.setVisible(false);
            stopColorize.setVisible(true);
        });

        stopColorize.setOnAction((event) -> {
            colorsGame.setEnableColorization(true);
            stopColorize.setVisible(false);
            colorize.setVisible(true);
        });
        stopColorize.setVisible(false);

        Pane colorPane = new StackPane(colorize, stopColorize);

        return colorPane;
    }
}
