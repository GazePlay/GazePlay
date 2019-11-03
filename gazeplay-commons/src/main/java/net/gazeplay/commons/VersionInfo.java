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

    public static Optional<String> findVersionInfo(String applicationName, boolean includBuildTime) {
        try {
            return locateVersionInfo(applicationName, includBuildTime);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the version info", e);
        }
    }

    private static Optional<String> locateVersionInfo(String applicationName, boolean includBuildTime) throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
            .getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            URL manifestUrl = resources.nextElement();
            Manifest manifest = new Manifest(manifestUrl.openStream());
            Attributes mainAttributes = manifest.getMainAttributes();
            String implementationTitle = mainAttributes.getValue("Implementation-Title");
            if (implementationTitle != null && implementationTitle.equals(applicationName)) {
                String implementationVersion = mainAttributes.getValue("Implementation-Version");
                StringBuilder resultBuilder = new StringBuilder();
                resultBuilder.append(implementationVersion);
                if (includBuildTime) {
                    String buildTime = mainAttributes.getValue("Build-Time");
                    resultBuilder.append(" (").append(buildTime).append(")");
                }
                return Optional.of(resultBuilder.toString());
            }
        }
        return Optional.empty();
    }

}
