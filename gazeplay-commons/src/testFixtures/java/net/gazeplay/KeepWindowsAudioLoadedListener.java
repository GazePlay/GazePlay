package net.gazeplay;

import net.gazeplay.commons.utils.WindowsAudioLibrary;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/**
 * Keeps the audio library of Windows loaded in each JVM of tests of the modules using these fixtures, before any test,
 * as {@code GazePlayLauncher} does in the application: the JVM of the tests used to stop without a report, now and then,
 * in this library (see {@link WindowsAudioLibrary}). Found by JUnit through
 * META-INF/services/org.junit.platform.launcher.LauncherSessionListener.
 */
public class KeepWindowsAudioLoadedListener implements LauncherSessionListener {

    @Override
    public void launcherSessionOpened(final LauncherSession session) {
        WindowsAudioLibrary.keepLoaded();
    }
}
