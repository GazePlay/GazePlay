package net.gazeplay.games.colors;

import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

/**
 *
 * @author Thomas MEDARD
 */
public class ColorBox extends ToggleButton {
    
    @Getter
    private final Color color;
    
    private Boolean selected;
    
    public static final double COLOR_BOX_WIDTH_PX  = 200;
    public static final double COLOR_BOX_HEIGHT_PX = 100;

    public ColorBox(final Color color) {
        super("");
        
        Rectangle graphic = new Rectangle(COLOR_BOX_WIDTH_PX, COLOR_BOX_HEIGHT_PX, color);
        this.setGraphic(graphic);
        
        this.color = color;
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
}
