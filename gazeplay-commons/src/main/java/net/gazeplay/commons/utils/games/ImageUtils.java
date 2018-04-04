package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.util.ArrayList;

@Slf4j
public class ImageUtils {

    public static final String FILESEPARATOR = System.getProperties().getProperty("file.separator");

    public static Image[] loadAllImagesInDirectory(String imagesDirectoryPath) {
        final File directoryFile = new File(imagesDirectoryPath);
        return loadAllImagesInDirectory(directoryFile);
    }

    private static Image[] loadAllImagesInDirectory(File directoryFile) {
        log.info("Try to find images in folder : {}", directoryFile);
        if (directoryFile.exists()) {
            Image[] images = getImages(directoryFile);

            if (images.length != 0) {
                log.debug("I found {} images in folder : {}", images.length, directoryFile);
                return images;
            } else {
                log.info("No image in folder : {}", directoryFile);
                return defaultImage();
            }
        } else {
            log.info("Folder doesn't exist : {}", directoryFile);
            return defaultImage();
        }
    }

    private static Image[] getImages(File directoryFile) {
        return getImages(directoryFile, -1);
    }

    private static Image[] getImages(final File directoryFile, final int nbMax) {
        ArrayList<Image> images = new ArrayList<>();

        final String[] filenames = directoryFile.list();
        for (String imagePath : filenames) {
            final String fileUrl = "file:" + directoryFile.getAbsoluteFile() + FILESEPARATOR + imagePath;
            boolean added;
            if (!imagePath.startsWith(".") && isImage(fileUrl)) { // Problems with filenames starting with a point on
                // Windows
                images.add(new Image(fileUrl));
                added = true;
            } else {
                added = false;
            }
            log.debug("{} : added = {}", fileUrl, added);
        }

        Image[] Timages = obj2im(images.toArray());

        if (nbMax <= 0) {
            return Timages;
        }

        Image[] rimages = new Image[nbMax];

        for (int i = 0; i < nbMax; i++) {
            rimages[i] = Timages[(int) (Math.random() * Timages.length)];
        }

        return rimages;
    }

    private static Image[] defaultImage() {

        Image[] defaultImages = new Image[10];
        defaultImages[0] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/animal-807308_1920.png"));
        defaultImages[1] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/bulldog-1047518_1920.jpg"));
        defaultImages[2] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/businessman-607786_1920.png"));
        defaultImages[3] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/businessman-607834_1920.png"));
        defaultImages[4] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/crocodile-614386_1920.png"));
        defaultImages[5] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/goldfish-30837_1280.png"));
        defaultImages[6] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/graphic_missbone17.gif"));
        defaultImages[7] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/nurse-37322_1280.png"));
        defaultImages[8] = new Image(
                ClassLoader.getSystemResourceAsStream("data/common/default/images/owl-161583_1280.png"));
        defaultImages[9] = new Image(ClassLoader.getSystemResourceAsStream(
                "data/common/default/images/pez-payaso-animales-el-mar-pintado-por-teoalmeyra-9844979.jpg"));
        return defaultImages;
    }

    private static Image[] obj2im(Object[] objects) {

        Image[] images = new Image[objects.length];

        for (int i = 0; i < objects.length; i++) {

            images[i] = (Image) objects[i];
        }

        return images;
    }

    private static boolean isImage(String file) {
        String mimetype = new MimetypesFileTypeMap().getContentType(file);
        log.debug("{} : mimetype = {}", file, mimetype);
        return mimetype.startsWith("image");
    }

}
