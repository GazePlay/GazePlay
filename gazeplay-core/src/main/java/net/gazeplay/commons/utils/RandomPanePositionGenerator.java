package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import lombok.AllArgsConstructor;
import net.gazeplay.GamePanelDimensionProvider;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.components.RandomPositionGenerator;

public class RandomPanePositionGenerator extends RandomPositionGenerator {

    private final GamePanelDimensionProvider gamePanelDimensionProvider;

    public RandomPanePositionGenerator(GamePanelDimensionProvider gamePanelDimensionProvider, ReplayablePseudoRandom randomGenerator) {
        super(randomGenerator);
        this.gamePanelDimensionProvider = gamePanelDimensionProvider;
    }

    @Override
    public Dimension2D getDimension2D() {
        return gamePanelDimensionProvider.getDimension2D();
    }
}
