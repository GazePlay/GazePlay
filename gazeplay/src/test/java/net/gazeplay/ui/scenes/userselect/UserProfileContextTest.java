package net.gazeplay.ui.scenes.userselect;

import javafx.scene.paint.ImagePattern;
import mockit.MockUp;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class UserProfileContextTest {

    @Mock
    private Configuration mockConfig;

    private static String profileRoot = "profiles";
    private static String profileDirectory = "test1";
    private static String exampleFile = "test.txt";
    private static String hiddenDirectory = ".hidden";

    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep
            + "src" + sep
            + "test" + sep
            + "resources" + sep;

    @BeforeAll
    static void setupMockProfiles() throws IOException {
        File rootDir = new File(profileRoot);

        File hiddenDir = new File(rootDir, hiddenDirectory);
        hiddenDir.mkdirs();
        Files.setAttribute(hiddenDir.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);

        File profileDir = new File(rootDir, profileDirectory);
        profileDir.mkdirs();

        File realFile = new File(rootDir, exampleFile);
        realFile.createNewFile();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterAll
    static void tearDownMockProfiles() {
        new File(profileRoot, hiddenDirectory).delete();
        new File(profileRoot, profileDirectory).delete();
        new File(profileRoot, exampleFile).delete();
        new File(profileRoot).delete();
    }

    @Test
    void shouldFindAllUsersProfiles() {
        new MockUp<GazePlayDirectories>() {
            @mockit.Mock
            public File getProfilesDirectory() {
                return new File(profileRoot);
            }
        };

        List<String> result = UserProfileContext.findAllUsersProfiles();

        assertTrue(result.contains(profileDirectory));
        assertFalse(result.contains(hiddenDirectory));
        assertFalse(result.contains(exampleFile));
    }

    @Test
    void shouldReturnEmptyListIfNoProfiles() {
        new MockUp<GazePlayDirectories>() {
            @mockit.Mock
            public File getProfilesDirectory() {
                return new File("bad/path");
            }
        };

        List<String> result = UserProfileContext.findAllUsersProfiles();

        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void shouldLookupProfilePicture() {
        when(mockConfig.getUserPicture()).thenReturn(localDataFolder + "bear.jpg");

        ImagePattern result = UserProfileContext.lookupProfilePicture(mockConfig);

        assertNotNull(result.getImage());
    }

    @Test
    void shouldGetDefaultProfilePicture() {
        when(mockConfig.getUserPicture()).thenReturn(null).thenReturn("someOtherFile.png");

        ImagePattern result1 = UserProfileContext.lookupProfilePicture(mockConfig);
        ImagePattern result2 = UserProfileContext.lookupProfilePicture(mockConfig);

        assertTrue(result1.getImage().getUrl().contains("DefaultUser.png"));
        assertTrue(result2.getImage().getUrl().contains("DefaultUser.png"));
    }
}
