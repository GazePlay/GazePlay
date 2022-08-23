package net.gazeplay.ui.scenes.stats;

import javafx.scene.layout.GridPane;
import javafx.scene.shape.Shape;
import lombok.Data;

@Data
public class AreaOfInterestCombinedView {
    private final Shape areaOfInterest;
    private final GridPane infoBox;
}
