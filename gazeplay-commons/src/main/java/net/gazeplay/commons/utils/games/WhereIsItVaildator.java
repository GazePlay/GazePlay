package net.gazeplay.commons.utils.games;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class WhereIsItVaildator {

    public static int getNumberOfValidDirectories(String selectedPath, List<File> imagesFolders) {
        final File imagesDirectory = new File(selectedPath + "/images/");
        int filesCount = 0;
        File[] listOfTheFiles = imagesDirectory.listFiles();
        if (listOfTheFiles != null) {
            for (File f : listOfTheFiles) {
                File[] filesInf = f.listFiles();
                if (filesInf != null) {
                    if (f.isDirectory() && filesInf.length > 0) {
                        boolean containsImage = false;
                        int i = 0;
                        while (!containsImage && i < filesInf.length) {
                            File file = filesInf[i];
                            containsImage = fileIsImageFile(file);
                            i++;
                        }
                        if (containsImage) {
                            imagesFolders.add(f);
                            filesCount++;
                        }
                    }
                }
            }
        }
        return filesCount;
    }

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
