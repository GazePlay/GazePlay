package net.gazeplay.commons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The audio library of Windows, kept loaded in the JVM of the tests too.
 */
class WindowsAudioLibraryTest {

    /**
     * Nothing in this test loads the library: on Windows, the listener of the test fixtures has already done it. On the
     * other systems, nothing is done.
     */
    @Test
    void shouldBeKeptLoadedBeforeTheTestsOnWindowsOnly() {
        assertEquals(System.getProperty("os.name", "").startsWith("Windows"), WindowsAudioLibrary.isKept());
    }

    @Test
    void shouldStayKeptWhenAskedAgain() {
        final boolean before = WindowsAudioLibrary.isKept();
        WindowsAudioLibrary.keepLoaded();
        assertEquals(before, WindowsAudioLibrary.isKept());
    }
}
