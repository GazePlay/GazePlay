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
import net.gazeplay.components.GazeIndicator;

@Slf4j
public class ColorBox extends StackPane {

    private final ColorToolBox toolBox;

    private final ToggleButton button;

    private final Rectangle graphic;

    private final double colorizeButtonsSizePx;
    private final double colorBoxPadding;

    @Getter
    private Color color;

    @Setter
    private GazeIndicator progressIndicator;

    public ColorBox(final IGameContext gameContext, final Color color, final Pane root, final ColorToolBox toolBox, final ToggleGroup group, final double colorizeButtonsSizePx) {
        super();
        this.colorizeButtonsSizePx = colorizeButtonsSizePx;
        this.toolBox = toolBox;

        progressIndicator = toolBox.getProgressIndicator();

        double initialTotalWidth = gameContext.getGamePanelDimensionProvider().getDimension2D().getWidth() / 5;
        double initialColorBoxWidth = (99.d * initialTotalWidth) / 100;
        colorBoxPadding = initialTotalWidth / 100.d;

        button = new ToggleButton();
        button.setPadding(new Insets(colorBoxPadding, colorBoxPadding, colorBoxPadding, colorBoxPadding));


        graphic = new Rectangle(initialColorBoxWidth, computeHeight(), color);
        graphic.widthProperty().bind(toolBox.widthProperty().multiply(99.d / 100d));
        graphic.widthProperty().addListener((obj, oldval, newval) -> {
            double newClorBoxPadding = toolBox.widthProperty().doubleValue() / 100.d;
            button.setPadding(new Insets(newClorBoxPadding, newClorBoxPadding, newClorBoxPadding, newClorBoxPadding));
        });
        button.setGraphic(graphic);
        button.setOpacity(1);

        gameContext.getGazeDeviceManager().addEventFilter(button);

        this.color = color;

        toolBox.heightProperty().addListener((observable) -> {

            double newHeight = this.computeHeight();
            graphic.setHeight(newHeight);
        });

        ColorEventHandler eventHandler = new ColorEventHandler(this);

        button.addEventHandler(MouseEvent.ANY, eventHandler);
        button.addEventHandler(GazeEvent.ANY, eventHandler);

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
        double freeSpace = totalHeight - ((2 * colorBoxPadding) * (ColorToolBox.NB_COLORS_DISPLAYED)
            + 3 * colorizeButtonsSizePx);


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
                if (!getChildren().contains(progressIndicator)) {
                    getChildren().add(progressIndicator);
                }
                progressIndicator.setMinSize(colorBox.button.getHeight() / 1.5, colorBox.button.getHeight() / 1.5);
                progressIndicator.setOnFinish((ActionEvent event1) -> action());
                progressIndicator.toFront();
                progressIndicator.start();

            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {

                getChildren().remove(progressIndicator);
                progressIndicator.stop();

            }
        }
    }
}
