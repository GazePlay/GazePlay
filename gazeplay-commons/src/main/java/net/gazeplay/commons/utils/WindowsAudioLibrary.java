package net.gazeplay.commons.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Keeps the audio library of Windows (MMDevAPI.dll) loaded until the end of the process, before any sound.
 * <p>
 * The sounds of JavaFX Media (DirectSound) load it; it can be unloaded again while a thread of Windows (RPC) still calls
 * back into it: the process then stops with an access violation (0xC0000005), without a crash report of the JVM. Seen in
 * October 2026 in the dumps of Windows (CrashDumps) of Connect4, which plays a sound in its first seconds (3 launches
 * out of 40 in a row), and on 4/11/2026 in the JVM of the tests (module MMDevApi.dll in the log of Windows). Loaded once
 * more by Java, the library is never unloaded. Nothing is done on the other systems, nor when the library is missing or
 * cannot be loaded (the sound then works as before).
 */
@Slf4j
public final class WindowsAudioLibrary {

    /**
     * Whether the library has been kept loaded in this process.
     */
    @Getter
    private static volatile boolean kept;

    private static final Object LOCK = new Object();

    private WindowsAudioLibrary() {
    }

    /**
     * Keeps the library loaded; once is enough, calling it again does nothing.
     */
    public static void keepLoaded() {
        if (!System.getProperty("os.name", "").startsWith("Windows")) {
            return;
        }
        synchronized (LOCK) {
            if (kept) {
                return;
            }
            final String systemRoot = System.getenv("SystemRoot");
            final File library = new File(systemRoot == null ? "C:\\Windows" : systemRoot, "System32\\MMDevAPI.dll");
            if (!library.isFile()) {
                log.warn("Audio library of Windows not found: {}", library);
                return;
            }
            try {
                System.load(library.getAbsolutePath());
                kept = true;
                log.info("Audio library of Windows kept loaded: {}", library);
            } catch (final UnsatisfiedLinkError | SecurityException e) {
                log.warn("Audio library of Windows not kept loaded: {}", library, e);
            }
        }
    }
}
