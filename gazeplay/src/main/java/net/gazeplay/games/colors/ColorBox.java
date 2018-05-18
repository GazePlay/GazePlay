package net.gazeplay.games.colors;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class ColorBox extends StackPane {

    @Getter
    private Color color;

    private final ColorToolBox toolBox;

    public static final double COLOR_BOX_WIDTH_PX = 200;
    public static final double COLOR_CIRCLE_RADIUS = 2;

    public static final double COLOR_BOX_HEIGHT_REDUCTION_COEFF = 1.3;

    @Setter
    private AbstractGazeIndicator progressIndicator;

    private final ToggleButton button;

    private final Rectangle graphic;
    // private final Circle graphic;

    public ColorBox(final Color color, final Pane root, final ColorToolBox toolBox, final ToggleGroup group) {
        super();

        final GameContext gameContext = toolBox.getColorsGame().getGameContext();
        this.toolBox = toolBox;

        progressIndicator = toolBox.getProgressIndicator();

        button = new ToggleButton();
        button.setToggleGroup(group);

        graphic = new Rectangle(COLOR_BOX_WIDTH_PX, computeHeight(), color);
        // graphic = new Circle(COLOR_CIRCLE_RADIUS);
        button.setGraphic(graphic);

        gameContext.getGazeDeviceManager().addEventFilter(graphic);

        this.color = color;

        root.heightProperty().addListener((observable) -> {

            double newHeight = this.computeHeight();
            // log.info("new Height = {}", newHeight);
            graphic.setHeight(newHeight);
            // graphic.setRadius(newHeight);
        });

        ColorEventHandler eventHandler = new ColorEventHandler(this);

        this.addEventHandler(MouseEvent.ANY, eventHandler);
        this.addEventHandler(GazeEvent.ANY, eventHandler);

        this.getChildren().add(button);

        toolBox.getColorsGame().getGameContext().getGazeDeviceManager().addEventFilter(this);
    }

    /**
     * Automatically compute free space in the tool box.
     * 
     * @return The computed height that every color box should have
     */
    private double computeHeight() {

        javafx.geometry.Dimension2D dimension2D = toolBox.getColorsGame().getGameContext()
                .getGamePanelDimensionProvider().getDimension2D();

        double totalHeight = dimension2D.getHeight() * ColorToolBox.HEIGHT_POURCENT;

        // Compute free space taking into account every elements in the tool box
        double freeSpace = totalHeight - (ColorToolBox.MAIN_INSETS.getTop() + ColorToolBox.MAIN_INSETS.getBottom()
                + ColorToolBox.SPACING_PX + toolBox.getImageManager().getHeight())
                + toolBox.getColorziationPane().getHeight();

        // + 1 for the curstom color box
        return freeSpace / ((ColorToolBox.NB_COLORS_DISPLAYED + 1) * COLOR_BOX_HEIGHT_REDUCTION_COEFF);
    }

    private double computeRadius() {

        javafx.geometry.Dimension2D dimension2D = toolBox.getColorsGame().getGameContext()
                .getGamePanelDimensionProvider().getDimension2D();

        double totalHeight = dimension2D.getHeight() * ColorToolBox.HEIGHT_POURCENT;

        // Compute free space taking into account every elements in the tool box
        double freeSpace = totalHeight - (ColorToolBox.MAIN_INSETS.getTop() + ColorToolBox.MAIN_INSETS.getBottom()
                + ColorToolBox.SPACING_PX + toolBox.getImageManager().getHeight())
                + toolBox.getColorziationPane().getHeight();

        // + 1 for the curstom color box
        return freeSpace / ((ColorToolBox.NB_COLORS_DISPLAYED + 1) * COLOR_BOX_HEIGHT_REDUCTION_COEFF);
    }

    public void select() {

        this.button.setSelected(true);
        this.getStyleClass().add("selectedColorBox");
    }

    public void unselect() {

        progressIndicator.stop();
        this.button.setSelected(false);
        this.getStyleClass().remove("selectedColorBox");
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

                progressIndicator.setOnFinish((ActionEvent event1) -> {

                    action();
                });

                colorBox.progressIndicator.start();

                // log.info("entered {}", colorBox.toString());

            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                    || event.getEventType() == GazeEvent.GAZE_EXITED) {

                colorBox.progressIndicator.stop();
                // log.info("exited : {}", colorBox.toString());
            }
        }
    }
}
