package net.gazeplay.commons.utils.stats;


import javafx.scene.layout.GridPane;
import javafx.scene.shape.Line;

public class InfoBoxProps {
    private final GridPane infoBox;
    private final Line lineToInfoBox;

    public InfoBoxProps(GridPane infoBox , Line lineToInfoBox )
    {
        this.infoBox = infoBox;
        this.lineToInfoBox = lineToInfoBox;
    }
    public GridPane getInfoBox(){ return this.infoBox;}
    public Line getLineToInfoBox(){return this.lineToInfoBox;}

}
