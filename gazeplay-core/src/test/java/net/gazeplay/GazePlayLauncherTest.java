package net.gazeplay;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Started from the shortcuts of the Windows installer, GazePlay Learning has no terminal, and neither have its games.
 */
class GazePlayLauncherTest {

    @Test
    void shouldRecognizeJavaw() {
        assertTrue(GazePlayLauncher.isJavaw("C:\\Program Files\\GazePlay Learning\\lib\\jre\\bin\\javaw.exe"));
        assertTrue(GazePlayLauncher.isJavaw("C:\\JRE\\BIN\\JAVAW.EXE"));
        assertFalse(GazePlayLauncher.isJavaw("C:\\Program Files\\GazePlay Learning\\lib\\jre\\bin\\java.exe"));
        assertFalse(GazePlayLauncher.isJavaw("/usr/lib/jvm/bin/java"));
    }

    @Test
    void shouldStartTheGameWithTheSameKindOfJava() {
        String bin = "jre" + File.separator + "bin" + File.separator;
        assertEquals(bin + "javaw", GazePlayLauncher.javaProgram("jre", true));
        assertEquals(bin + "java", GazePlayLauncher.javaProgram("jre", false));
    }
}
