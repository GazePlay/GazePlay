package net.gazeplay.commons.utils.stats;

import javafx.scene.layout.GridPane;
import javafx.scene.shape.Shape;

public class InitialAreaOfInterestProps {
    private final Shape areaOfInterest;
    private final GridPane informationBox;

    public InitialAreaOfInterestProps(Shape areaOfInterest, GridPane informationBox) {
        this.areaOfInterest = areaOfInterest;
        this.informationBox = informationBox;
    }

    public Shape getAreaOfInterest() {
        return this.areaOfInterest;
    }

    public GridPane getInformationBox() {
        return this.informationBox;
    }
}
