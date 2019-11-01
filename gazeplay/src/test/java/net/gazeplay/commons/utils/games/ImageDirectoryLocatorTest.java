package net.gazeplay.commons.utils.games;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class ImageDirectoryLocatorTest {

    private static String folderName = "test_music_folder";
    private static int numberOfFiles = 10;

    @BeforeAll
    static void createMockImageFolder() throws IOException {
        File folder = new File(folderName);
        boolean folderCreated = folder.mkdirs();
        log.debug("folderCreated = {}", folderCreated);
        for (int i = 0; i < numberOfFiles; i++) {
            boolean created = new File(folder, i + ".jpg").createNewFile();
            log.debug("created = {}", created);
        }
    }

    @AfterAll
    static void removeMockImageFolder() {
        File folder = new File(folderName);
        for (int i = 0; i < numberOfFiles; i++) {
            boolean deleted = new File(folder, i + ".jpg").delete();
            log.debug("deleted = {}", deleted);
        }
        boolean deleted = new File(folderName).delete();
        log.debug("deleted = {}", deleted);
    }

    @Test
    void canLocateImagesDirectoryInUnpackedDistDirectory() {
        File expected = new File(".", folderName);
        File result = ImageDirectoryLocator.locateImagesDirectoryInUnpackedDistDirectory(folderName);
        assertEquals(expected, result);
    }

    @Test
    void canLocateImagesDirectoryInExplodedClassPath() {
        String pathName = "data/common/default";
        File result = ImageDirectoryLocator.locateImagesDirectoryInExplodedClassPath(pathName);
        assertTrue(result.getAbsolutePath().contains("data" + "/" + "common" + "/" + "default"));
    }
}
