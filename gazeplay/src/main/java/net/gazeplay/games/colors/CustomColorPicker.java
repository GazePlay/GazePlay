package net.gazeplay.games.colors;

import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class CustomColorPicker extends Pane {
    
    final GridPane colorGrid;
    
    public static final Color[] COLOR_LIST = {Color.ALICEBLUE, Color.BURLYWOOD, Color.DARKCYAN};
    
    public static final int NB_COLOR_PER_ROW = ((int) Math.sqrt(COLOR_LIST.length));
    
    public CustomColorPicker(final Pane root, final ColorToolBox toolBox) {
        super();
        
        this.colorGrid = new GridPane();
        
        ToggleGroup colorGroup = new ToggleGroup();
        
        for(int i = 0; i < COLOR_LIST.length; ++i) {
            
            for (int j = 0; j < NB_COLOR_PER_ROW; ++j) {
                ColorBox colorBox = new ColorBox(COLOR_LIST[i], root, toolBox, colorGroup);
                if(i == 0 && j == 0) {
                    colorBox.select();
                }

                colorGrid.add(colorBox, i, j);
            }
        }
    }
}
