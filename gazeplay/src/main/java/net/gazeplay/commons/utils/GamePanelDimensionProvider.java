package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GamePanelDimensionProvider {

    private final Pane pane;

    public Dimension2D getDimension2D() {
        return new Dimension2D(pane.getWidth(), pane.getHeight());
    }

}
