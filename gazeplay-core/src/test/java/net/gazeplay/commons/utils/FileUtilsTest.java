package net.gazeplay.commons.utils;


import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilsTest {

    @Test
    void shouldDeleteShallowDirectory() throws IOException {
        File parent = new File("parent");
        parent.mkdir();
        File test = new File("parent/test.txt");
        test.createNewFile();

        assertTrue(parent.isDirectory());
        assertTrue(test.isFile());

        FileUtils.deleteDirectoryRecursively(parent);

        assertFalse(parent.isDirectory());
        assertFalse(test.isFile());
    }

    @Test
    void shouldDeleteSingleFile() throws IOException {
        File test = new File("test.txt");
        test.createNewFile();

        assertTrue(test.isFile());

        FileUtils.deleteDirectoryRecursively(test);

        assertFalse(test.isFile());
    }

    @Test
    void shouldDeleteDeepDirectory() throws IOException {
        File parent = new File("parent/child/deep");
        parent.mkdirs();
        File test = new File("parent/child/deep/test.txt");
        test.createNewFile();
        File test2 = new File("parent/child/test2.txt");
        test2.createNewFile();
        File test3 = new File("parent/test3.txt");
        test3.createNewFile();

        assertTrue(parent.isDirectory());
        assertTrue(test.isFile());
        assertTrue(test2.isFile());
        assertTrue(test3.isFile());

        FileUtils.deleteDirectoryRecursively(new File("parent"));

        assertFalse(parent.isDirectory());
        assertFalse(test.isFile());
        assertFalse(test2.isFile());
        assertFalse(test3.isFile());
    }
}
