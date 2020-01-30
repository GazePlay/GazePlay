package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.util.function.Supplier;

public class ScreenDimensionSupplier implements Supplier<Dimension2D> {
    @Override
    public Dimension2D get() {
        Screen primaryScreen = Screen.getPrimary();
        Rectangle2D bounds = primaryScreen.getBounds();
        return new Dimension2D(bounds.getWidth(), bounds.getHeight());
    }
}
