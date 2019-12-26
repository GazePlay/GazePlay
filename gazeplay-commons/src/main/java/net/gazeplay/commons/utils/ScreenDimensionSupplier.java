package net.gazeplay.commons.utils;

import com.sun.glass.ui.Screen;
import javafx.geometry.Dimension2D;

import java.util.function.Supplier;

public class ScreenDimensionSupplier implements Supplier<Dimension2D> {
    @Override
    public Dimension2D get() {
        Screen mainScreen = Screen.getMainScreen();
        return new Dimension2D(mainScreen.getWidth(), mainScreen.getHeight());
    }
}
