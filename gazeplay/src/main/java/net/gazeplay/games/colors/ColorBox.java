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
import javafx.stage.Screen;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class ColorBox extends StackPane {

    @Getter
    private Color color;

    private final ColorToolBox toolBox;

    public static final double COLOR_BOX_WIDTH_PX = 200;
    public static final double COLOR_CIRCLE_RADIUS = 2;

    public static final double COLOR_BOX_HEIGHT_REDUCTION_COEFF = 1.3;

    private final AbstractGazeIndicator progressIndicator;

    private final ToggleButton button;

    private final Rectangle graphic;
    // private final Circle graphic;

    public ColorBox(final Color color, final Pane root, final ColorToolBox toolBox, final ToggleGroup group) {
        super();

        final GameContext gameContext = toolBox.getColorsGame().getGameContext();
        this.toolBox = toolBox;

        /*Configuration config = toolBox.getColorsGame().getConfig();
        this.progressIndicator = new GazeProgressIndicator(this.getWidth(), this.getHeight(),
                config.getFixationlength());*/

        progressIndicator = toolBox.getProgressIndicator();
        //progressIndicator.getStyleClass().add("withoutTextProgress");
        
        
        /*progressIndicator.setOnFinish((ActionEvent event1) -> {

                  ColorBox selectedColorBox = toolBox.getSelectedColorBox();
                  action(selectedColorBox);
         });*/

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
        //this.getChildren().add(progressIndicator);
        
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

    public void select(Rectangle test) {

        test.setFill(color);
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
    
    private void action(ColorBox selectedColorBox) {

            selectedColorBox.unselect();

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
            
            GazePlay gazePlay = GazePlay.getInstance();

            double gameWidth = gazePlay.getPrimaryStage().getWidth();
            double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();

            double eventX = 0;
            double eventY = 0;

            if(event.getEventType() == GazeEvent.ANY) {
                GazeEvent gazeEvent = (GazeEvent) event;
                eventX = gazeEvent.getX();
                eventY = gazeEvent.getY();
             }
            else if(event.getEventType() == MouseEvent.ANY) {
                MouseEvent mouseEvent = (MouseEvent) event;
                eventX = mouseEvent.getX();
                eventY = mouseEvent.getY();
            }

            ColorBox selectedColorBox = toolBox.getSelectedColorBox();

            // If already selected, then do nothing
            if (selectedColorBox.equals(colorBox)) {
                return;
            }

            if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {

                action(selectedColorBox);
            } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED
                    || event.getEventType() == GazeEvent.GAZE_ENTERED) {

                  progressIndicator.setOnFinish((ActionEvent event1) -> {

                          action(selectedColorBox);
                  });

                colorBox.progressIndicator.start();
                
                log.info("entered {}", colorBox.toString());
                
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                    || event.getEventType() == GazeEvent.GAZE_EXITED) {

                colorBox.progressIndicator.stop();
                log.info("exited : {}", colorBox.toString());
            }
        }
    }
}
