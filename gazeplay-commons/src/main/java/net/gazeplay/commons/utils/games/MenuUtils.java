package net.gazeplay.commons.utils.games;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import net.gazeplay.commons.VersionInfo;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class MenuUtils {

    public static MenuBar buildMenuBar() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String licenseFileAsString = loadLicenseFileAsString(classLoader);

        @Nullable
        Optional<String> versionNumber = VersionInfo.findVersionInfo(VersionInfo.artifactId);

        MenuItem licenseMenuItem = new MenuItem(licenseFileAsString);

        Menu versionMenu = new Menu(versionNumber.map(value -> "GazePlay " + value).orElse("GazePlay unreleased version"));
        versionMenu.getItems().add(licenseMenuItem);

        String username = ActiveConfigurationContext.getInstance().getUserName();
        Menu profileMenu = new Menu("Current Profile: " + (username.isEmpty() ? "Default" : username));

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(versionMenu, profileMenu);
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
