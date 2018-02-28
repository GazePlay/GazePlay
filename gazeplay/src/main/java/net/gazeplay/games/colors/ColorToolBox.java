/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.colors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import net.gazeplay.GazePlay;

/**
 *
 * @author medard
 */
public class ColorToolBox extends BorderPane{
    
    /**
     * Pourcents use to compute height and width.
     */
    public static final double WIDTH_POURCENT = 0.25;
    public static final double HEIGHT_POURCENT = 0.8;
    
    public static final double SPACING_PX = 10;
    
    public static final Insets MAIN_INSETS = new Insets(50, 15, 50, 15)
    
    private VBox mainPane;
    
    @Getter
    private ColorBox selectedColorBox;

    public ColorToolBox() {
        super();
        
        final ReadOnlyDoubleProperty height = GazePlay.getInstance().getPrimaryStage().heightProperty();
        final ReadOnlyDoubleProperty width  = GazePlay.getInstance().getPrimaryStage().widthProperty();
        
        height.addListener((observable) -> {
            
            this.setPrefHeight(height.doubleValue() * HEIGHT_POURCENT);
        });
        
        width.addListener((observable) -> {
            
            this.setPrefHeight(width.doubleValue() * WIDTH_POURCENT);
        });
        
        this.selectedColorBox = null;
        
        this.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        
        mainPane = new VBox();
        this.setCenter(mainPane);
        mainPane.setSpacing(SPACING_PX);
        mainPane.setPadding(MAIN_INSETS);
        
        ColorBox colorBox;
        EventHandler<MouseEvent> mouseHandler;
        
        // COLORS
        Color colorArray[] = {Color.AZURE, Color.BEIGE, Color.BLUEVIOLET, 
            Color.CORNFLOWERBLUE, Color.DARKGOLDENROD, Color.DIMGREY, 
            Color.GOLDENROD, Color.RED};
        
        List<Color> colors = new ArrayList<Color>(Arrays.asList(colorArray));
        
        ToggleGroup group = new ToggleGroup();
        
        for(Color color : colors) {
            
            colorBox = new ColorBox(color);
            mouseHandler = new ColorMouseEventHandler(colorBox);
            mainPane.getChildren().add(colorBox);
            colorBox.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseHandler);
            colorBox.setToggleGroup(group);
            
            if(this.selectedColorBox == null) {
                colorBox.select();
                selectedColorBox = colorBox;
            }
        }
        
        this.getStyleClass().add("bg-colored");
    }
    
    private class ColorMouseEventHandler implements EventHandler<MouseEvent> {

        private final ColorBox colorBox;

        public ColorMouseEventHandler(final ColorBox colorBox) {
            this.colorBox = colorBox;
        }
        
        @Override
        public void handle(MouseEvent event) {
            
            // If already selected, then do nothing
            if(selectedColorBox.equals(colorBox)) {
               return; 
            }
            
            selectedColorBox.unselect();
            colorBox.select();
            selectedColorBox = colorBox;
        }
    }
}
