package net.gazeplay.commons.utils.games;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsTest {

    @Test
    void shouldGetInputStream() {
        assertNotNull(Utils.getInputStream("data/biboule/images/gazeplayClassicLogo.png"));
    }

    /**
     * The images of the user: one folder for all the games and all the profiles, in the GazePlay folder, and one for
     * the drawings to color (3/11/2026, instead of the files folder of each profile and its subfolders).
     */
    @Test
    void shouldGiveTheFoldersOfTheImagesOfTheUserInTheGazePlayFolder() {
        final File images = GazePlayDirectories.getImagesDirectory();
        assertEquals(new File(GazePlayDirectories.getGazePlayFolder(), "images"), images);
        assertTrue(images.isDirectory(), "created");
        final File coloring = GazePlayDirectories.getColoringDirectory();
        assertEquals(new File(GazePlayDirectories.getGazePlayFolder(), "coloriages"), coloring);
        assertTrue(coloring.isDirectory(), "created");
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
