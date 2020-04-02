package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import lombok.AllArgsConstructor;
import net.gazeplay.GamePanelDimensionProvider;
import net.gazeplay.components.RandomPositionGeneratorGazePlayRandomLib;

@AllArgsConstructor
public class RandomPanePositionGeneratorTest extends RandomPositionGeneratorGazePlayRandomLib {

    private final GamePanelDimensionProvider gamePanelDimensionProvider;

    @Override
    public Dimension2D getDimension2D() {
        return gamePanelDimensionProvider.getDimension2D();
    }
}
