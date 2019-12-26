package net.gazeplay.commons.utils;

import java.awt.*;
import java.util.function.Supplier;

public class RobotSupplier implements Supplier<Robot> {

    @Override
    public Robot get() {
        try {
            return new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

}
