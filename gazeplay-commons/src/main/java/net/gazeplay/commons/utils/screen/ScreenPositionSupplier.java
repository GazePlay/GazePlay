package net.gazeplay.commons.utils.screen;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.util.function.Supplier;

public class ScreenPositionSupplier implements Supplier<Point2D> {

    private final Supplier<Screen> screenSupplier;

    public ScreenPositionSupplier(Supplier<Screen> screenSupplier) {
        this.screenSupplier = screenSupplier;
    }

    @Override
    public Point2D get() {
        Screen screen = screenSupplier.get();
        Rectangle2D bounds = screen.getBounds();
        return new Point2D(bounds.getMinX(), bounds.getMinY());
    }
}
