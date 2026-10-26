package net.gazeplay.commons.utils.games;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Checks a folder chosen for "Where is it?" with custom images: an "images" folder with one subfolder per question,
 * each holding at least one image.
 */
public class WhereIsItVaildator {

    /**
     * Adds to {@code imagesFolders} each subfolder of {@code selectedPath}/images that holds at least one image.
     *
     * @return the number of folders added
     */
    public static int getNumberOfValidDirectories(String selectedPath, List<File> imagesFolders) {
        final File imagesDirectory = new File(selectedPath + "/images/");
        int filesCount = 0;
        File[] listOfTheFiles = imagesDirectory.listFiles();
        if (listOfTheFiles != null) {
            for (File f : listOfTheFiles) {
                File[] filesInf = f.listFiles();
                if (filesInf != null) {
                    if (f.isDirectory() && filesInf.length > 0) {
                        for (File file : filesInf) {
                            if (fileIsImageFile(file)) {
                                imagesFolders.add(f);
                                filesCount++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return filesCount;
    }

    /**
     * @return whether the system recognises the file as an image (from its content type)
     */
    public static boolean fileIsImageFile(File file) {
        if (file.exists()) {
            try {
                String mimetype = Files.probeContentType(file.toPath());
                if (mimetype != null && mimetype.split("/")[0].equals("image")) {
                    return true;
                }
            } catch (IOException ignored) {

            }
        }
        return false;
    }

}
