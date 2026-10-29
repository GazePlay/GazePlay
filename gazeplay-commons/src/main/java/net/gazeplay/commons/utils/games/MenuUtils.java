package net.gazeplay.commons.utils.games;

import net.gazeplay.commons.VersionInfo;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * The version and the licence of GazePlay Learning, shown by the menus.
 */
public class MenuUtils {

    /**
     * @return the name and version of the application, e.g. "GazePlay Learning 1.11.10"
     */
    public static String versionText() {
        Optional<String> versionNumber = VersionInfo.findVersionInfo(VersionInfo.artifactId);
        return versionNumber.map(value -> "GazePlay Learning " + value).orElse("GazePlay Learning unreleased version");
    }

    /**
     * @return the licence of GazePlay Learning (data/common/licence.txt), or a message when it cannot be read
     */
    public static String licenseText() {
        return loadLicenseFileAsString(ClassLoader.getSystemClassLoader());
    }

    private static String loadLicenseFileAsString(ClassLoader classLoader) {
        String resourceName = "data/common/licence.txt";
        try (InputStream resourceAsStream = classLoader.getResourceAsStream(resourceName)) {
            return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
        } catch (RuntimeException | IOException e) {
            return "Failed to load the license file";
        }
    }

}
