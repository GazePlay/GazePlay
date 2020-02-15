package net.gazeplay.ui.scenes.userselect;

import mockit.Mock;
import mockit.MockUp;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileContextTest {

    private static String profileRoot = "profiles";
    private static String profileDirectory = "test1";
    private static String exampleFile = "test.txt";
    private static String hiddenDirectory = ".hidden";

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
            @Mock
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
            @Mock
            public File getProfilesDirectory() {
                return new File("bad/path");
            }
        };

        List<String> result = UserProfileContext.findAllUsersProfiles();

        assertEquals(Collections.emptyList(), result);
    }
}
