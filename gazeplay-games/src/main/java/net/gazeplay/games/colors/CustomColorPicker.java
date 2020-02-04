package net.gazeplay.games.colors;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;

import static net.gazeplay.games.colors.ColorToolBox.COLORIZE_BUTTONS_SIZE_PX;
import static net.gazeplay.games.colors.ColorToolBox.COLORS_IMAGES_PATH;

@Slf4j
public class CustomColorPicker extends Pane {

    public static final Color[] COLOR_LIST = {Color.BURLYWOOD, Color.DARKCYAN, Color.BLUEVIOLET, Color.BROWN,
        Color.CADETBLUE, Color.DARKGRAY, Color.DARKORANGE, Color.GOLD, Color.LIMEGREEN, Color.ROYALBLUE,
        Color.SIENNA, Color.YELLOWGREEN};

    public static final int NB_COLOR_PER_ROW = 5/* ((int) Math.sqrt(COLOR_LIST.length)) */;

    // Credits
    // <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from
    // <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a
    // href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0
    // BY</a></div>
    public static final String CLOSE_CURSTOM_PANEL_IMAGE_PATH = COLORS_IMAGES_PATH + "error.png";

    final GridPane colorGrid;

    @Getter
    private ColorBox selectedColor;

    public CustomColorPicker(final IGameContext gameContext, final Pane root, final ColorToolBox toolBox, final ColorBox representingBox,
                             final Stage stage) {
        super();

        this.getStyleClass().add("bg-colored");

        final VBox mainNode = new VBox();
        mainNode.setSpacing(5);
        mainNode.setAlignment(Pos.CENTER);
        mainNode.setPadding(new Insets(5, 5, 15, 5));
        this.getChildren().add(mainNode);

        this.colorGrid = new GridPane();
        colorGrid.setVgap(5);
        colorGrid.setHgap(5);

        final ToggleGroup colorGroup = new ToggleGroup();

        final AbstractGazeIndicator progressIndicator = new GazeFollowerIndicator(gameContext, this);

        for (int i = 0; i < COLOR_LIST.length / NB_COLOR_PER_ROW; ++i) {

            for (int j = 0; j < NB_COLOR_PER_ROW; ++j) {
                final ColorBox colorBox = new CustomColorBox(gameContext, COLOR_LIST[i * NB_COLOR_PER_ROW + j], root, toolBox, colorGroup,
                    representingBox);
                colorBox.setProgressIndicator(progressIndicator);

                colorGrid.add(colorBox, j, i);
            }
        }


        mainNode.getChildren().add(colorGrid);

        // Send a close request on the window
        final EventHandler<ActionEvent> closeEvent = (ActionEvent event) -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));

        // Close button
        Image buttonImg = null;
        try {
            buttonImg = new Image(CLOSE_CURSTOM_PANEL_IMAGE_PATH, COLORIZE_BUTTONS_SIZE_PX, COLORIZE_BUTTONS_SIZE_PX,
                false, true);
        } catch (final IllegalArgumentException e) {
            log.warn(e.toString() + " : " + CLOSE_CURSTOM_PANEL_IMAGE_PATH);
        }

        final Button closeButton;
        if (buttonImg != null) {
            closeButton = new Button("", new ImageView(buttonImg));
            closeButton.setPrefHeight(buttonImg.getHeight());
        } else {
            closeButton = new Button("X");
            closeButton.setPrefHeight(COLORIZE_BUTTONS_SIZE_PX);
            closeButton.setPrefWidth(COLORIZE_BUTTONS_SIZE_PX);
        }
        closeButton.setOnAction(closeEvent);
        mainNode.getChildren().add(closeButton);

        final AbstractGazeIndicator closeProgressIndic = new GazeFollowerIndicator(gameContext, this);
        closeProgressIndic.setOnFinish(closeEvent);
        closeProgressIndic.addNodeToListen(closeButton,
            toolBox.getColorsGame().getGameContext().getGazeDeviceManager());

        this.getChildren().add(progressIndicator);
        this.getChildren().add(closeProgressIndic);
        progressIndicator.toFront();
    }
}
