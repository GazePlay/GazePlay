package net.gazeplay.commons.utils.stats;

import javafx.scene.layout.GridPane;
import javafx.scene.shape.Line;
import lombok.Data;

/**
 * Information box of an area of interest on the areas of interest screen: its identifier, the time to its first
 * fixation (TTFF), the time spent in it, its number of samples, and the line joining the box to the area.
 */
@Data
public class InfoBoxProps {
    private final GridPane infoBox;
    private final Line lineToInfoBox;
    private final String aoiID;
    private final double TTFF;
    private final double TimeSpent;
    private final int Fixation;
}
