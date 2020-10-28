package net.gazeplay.commons;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class VersionInfo {

    public static final String artifactId = "gazeplay";

    public static String findVersionInfo() {
        return findVersionInfo(artifactId).orElse("Current Version");
    }

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
