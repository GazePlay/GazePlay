package net.gazeplay.commons.utils.games;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class ImageDirectoryLocatorTest {

    private static String folderName = "test_music_folder";
    private static int numberOfFiles = 10;

    @BeforeAll
    static void createMockImageFolder() throws IOException {
        File folder = new File(folderName);
        folder.mkdirs();
        for (int i = 0; i < numberOfFiles; i++) {
            new File(folder, i + ".jpg").createNewFile();
        }
    }

    @AfterAll
    static void removeMockImageFolder() {
        File folder = new File(folderName);
        for (int i = 0; i < numberOfFiles; i++) {
            new File(folder, i + ".jpg").delete();
        }
        new File(folderName).delete();
    }

    @Test
    void canLocateImagesDirectoryInUnpackedDistDirectory() {
        File expected = new File(".", folderName);
        File result = ImageDirectoryLocator.locateImagesDirectoryInUnpackedDistDirectory(folderName);
        assert (result.equals(expected));
    }

    @Test
    void canLocateImagesDirectoryInExplodedClassPath() {
        String pathName = "data/common/default";
        File result = ImageDirectoryLocator.locateImagesDirectoryInExplodedClassPath(pathName);
        assert (result.getAbsolutePath().contains("data" + "/" + "common" + "/" + "default"));
    }
}
