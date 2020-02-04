package net.gazeplay.commons.utils.games;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

@Slf4j
class ImageDirectoryLocatorTest {

    private static final String folderName = "test_music_folder";
    private static final int numberOfFiles = 10;

    private static final String FILESEPARATOR = File.separator;

    @BeforeAll
    static void createMockImageFolder() throws IOException {
        boolean created = new File(folderName).mkdir();
        for (int i = 0; i < numberOfFiles; i++) {
            created = new File(folderName + FILESEPARATOR + i + ".jpg").createNewFile();
        }
        log.info("Created Mock Folder: {}", created);
    }

    @AfterAll
    static void removeMockImageFolder() {
        boolean deleted;
        for (int i = 0; i < numberOfFiles; i++) {
            deleted = new File(folderName + FILESEPARATOR + i + ".jpg").delete();
        }
        deleted = new File(folderName).delete();
        log.info("Deleted Mock Folder: {}", deleted);
    }

    @Test
    void canLocateImagesDirectoryInUnpackedDistDirectory() {
        final File expected = new File("." + FILESEPARATOR, folderName);
        final File result = ImageDirectoryLocator.locateImagesDirectoryInUnpackedDistDirectory(folderName);
        assert (result.equals(expected));
    }

    @Test
    void canLocateImagesDirectoryInExplodedClassPath() {
        final String pathName = "data/common/default";
        final File result = ImageDirectoryLocator.locateImagesDirectoryInExplodedClassPath(pathName);
        assert (result.getAbsolutePath().contains("data" + FILESEPARATOR + "common" + FILESEPARATOR + "default"));
    }
}
