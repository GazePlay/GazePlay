package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ImageUtils {

    public static final String FILESEPARATOR = System.getProperties().getProperty("file.separator");

    public static ImageLibrary createImageLibrary(File directoryFile) {
        return new LazyImageLibrary(directoryFile, new EagerImageLibrary(defaultImages()));
    }

    public static ImageLibrary createImageLibrary(File directoryFile, File defaultDirectoryFile) {
        return new LazyImageLibrary(directoryFile,
                new EagerImageLibrary(defaultImages(), new LazyImageLibrary(defaultDirectoryFile)));
    }

    public static List<Image> loadAllImagesInDirectory(File directoryFile) {
        log.info("Try to find images in folder : {}", directoryFile);
        if (!directoryFile.exists()) {
            log.info("Folder doesn't exist : {}", directoryFile);
            return defaultImages();
        }

        List<Image> images = loadAllImages(directoryFile);

        if (images.isEmpty()) {
            log.info("No image in folder : {}", directoryFile);
            return defaultImages();
        }

        log.debug("I found {} images in folder : {}", images.size(), directoryFile);
        return images;
    }

    public static List<Image> loadAllImages(File directoryFile) {
        List<File> files = listImageFiles(directoryFile);

        return loadAllAsImages(files);
    }

    public static List<File> listImageFiles(File directoryFile) {
        File[] files = directoryFile.listFiles(ImageUtils::isImage);
        if (files == null) {
            files = new File[0];
        }
        return Arrays.asList(files);
    }

    public static List<Image> loadAllAsImages(List<File> imageFiles) {
        List<Image> result = new ArrayList<>();
        for (File currentFile : imageFiles) {
            result.add(new Image(currentFile.toURI().toString()));
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

    private static boolean isImage(File file) {
        String filename = file.getName();
        if (filename.startsWith(".")) {
            // Problems with filenames starting with a dot on Windows
            return false;
        }
        String mimetype = new MimetypesFileTypeMap().getContentType(filename);
        log.debug("{} : mimetype = {}", filename, mimetype);
        return mimetype.startsWith("image");
    }

}
