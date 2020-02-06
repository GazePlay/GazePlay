package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.List;

@Slf4j
@ExtendWith(ApplicationExtension.class)
class ImageUtilsTest {

    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep
            + "src" + sep
            + "test" + sep
            + "resources" + sep
            + "data" + sep
            + "biboule" + sep
            + "images";


    @Test
    void canCreateAnImageLibraryFromADirectory() {
        final ImageLibrary library = ImageUtils.createImageLibrary(new File(localDataFolder));
        assert (library.getImagesCount() == 1);
    }

    @Test
    void canCreateAnImageLibraryFromResources() {
        final ImageLibrary library = ImageUtils.createDefaultImageLibrary(null);
        assert (library.getImagesCount() == 2);
    }

    @Test
    void canCreateACustomImageLibraryFromADirectory() {
        final ImageLibrary library = ImageUtils.createCustomizedImageLibrary(null, "biboule/images");
        assert (library.getImagesCount() == 1);
    }

    @Test
    void canListAllImagesInDirectory() {
        final List<File> list = ImageUtils.listImageFiles(new File(localDataFolder));
        assert (list.size() == 1);
    }

    @Test
    void canLoadAllFilesAsImages() {
        final List<File> fileList = ImageUtils.listImageFiles(new File(localDataFolder));
        final List<Image> imageList = ImageUtils.loadAllAsImages(fileList);
        assert (imageList.size() == 1);
    }

    @Test
    void canLoadAFileAsAnImage() {
        final String pathname = (localDataFolder + sep + "gazeplayClassicLogo.png");
        final Image image = ImageUtils.loadImage(new File(pathname));
        assert (image.getUrl().contains("gazeplayClassicLogo.png"));
    }
}
