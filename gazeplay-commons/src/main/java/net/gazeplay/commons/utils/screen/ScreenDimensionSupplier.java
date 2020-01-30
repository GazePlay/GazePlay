package net.gazeplay.commons.utils.screen;

import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class ScreenDimensionSupplier implements Supplier<Dimension2D> {

    private final Supplier<Screen> screenSupplier;

    @Override
    public Dimension2D get() {
        Screen primaryScreen = screenSupplier.get();
        Rectangle2D bounds = primaryScreen.getBounds();
        return new Dimension2D(bounds.getWidth(), bounds.getHeight());
    }
}
