package net.gazeplay.commons.utils.screen;

import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.util.function.Supplier;

public class ScreenDimensionSupplier implements Supplier<Dimension2D> {

    private final Supplier<Screen> screenSupplier;

    public ScreenDimensionSupplier(Supplier<Screen> screenSupplier) {
        this.screenSupplier = screenSupplier;
    }

    @Override
    public Dimension2D get() {
        Screen screen = screenSupplier.get();
        Rectangle2D bounds = screen.getBounds();
        return new Dimension2D(bounds.getWidth(), bounds.getHeight());
    }
}
