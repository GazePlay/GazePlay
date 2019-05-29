package net.gazeplay.commons.utils.stats;

import javafx.scene.layout.GridPane;
import javafx.scene.shape.Line;

public class InfoBoxProps {
    private final GridPane infoBox;
    private final Line lineToInfoBox;
    private final String aoiID;
    private final long TTFF;
    private final long TimeSpent;
    private final long Fixation;

    public InfoBoxProps(GridPane infoBox, Line lineToInfoBox, String aoiID, long TTFF, long TimeSpent, long Fixation) {
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

    public long getTTFF() {
        return this.TTFF;
    }

    public long getTimeSpent() {
        return this.TimeSpent;
    }

    public long getFixation() {
        return this.Fixation;
    }

}
