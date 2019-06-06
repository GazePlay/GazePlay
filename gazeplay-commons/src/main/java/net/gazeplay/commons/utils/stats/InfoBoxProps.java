package net.gazeplay.commons.utils.stats;

import javafx.scene.layout.GridPane;
import javafx.scene.shape.Line;

public class InfoBoxProps {
    private final GridPane infoBox;
    private final Line lineToInfoBox;
    private final String aoiID;
    private final double TTFF;
    private final double TimeSpent;
    private final int Fixation;

    public InfoBoxProps(GridPane infoBox, Line lineToInfoBox, String aoiID, double TTFF, double TimeSpent, int Fixation) {
        this.infoBox = infoBox;
        this.lineToInfoBox = lineToInfoBox;
        this.aoiID = aoiID;
        this.TTFF = TTFF;
        this.TimeSpent = TimeSpent;
        this.Fixation = Fixation;
    }

    public GridPane getInfoBox() {
        return this.infoBox;
    }

    public Line getLineToInfoBox() {
        return this.lineToInfoBox;
    }

    public String getAoiID() {
        return this.aoiID;
    }

    public double getTTFF() {
        return this.TTFF;
    }

    public double getTimeSpent() {
        return this.TimeSpent;
    }

    public int getFixation() {
        return this.Fixation;
    }

}
