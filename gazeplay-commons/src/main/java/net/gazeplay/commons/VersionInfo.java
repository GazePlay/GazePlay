package net.gazeplay.commons;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class VersionInfo {

    public static final String artifactId = "gazeplay";

    public static String findVersionInfo() {
        return findVersionInfo(artifactId, true).orElse("Current Version");
    }

    public static Optional<String> findVersionInfo(final String applicationName, final boolean includeBuildTime) {
        try {
            return locateVersionInfo(applicationName, includeBuildTime);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load the version info", e);
        }
    }

    private static Optional<String> locateVersionInfo(final String applicationName, final boolean includeBuildTime) throws IOException {
        final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
            .getResources("META-INF/MANIFEST.MF");

        while (resources.hasMoreElements()) {
            final URL manifestUrl = resources.nextElement();
            final Manifest manifest = new Manifest(manifestUrl.openStream());
            final Attributes mainAttributes = manifest.getMainAttributes();
            final String implementationTitle = mainAttributes.getValue("Implementation-Title");

            if (implementationTitle != null && implementationTitle.equals(applicationName)) {
                final String implementationVersion = mainAttributes.getValue("Implementation-Version");
                final StringBuilder resultBuilder = new StringBuilder();

                if (implementationVersion != null) {
                    resultBuilder.append(implementationVersion);
                }

                if (includeBuildTime) {
                    final String buildTime = mainAttributes.getValue("Build-Time");

                    if (buildTime != null) {
                        resultBuilder.append(" (").append(buildTime).append(")");
                    }
                }
                if (resultBuilder.toString().isEmpty()) {
                    return Optional.empty();
                } else {
                    return Optional.of(resultBuilder.toString());
                }
            }
        }
        return Optional.empty();
    }

}
