package net.gazeplay.commons;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Version of GazePlay, read from the manifest of the jar (Implementation-Title and Implementation-Version, written by
 * the build).
 */
@Slf4j
public class VersionInfo {

    public static final String artifactId = "gazeplay";

    /**
     * @return the version of GazePlay, or "Current Version" when it is not run from a built jar (e.g. from an IDE)
     */
    public static String findVersionInfo() {
        return findVersionInfo(artifactId).orElse("Current Version");
    }

    /**
     * @return the version written in the manifest of the jar of this class, if its title contains {@code
     *   applicationName}
     */
    public static Optional<String> findVersionInfo(final String applicationName) {
        return locateVersionInfo(applicationName);
    }

    private static Optional<String> locateVersionInfo(final String applicationName) {
        final Package resources = VersionInfo.class.getPackage();
        final String implementationTitle = resources.getImplementationTitle();
        if (implementationTitle != null && implementationTitle.contains(applicationName)) {
            final String implementationVersion = resources.getImplementationVersion();
            return Optional.of(implementationVersion);
        }
        return Optional.empty();
    }
}
