package net.gazeplay.commons.utils.games;

import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class UtilsTest {

    @Mock
    private Configuration mockConfiguration;

    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep
            + "src" + sep
            + "test" + sep
            + "resources" + sep;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockConfiguration.getFileDir()).thenReturn(localDataFolder);
        ActiveConfigurationContext.setInstance(mockConfiguration);
    }

    @AfterEach
    void restoreActiveConfiguration() {
        ActiveConfigurationContext.switchToDefaultUser();
    }

    @Test
    void shouldGetInputStream() {
        assertNotNull(Utils.getInputStream("data/biboule/images/gazeplayClassicLogo.png"));
    }

    @Test
    void shouldGetFilesFolder() {
        assertEquals(localDataFolder, Utils.getFilesFolder());
    }

    @Test
    void shouldGetBaseImagesDirectory() {
        assertEquals(new File(localDataFolder, "images"), Utils.getBaseImagesDirectory());
    }

    @Test
    void shouldGetImagesSubdirectory() {
        assertEquals(
            new File(localDataFolder + "/images", "subdirectory"),
            Utils.getImagesSubdirectory("subdirectory")
        );
    }

    @Test
    void shouldConvertWindowsPath() {
        Map<String, String> answers = Map.of(
            "/this/is/already/a/path", "/this/is/already/a/path",
            "a\\windows\\path", "a/windows/path"
        );

        for (Map.Entry<String, String> entry : answers.entrySet()) {
            assertEquals(entry.getValue(), Utils.convertWindowsPath(entry.getKey()));
        }
    }

    @Test
    void shouldGetIsWindows() {
        assertEquals(
            System.getProperty("os.name").toLowerCase().contains("win"),
            Utils.isWindows()
        );
    }
}
