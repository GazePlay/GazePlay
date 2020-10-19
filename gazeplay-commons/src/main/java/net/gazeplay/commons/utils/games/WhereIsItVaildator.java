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
