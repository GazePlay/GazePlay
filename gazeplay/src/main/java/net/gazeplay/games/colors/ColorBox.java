package net.gazeplay.games.colors;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class ColorBox extends ToggleButton {
    
    @Getter
    private Color color;
    
    private Boolean selected;
    
    private final ColorToolBox toolBox;
    
    public static final double COLOR_BOX_WIDTH_PX = 200;
    
    private final Rectangle graphic;
    
    public ColorBox(final Color color, final Pane root, final ColorToolBox toolBox) {
        super("");
        
        this.toolBox = toolBox;
        
        graphic = new Rectangle(COLOR_BOX_WIDTH_PX, computeHeight(), color);
        this.setGraphic(graphic);
        
        this.color = color;
        
        root.heightProperty().addListener((observable) -> {
            
            double newHeight = this.computeHeight();
            log.info("new Height = {}", newHeight);
            graphic.setHeight(newHeight);
        });
        
        toolBox.getColorsGame().getGameContext().getGazeDeviceManager().addEventFilter(this);
        
        ColorEventHandler eventHandler = new ColorEventHandler(this);
        
        this.addEventHandler(MouseEvent.ANY, eventHandler);
        this.addEventHandler(GazeEvent.ANY, eventHandler);
    }
    
    /**
     * Automatically compute free space in the tool box.
     * @return The computed height that every color box should have
     */
    private double computeHeight() {
        
        javafx.geometry.Dimension2D dimension2D = toolBox.getColorsGame().getGameContext().getGamePanelDimensionProvider().getDimension2D();
        
        double totalHeight = dimension2D.getHeight() * ColorToolBox.HEIGHT_POURCENT;
        
        // Compute free space taking into account every elements in the tool box
        double freeSpace = totalHeight - (ColorToolBox.MAIN_INSETS.getTop()
                + ColorToolBox.MAIN_INSETS.getBottom() + ColorToolBox.SPACING_PX + 
                toolBox.getImageManager().getHeight()) + 
                toolBox.getColorziationPane().getHeight();
        
        // + 1 for the curstom color box
        return freeSpace / (ColorToolBox.NB_COLORS_DISPLAYED + 1);
    }
    
    public void select(Rectangle test) {
        
        test.setFill(color);
    }
    
    public void select() {
        
        this.setSelected(true);
    }
    
    public void unselect() {
        
        this.setSelected(false);
    }
    
    public void setColor(final Color color) {
        this.color = color;
        this.graphic.setFill(color);
    }
    
    private class ColorEventHandler implements EventHandler<Event> {
        
        private final ColorBox colorBox;
        /**
         * The gaze progress indicator to show time before colorization.
         */
        private GazeProgressIndicator gazeProgressIndicator;
        
        public ColorEventHandler(final ColorBox colorBox) {
            this.colorBox = colorBox;
            
            Configuration config = toolBox.getColorsGame().getConfig();
            this.gazeProgressIndicator = new GazeProgressIndicator(colorBox.getWidth(), colorBox.getHeight(), config.getFixationlength());
            
            gazeProgressIndicator.minWidthProperty().bind(colorBox.widthProperty());
            gazeProgressIndicator.minHeightProperty().bind(colorBox.heightProperty());
        }
        
        @Override
        public void handle(Event event) {
            
            ColorBox selectedColorBox = toolBox.getSelectedColorBox();
            
            // If already selected, then do nothing
            if (selectedColorBox.equals(colorBox)) {
                return;
            }
            
            if(event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                
                action(selectedColorBox);
            }
            else if(event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                
                // Translate here. We can't do it in constructor because we need to wait for Node to be placed.
                gazeProgressIndicator.setTranslateX(colorBox.getLayoutX());
                gazeProgressIndicator.setTranslateY(colorBox.getLayoutY());
                
                gazeProgressIndicator.setOnFinish((ActionEvent event1) -> {
                    
                    action(selectedColorBox);
                });
                
                gazeProgressIndicator.start();
            }
            else if (event.getEventType() == MouseEvent.MOUSE_EXITED || event.getEventType() == GazeEvent.GAZE_EXITED) {
                
                gazeProgressIndicator.stop();
            }
        }
        
        private void action(ColorBox selectedColorBox) {
            
            selectedColorBox.setSelected(false);
            selectedColorBox.unselect();
            
            colorBox.setSelected(true);
            colorBox.select();
            
            toolBox.setSelectedColorBox(colorBox);
        }
    }
}
