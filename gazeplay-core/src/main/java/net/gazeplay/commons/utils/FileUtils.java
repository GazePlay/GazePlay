package net.gazeplay.commons.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * File operations missing from the standard library.
 */
@Slf4j
public class FileUtils {

    /**
     * Deletes a file, or a folder with all its content. Failures are ignored.
     */
    public static void deleteDirectoryRecursively(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectoryRecursively(f);
            }
        }
        boolean deleted = file.delete();
        if (deleted) {
            log.debug("File {} was deleted", file.getAbsolutePath());
        }
    }

}
