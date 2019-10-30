package net.gazeplay.commons.utils.games;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LicenseUtils {

    public static MenuBar buildLicenceMenuBar() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String licenseFileAsString = loadLicenseFileAsString(classLoader);

        MenuItem licenseMenuItem = new MenuItem(licenseFileAsString);

        Menu menu = new Menu("GazePlay");
        menu.getItems().add(licenseMenuItem);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(menu);
        menuBar.setPrefHeight(40);
        menuBar.setPrefWidth(80);

        return menuBar;
    }

    private static String loadLicenseFileAsString(ClassLoader classLoader) {
        String resourceName = "data/common/licence.txt";
        try (InputStream resourceAsStream = classLoader.getResourceAsStream(resourceName)) {
            return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
        } catch (RuntimeException | IOException e) {
            return "Failed to load the license file";
        }
    }

}
