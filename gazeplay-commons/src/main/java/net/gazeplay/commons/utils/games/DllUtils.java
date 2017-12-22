package net.gazeplay.commons.utils.games;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;

@Slf4j
public class DllUtils {

    @Getter
    private static final DllUtils singleton = new DllUtils();

    private static final String DLL_DIRECTORY_NAME = "DLL";

    protected File locateDLLDirectoryInDist() {
        File workingDirectory = new File(".").getAbsoluteFile();
        log.info("workingDirectory = {}", workingDirectory);

        String executableJarPath = System.getProperty("sun.java.command");
        log.info("executableJarPath = {}", executableJarPath);

        File executableJarFile = new File(workingDirectory, executableJarPath);
        log.info("executableJarFile = {}", executableJarFile);

        if (!executableJarFile.exists()) {
            throw new IllegalStateException("File not found " + executableJarFile);
        }

        File distDirectory = executableJarFile.getParentFile().getParentFile();
        log.info("distDirectory = {}", distDirectory);

        File dllDirectory = new File(distDirectory, DLL_DIRECTORY_NAME);
        log.info("dllDirectory = {}", dllDirectory);

        return dllDirectory;
    }

    protected boolean loadDllAsResource(ClassLoader classLoader, String resourceName) {
        URL dllResourceUrl = classLoader.getResource(resourceName);
        log.info("dllResourceUrl = {}", dllResourceUrl);
        if (dllResourceUrl != null) {
            String dllFilePath = dllResourceUrl.getFile();
            log.info("Attempting to load DLL " + dllFilePath);
            System.load(dllFilePath);
            return true;
        }
        return false;
    }

    protected boolean loadDllAsFile(File dllDirectory, String resourceName) {
        File dllFile = new File(dllDirectory, resourceName);
        if (dllFile.exists()) {
            String dllFilePath = dllFile.getAbsolutePath();
            log.info("Attempting to load DLL " + dllFilePath);
            System.load(dllFilePath);
            return true;
        }
        return false;
    }

    protected boolean isWindowsOperatingSystem() {
        return System.getProperty("os.name").indexOf("indow") > 0;
    }

}
