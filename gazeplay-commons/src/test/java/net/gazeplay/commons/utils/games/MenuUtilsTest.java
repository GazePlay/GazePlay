package net.gazeplay.commons.utils.games;

import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import mockit.MockUp;
import net.gazeplay.commons.VersionInfo;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class MenuUtilsTest {

    @Mock
    private Configuration mockConfiguration;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenVersionIsUnknownGivenProfileIsDefaultShouldBuildMenuBar() {
        new MockUp<VersionInfo>() {
            @mockit.Mock
            public Optional<String> findVersionInfo(String id, boolean time) {
                return Optional.empty();
            }
        };
        MenuBar menuBar = MenuUtils.buildMenuBar();
        assertEquals("GazePlay unreleased version", menuBar.getMenus().get(0).getText());
        assertEquals("Current Profile: Default", menuBar.getMenus().get(1).getText());
    }

//    @Test
//    void givenVersionIsKnown_givenProfileIsNamed_shouldBuildMenuBar() {
//        when(mockConfiguration.getUserName()).thenReturn("test user name");
//        new MockUp<ActiveConfigurationContext>() {
//            @mockit.Mock
//            public Configuration getInstance() {
//                return mockConfiguration;
//            }
//        };
//        new MockUp<VersionInfo>() {
//            @mockit.Mock
//            public Optional<String> findVersionInfo(String id, boolean time) {
//                return Optional.of("v1.2.3");
//            }
//        };
//
//        MenuBar menuBar = MenuUtils.buildMenuBar();
//        assertEquals("GazePlay v1.2.3", menuBar.getMenus().get(0).getText());
//        assertEquals("Current Profile: test user name", menuBar.getMenus().get(1).getText());
//    }

    @Test
    void givenLicenseFileIsFoundShouldCreateLicenseMenu() {
        MenuBar menuBar = MenuUtils.buildMenuBar();
        MenuItem license = menuBar.getMenus().get(0).getItems().get(0);
        assertTrue(license.getText().contains("test licence"));
    }
}
