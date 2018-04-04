package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ImageUtils {

    public static final String FILESEPARATOR = System.getProperties().getProperty("file.separator");

    public static Image[] loadAllImagesInDirectory(String imagesDirectoryPath) {
        final File directoryFile = new File(imagesDirectoryPath);
        List<Image> images = loadAllImagesInDirectory(directoryFile);
        return images.toArray(new Image[images.size()]);
    }

    private static List<Image> loadAllImagesInDirectory(File directoryFile) {
        log.info("Try to find images in folder : {}", directoryFile);
        if (directoryFile.exists()) {
            List<Image> images = getImages(directoryFile);

            if (!images.isEmpty()) {
                log.debug("I found {} images in folder : {}", images.size(), directoryFile);
                return images;
            } else {
                log.info("No image in folder : {}", directoryFile);
                return defaultImages();
            }
        } else {
            log.info("Folder doesn't exist : {}", directoryFile);
            return defaultImages();
        }
    }

    private static List<Image> getImages(File directoryFile) {
        List<Image> result = new ArrayList<>();

        final String[] filenames = directoryFile.list();
        for (String imagePath : filenames) {
            final String fileUrl = "file:" + directoryFile.getAbsoluteFile() + FILESEPARATOR + imagePath;
            boolean added;
            if (!imagePath.startsWith(".") && isImage(fileUrl)) { // Problems with filenames starting with a point on
                // Windows
                result.add(new Image(fileUrl));
                added = true;
            } else {
                added = false;
            }
            log.debug("{} : added = {}", fileUrl, added);
        }

        return result;
    }

    private static List<Image> defaultImages() {
        List<Image> result = new ArrayList<>();
        result.add(loadImage("data/common/default/images/animal-807308_1920.png"));
        result.add(loadImage("data/common/default/images/bulldog-1047518_1920.jpg"));
        result.add(loadImage("data/common/default/images/businessman-607786_1920.png"));
        result.add(loadImage("data/common/default/images/businessman-607834_1920.png"));
        result.add(loadImage("data/common/default/images/crocodile-614386_1920.png"));
        result.add(loadImage("data/common/default/images/goldfish-30837_1280.png"));
        result.add(loadImage("data/common/default/images/graphic_missbone17.gif"));
        result.add(loadImage("data/common/default/images/nurse-37322_1280.png"));
        result.add(loadImage("data/common/default/images/owl-161583_1280.png"));
        result.add(
                loadImage("data/common/default/images/pez-payaso-animales-el-mar-pintado-por-teoalmeyra-9844979.jpg"));
        return result;
    }

    private static Image loadImage(String resourceLocation) {
        try {
            try (InputStream resourceInputStream = ClassLoader.getSystemResourceAsStream(resourceLocation)) {
                return new Image(resourceInputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource " + resourceLocation + " as an Image", e);
        }
    }

    private static boolean isImage(String file) {
        String mimetype = new MimetypesFileTypeMap().getContentType(file);
        log.debug("{} : mimetype = {}", file, mimetype);
        return mimetype.startsWith("image");
    }

}
