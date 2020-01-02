package net.gazeplay.commons.utils;

import java.awt.*;
import java.util.function.Supplier;

public class RobotSupplier implements Supplier<Robot> {

    public RobotSupplier() {
        System.setProperty("java.awt.headless", "false");
    }

    @Override
    public Robot get() {
        try {
            return new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

}
