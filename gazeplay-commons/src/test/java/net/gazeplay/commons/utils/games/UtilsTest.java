package net.gazeplay.commons.utils.games;

import mockit.MockUp;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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
        MockitoAnnotations.openMocks(this);
        when(mockConfiguration.getFileDir()).thenReturn(localDataFolder);
        new MockUp<ActiveConfigurationContext>() {
            @mockit.Mock
            public Configuration getInstance() {
                return mockConfiguration;
            }
        };
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
