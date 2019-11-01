package net.gazeplay.commons.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class FilesUtility {

    public static void deleteDirectoryRecursivly(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectoryRecursivly(f);
            }
        }
        boolean deleted = file.delete();
        if (deleted) {
            log.debug("File {} was deleted", file.getAbsolutePath());
        }
    }
    
}
