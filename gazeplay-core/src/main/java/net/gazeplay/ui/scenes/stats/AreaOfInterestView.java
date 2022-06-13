package net.gazeplay.ui.scenes.stats;

import javafx.scene.layout.GridPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import lombok.Data;

@Data
public class AreaOfInterestView {
    private final GridPane infoBox;
    private final Line lineToInfoBox;
    private final Polygon areaOfInterest;
}
