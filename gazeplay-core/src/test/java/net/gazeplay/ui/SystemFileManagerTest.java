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

    /**
     * The address goes whole to the browser, its "#" included (the GazePlay Eval website).
     */
    @Test
    void shouldOpenAWebPageWithTheBrowserOfEachSystem() {
        String url = "https://lig-gazeplay.imag.fr/#/home";

        assertEquals(List.of("rundll32", "url.dll,FileProtocolHandler", url),
            SystemFileManager.openWebPageCommand("Windows 11", url));
        assertEquals(List.of("open", url), SystemFileManager.openWebPageCommand("Mac OS X", url));
        assertEquals(List.of("xdg-open", url), SystemFileManager.openWebPageCommand("Linux", url));
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
