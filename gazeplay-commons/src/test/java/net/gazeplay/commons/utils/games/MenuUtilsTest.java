package net.gazeplay.commons.utils.games;

import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import net.gazeplay.commons.VersionInfo;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class MenuUtilsTest {

    @AfterEach
    void restoreActiveConfiguration() {
        ActiveConfigurationContext.switchToDefaultUser();
    }

    @Test
    void givenProfileIsDefaultShouldBuildMenuBar() {
        // The version comes from the jar manifest, which is present or not depending on how the tests are launched
        String expectedVersion = VersionInfo.findVersionInfo(VersionInfo.artifactId)
            .map(version -> "GazePlay Learning " + version)
            .orElse("GazePlay Learning unreleased version");

        MenuBar menuBar = MenuUtils.buildMenuBar();
        assertEquals(expectedVersion, menuBar.getMenus().get(0).getText());
        assertEquals("Current Profile: Default", menuBar.getMenus().get(1).getText());
    }

    @Test
    void givenProfileIsNamedShouldShowItInMenuBar() {
        Configuration mockConfiguration = mock(Configuration.class);
        when(mockConfiguration.getUserName()).thenReturn("test user name");
        ActiveConfigurationContext.setInstance(mockConfiguration);

        MenuBar menuBar = MenuUtils.buildMenuBar();
        assertEquals("Current Profile: test user name", menuBar.getMenus().get(1).getText());
    }

    @Test
    void givenLicenseFileIsFoundShouldCreateLicenseMenu() {
        MenuBar menuBar = MenuUtils.buildMenuBar();
        MenuItem license = menuBar.getMenus().get(0).getItems().get(0);
        assertTrue(license.getText().contains("test licence"));
    }
}
