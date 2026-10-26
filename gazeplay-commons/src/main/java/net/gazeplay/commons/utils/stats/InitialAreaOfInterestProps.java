package net.gazeplay.commons.utils.stats;

import javafx.scene.layout.GridPane;
import javafx.scene.shape.Shape;
import lombok.Data;

/**
 * An area of interest given by the game (a target), with its information box, on the areas of interest screen.
 */
@Data
public class InitialAreaOfInterestProps {
    private final Shape areaOfInterest;
    private final GridPane informationBox;
}
