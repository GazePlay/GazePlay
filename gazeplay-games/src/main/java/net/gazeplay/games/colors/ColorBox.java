package net.gazeplay.games.colors;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class ColorBox extends StackPane {

    public static final double COLOR_BOX_WIDTH_PX = 200;

    public static final double COLOR_BOX_PADDING = 5;

    private final ColorToolBox toolBox;

    private final ToggleButton button;

    private final Rectangle graphic;

    @Getter
    private Color color;

    @Setter
    private AbstractGazeIndicator progressIndicator;

    public ColorBox(final IGameContext gameContext, final Color color, final Pane root, final ColorToolBox toolBox, final ToggleGroup group) {
        super();

        this.toolBox = toolBox;

        progressIndicator = toolBox.getProgressIndicator();

        button = new ToggleButton();
        button.setPadding(new Insets(COLOR_BOX_PADDING, COLOR_BOX_PADDING, COLOR_BOX_PADDING, COLOR_BOX_PADDING));

        graphic = new Rectangle(COLOR_BOX_WIDTH_PX, computeHeight(), color);
        button.setGraphic(graphic);

        gameContext.getGazeDeviceManager().addEventFilter(graphic);

        this.color = color;

        toolBox.heightProperty().addListener((observable) -> {

            double newHeight = this.computeHeight();
            graphic.setHeight(newHeight);
        });

        ColorEventHandler eventHandler = new ColorEventHandler(this);

        this.addEventHandler(MouseEvent.ANY, eventHandler);
        this.addEventHandler(GazeEvent.ANY, eventHandler);

        this.getChildren().add(button);

        toolBox.getColorsGame().getGameContext().getGazeDeviceManager().addEventFilter(this);
    }

    public void updateHeight() {
        graphic.setHeight(computeHeight());
    }

    /**
     * Automatically compute free space in the tool box.
     *
     * @return The computed height that every color box should have
     */
    private double computeHeight() {

        javafx.geometry.Dimension2D dimension2D = toolBox.getColorsGame().getGameContext()
            .getGamePanelDimensionProvider().getDimension2D();

        double totalHeight = dimension2D.getHeight();

        // Compute free space taking into account every elements in the tool box
        double freeSpace = totalHeight - ((ColorToolBox.SPACING_PX + 2 * COLOR_BOX_PADDING) * (ColorToolBox.NB_COLORS_DISPLAYED)
            + 3 * ColorToolBox.COLORIZE_BUTTONS_SIZE_PX);


        // + 1 for the curstom color box
        return freeSpace / (ColorToolBox.NB_COLORS_DISPLAYED + 1);
    }

    public void select() {

        this.button.setSelected(true);
        this.button.getStyleClass().add("selectedColorBox");
    }

    public void unselect() {

        progressIndicator.stop();
        this.button.setSelected(false);
        this.button.getStyleClass().remove("selectedColorBox");
    }

    public void setColor(final Color color) {
        this.color = color;
        this.graphic.setFill(color);
    }

    protected void action() {

        toolBox.getSelectedColorBox().unselect();

        this.select();

        toolBox.setSelectedColorBox(this);
    }

    private class ColorEventHandler implements EventHandler<Event> {

        private final ColorBox colorBox;

        public ColorEventHandler(final ColorBox colorBox) {
            this.colorBox = colorBox;

        }

        @Override
        public void handle(Event event) {

            ColorBox selectedColorBox = toolBox.getSelectedColorBox();

            // If already selected, then do nothing
            if (selectedColorBox.equals(colorBox)) {
                return;
            }

            if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {

                action();
            } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED
                || event.getEventType() == GazeEvent.GAZE_ENTERED) {

                progressIndicator.setOnFinish((ActionEvent event1) -> action());
                colorBox.progressIndicator.start();

            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {

                colorBox.progressIndicator.stop();
            }
        }
    }
}
