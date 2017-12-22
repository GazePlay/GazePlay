package net.gazeplay.commons.utils.games;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class TobiiDllUtils {

    @Getter
    private static final TobiiDllUtils singleton = new TobiiDllUtils();

    private static final String DLL_DIRECTORY_NAME = "DLL";

    private static final String DLL1_NAME = "tobii_stream_engine.dll";

    private static final String DLL2_NAME = "GazePlayTobiiLibrary2.dll";

    private final DllUtils dllUtils = DllUtils.getSingleton();

    public void loadTobiiDlls() {
        if (!dllUtils.isWindowsOperatingSystem()) {
            log.warn("DLL would only work on Windows Operation System");
            return;
        }

        //
        boolean success;

        success = loadTobiiDllsFromClassPath();

        if (!success) {
            success = loadTobiiDllsFromDLLDirectoryUnderWorkingDirectory();
        }

        if (!success) {
            printWarningMessage();
        }
    }

    public boolean loadTobiiDllsFromClassPath() {
        ClassLoader classLoader = TobiiDllUtils.class.getClassLoader();
        //
        boolean success;
        success = dllUtils.loadDllAsResource(classLoader, DLL1_NAME);
        if (success) {
            success = dllUtils.loadDllAsResource(classLoader, DLL2_NAME);
        }
        return success;
    }

    public boolean loadTobiiDllsFromDLLDirectoryUnderWorkingDirectory() {
        File dllDirectory = dllUtils.locateDLLDirectoryInDist();
        //
        boolean success;
        success = dllUtils.loadDllAsFile(dllDirectory, DLL1_NAME);
        if (success) {
            success = dllUtils.loadDllAsFile(dllDirectory, DLL2_NAME);
        }
        return success;
    }

    protected void printWarningMessage() {
        File dllDirectory = dllUtils.locateDLLDirectoryInDist();

        log.info("******************************************************");
        log.info("If you wish to Use a Tobii 4C or a Tobii EyeX");
        log.info("Please put appropriate DLLs in " + DLL_DIRECTORY_NAME + " folder :");
        log.info(DLL1_NAME + " and " + DLL2_NAME);
        log.info("should be in");
        log.info(dllDirectory.getAbsolutePath());
        log.info("******************************************************");
    }

}
