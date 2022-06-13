package net.gazeplay.commons.utils.stats;

import javafx.scene.layout.GridPane;
import javafx.scene.shape.Shape;
import lombok.Data;

@Data
public class InitialAreaOfInterest {
    private final Shape areaOfInterest;
    private final GridPane informationBox;
}
