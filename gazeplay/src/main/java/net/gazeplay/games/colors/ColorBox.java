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
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class ColorBox extends StackPane {

    @Getter
    private Color color;

    private final ColorToolBox toolBox;

    public static final double COLOR_BOX_WIDTH_PX = 200;

    private final GazeProgressIndicator progressIndicator;

    private final ToggleButton button;
    private final Rectangle graphic;

    public ColorBox(final Color color, final Pane root, final ColorToolBox toolBox, final ToggleGroup group) {
        super();

        this.toolBox = toolBox;

        Configuration config = toolBox.getColorsGame().getConfig();
        this.progressIndicator = new GazeProgressIndicator(this.getWidth(), this.getHeight(),
                config.getFixationlength());
        // this.gazeProgressIndicator = new GazeProgressIndicator(50, 50, config.getFixationlength());

        this.widthProperty().addListener((observable) -> {
            progressIndicator.setPrefWidth(this.getWidth() - 10);
        });

        this.heightProperty().addListener((observable) -> {
            progressIndicator.setPrefHeight(this.getHeight() - 10);
        });

        button = new ToggleButton();
        button.setToggleGroup(group);

        graphic = new Rectangle(COLOR_BOX_WIDTH_PX, computeHeight(), color);
        button.setGraphic(graphic);

        this.color = color;

        root.heightProperty().addListener((observable) -> {

            double newHeight = this.computeHeight();
            // log.info("new Height = {}", newHeight);
            graphic.setHeight(newHeight);
        });

        toolBox.getColorsGame().getGameContext().getGazeDeviceManager().addEventFilter(this);

        ColorEventHandler eventHandler = new ColorEventHandler(this);

        this.addEventHandler(MouseEvent.ANY, eventHandler);
        this.addEventHandler(GazeEvent.ANY, eventHandler);

        this.getChildren().add(button);
        this.getChildren().add(progressIndicator);
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
        return freeSpace / (ColorToolBox.NB_COLORS_DISPLAYED + 1);
    }

    public void select(Rectangle test) {

        test.setFill(color);
    }

    public void select() {

        this.button.setSelected(true);
    }

    public void unselect() {

        progressIndicator.stop();
        this.button.setSelected(false);
    }

    public void setColor(final Color color) {
        this.color = color;
        this.graphic.setFill(color);
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

                action(selectedColorBox);
            } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED
                    || event.getEventType() == GazeEvent.GAZE_ENTERED) {

                colorBox.progressIndicator.setOnFinish((ActionEvent event1) -> {

                    action(selectedColorBox);
                });

                colorBox.progressIndicator.start();
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                    || event.getEventType() == GazeEvent.GAZE_EXITED) {

                colorBox.progressIndicator.stop();
            }
        }

        private void action(ColorBox selectedColorBox) {

            selectedColorBox.unselect();

            colorBox.select();

            toolBox.setSelectedColorBox(colorBox);
        }
    }
}
