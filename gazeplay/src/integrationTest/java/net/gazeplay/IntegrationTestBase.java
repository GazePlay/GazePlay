package net.gazeplay;

import org.junit.jupiter.api.BeforeAll;
import org.testfx.framework.junit5.ApplicationTest;

public class IntegrationTestBase extends ApplicationTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        if (Boolean.getBoolean("headless")) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
            System.setProperty("headless.geometry", "1920x1080-32");
        }
    }
}
