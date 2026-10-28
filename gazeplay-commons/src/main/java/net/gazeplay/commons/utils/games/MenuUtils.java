package net.gazeplay.commons.utils.games;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import net.gazeplay.commons.VersionInfo;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Menu bar shown at the top of the main screens.
 */
public class MenuUtils {

    /**
     * @return a menu bar with the version of GazePlay Learning (opening the licence) and the current profile
     */
    public static MenuBar buildMenuBar() {
        MenuItem licenseMenuItem = new MenuItem(licenseText());

        Menu versionMenu = new Menu(versionText());
        versionMenu.getItems().add(licenseMenuItem);

        String username = ActiveConfigurationContext.getInstance().getUserName();
        Menu profileMenu = new Menu("Current Profile: " + (username.isEmpty() ? "Default" : username));

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(versionMenu, profileMenu);
        menuBar.setPrefHeight(40);
        menuBar.setPrefWidth(80);

        return menuBar;
    }

    /**
     * @return the name and version of the application, e.g. "GazePlay Learning 1.11.10"
     */
    public static String versionText() {
        Optional<String> versionNumber = VersionInfo.findVersionInfo(VersionInfo.artifactId);
        return versionNumber.map(value -> "GazePlay Learning " + value).orElse("GazePlay Learning unreleased version");
    }

    /**
     * @return the licence of GazePlay Learning (data/common/licence.txt), or a message when it cannot be read
     */
    public static String licenseText() {
        return loadLicenseFileAsString(ClassLoader.getSystemClassLoader());
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
