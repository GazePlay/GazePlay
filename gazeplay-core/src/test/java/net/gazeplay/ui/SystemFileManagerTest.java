package net.gazeplay.ui;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemFileManagerTest {

    @Test
    void shouldOpenAFolderWithTheFileManagerOfEachSystem() {
        File folder = new File("statistics");
        String path = folder.getAbsolutePath();

        assertEquals(List.of("explorer.exe", path), SystemFileManager.openFolderCommand("Windows 11", folder));
        assertEquals(List.of("open", path), SystemFileManager.openFolderCommand("Mac OS X", folder));
        assertEquals(List.of("xdg-open", path), SystemFileManager.openFolderCommand("Linux", folder));
    }

    @Test
    void shouldShowAFileInItsFolder() {
        File file = new File(new File("statistics"), "2026-10-22-14-23-34-replayData.json");
        String path = file.getAbsolutePath();

        assertEquals(List.of("explorer.exe", "/select," + path), SystemFileManager.showFileCommand("Windows 11", file));
        assertEquals(List.of("open", "-R", path), SystemFileManager.showFileCommand("Mac OS X", file));
        // xdg-open cannot select a file
        assertEquals(List.of("xdg-open", new File("statistics").getAbsolutePath()),
            SystemFileManager.showFileCommand("Linux", file));
    }
}
