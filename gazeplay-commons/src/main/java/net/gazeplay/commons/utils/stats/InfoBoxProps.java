package net.gazeplay.commons.utils.stats;

import javafx.scene.layout.GridPane;
import javafx.scene.shape.Line;
import lombok.Data;

@Data
public class InfoBoxProps {
    private final GridPane infoBox;
    private final Line lineToInfoBox;
    private final String aoiID;
    private final double TTFF;
    private final double TimeSpent;
    private final int Fixation;
}
