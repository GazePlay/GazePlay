package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@ExtendWith(ApplicationExtension.class)
class ImageUtilsTest {

    private static String folderName = "test_music_folder";
    private static int numberOfFiles = 10;

    private static String FILESEPARATOR = File.separator;

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

    void createMockDataFolder() throws IOException {
        boolean created = new File("data/" + folderName).mkdirs();
        for (int i = 0; i < numberOfFiles; i++) {
            created = new File("data/" + folderName + FILESEPARATOR + i + ".jpg").createNewFile();
        }
        log.info("Created Mock Folder: {}", created);
    }

    void removeMockDataFolder() {
        boolean deleted;
        for (int i = 0; i < numberOfFiles; i++) {
            deleted = new File("data/" + folderName + FILESEPARATOR + i + ".jpg").delete();
        }
        deleted = new File("data/" + folderName).delete();
        log.info("Deleted Mock Folder: {}", deleted);
    }

    @Test
    void canCreateAnImageLibraryFromADirectory() {
        LazyImageLibrary library = (LazyImageLibrary) ImageUtils.createImageLibrary(new File(folderName));
        assert (library.getImagesCount() == numberOfFiles);
    }

    @Test
    void canCreateAnImageLibraryFromResources() {
        LazyImageLibrary result = (LazyImageLibrary) ImageUtils.createDefaultImageLibrary(null);

        assert (result.getImagesCount() > 0);
    }

    @Test
    void canCreateAnImageLibraryFromLocalData() throws IOException {
        createMockDataFolder();

        LazyImageLibrary result = (LazyImageLibrary) ImageUtils.createDefaultImageLibrary(null);

        assert (result.getImagesCount() > 0);

        removeMockDataFolder();
    }

    @Test
    void canCreateACustomImageLibraryFromADirectory() {
        LazyImageLibrary library = new LazyImageLibrary(Utils.getImagesSubDirectory("default"));
        LazyImageLibrary result = (LazyImageLibrary) ImageUtils.createCustomizedImageLibrary(library, "biboule/images");

        assert (result.getImagesCount() > 0);
    }

    @Test
    void canCreateACustomImageLibraryFromLocalData() throws IOException {
        createMockDataFolder();

        LazyImageLibrary library = new LazyImageLibrary(Utils.getImagesSubDirectory("default"));
        LazyImageLibrary result = (LazyImageLibrary) ImageUtils.createCustomizedImageLibrary(library, folderName);

        assert (result.getImagesCount() > 0);

        removeMockDataFolder();
    }

    @Test
    void canListAllImagesInDirectory() {
        List<File> list = ImageUtils.listImageFiles(new File(folderName));
        assert (list.size() == numberOfFiles);
    }

    @Test
    void canLoadAllFilesAsImages() {
        List<File> fileList = ImageUtils.listImageFiles(new File(folderName));
        List<Image> imageList = ImageUtils.loadAllAsImages(fileList);
        assert (imageList.size() == numberOfFiles);
    }

    @Test
    void canLoadAFileAsAnImage() {
        String pathname = (folderName + "/0.jpg");
        Image image = ImageUtils.loadImage(new File(pathname));
        assert (image.getUrl().contains(pathname));
    }
}
