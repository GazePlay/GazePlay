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

@ExtendWith(ApplicationExtension.class)
@Slf4j
class ImageUtilsTest {

    private static String folderName = "test_music_folder";
    private static int numberOfFiles = 10;

    @BeforeAll
    static void createMockImageFolder() throws IOException {
        File folder = new File(folderName);
        boolean folderCreated = folder.mkdirs();
        log.debug("folderCreated = {}", folderCreated);
        for (int i = 0; i < numberOfFiles; i++) {
            File file = new File(folder, i + ".jpg");
            boolean created = file.createNewFile();
            log.debug("created = {}", created);
        }
    }

    @AfterAll
    static void removeMockImageFolder() {
        File folder = new File(folderName);
        for (int i = 0; i < numberOfFiles; i++) {
            File file = new File(folder, i + ".jpg");
            boolean deleted = file.delete();
            log.debug("deleted = {}", deleted);
        }
        boolean deleted = folder.delete();
        log.debug("deleted = {}", deleted);
    }

    void createMockDataFolder() throws IOException {
        File directory = new File("data/" + folderName);
        boolean directoryCreated = directory.mkdirs();
        log.debug("directoryCreated = {}", directoryCreated);
        for (int i = 0; i < numberOfFiles; i++) {
            File file = new File("data/" + folderName + "/" + i + ".jpg");
            boolean created = file.createNewFile();
            log.debug("created = {}", created);
        }
    }

    void removeMockDataFolder() {
        for (int i = 0; i < numberOfFiles; i++) {
            File file = new File("data/" + folderName + "/" + i + ".jpg");
            boolean deleted = file.delete();
            log.debug("deleted = {}", deleted);
        }
        boolean deleted = new File("data/" + folderName).delete();
        log.debug("deleted = {}", deleted);
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
