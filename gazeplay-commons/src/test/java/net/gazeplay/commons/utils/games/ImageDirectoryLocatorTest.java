package net.gazeplay.commons.utils.games;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class ImageDirectoryLocatorTest {

    private static String folderName = "test_music_folder";
    private static int numberOfFiles = 10;

    private static String FILESEPARATOR = File.separator;

    @BeforeAll
    static void createMockImageFolder() throws IOException {
        new File(folderName).mkdir();
        for (int i = 0; i < numberOfFiles; i++) {
            new File(folderName + FILESEPARATOR + i + ".jpg").createNewFile();
        }
    }

    @AfterAll
    static void removeMockImageFolder() {
        for (int i = 0; i < numberOfFiles; i++) {
            new File(folderName + FILESEPARATOR + i + ".jpg").delete();
        }
        new File(folderName).delete();
    }

    @Test
    void canLocateImagesDirectoryInUnpackedDistDirectory() {
        File expected = new File("." + FILESEPARATOR, folderName);
        File result = ImageDirectoryLocator.locateImagesDirectoryInUnpackedDistDirectory(folderName);
        assert (result.equals(expected));
    }

    @Test
    void canLocateImagesDirectoryInExplodedClassPath() {
        String pathName = "data/common/default";
        File result = ImageDirectoryLocator.locateImagesDirectoryInExplodedClassPath(pathName);
        assert (result.getAbsolutePath().contains("data" + FILESEPARATOR + "common" + FILESEPARATOR + "default"));
    }
}
