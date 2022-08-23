package net.gazeplay.commons.utils.games;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class ResourceFileManager {

    public static Set<String> getResourcePaths(String path) {
        String packageName = path.replaceAll("[/\\\\]", ".");
        Reflections reflections = new Reflections(packageName, Scanners.Resources);
        return reflections.getResources(Pattern.compile(createExtensionRegex(ImageUtils.supportedFilesExtensions)));
    }

    public static Set<String> getResourceFolders(String path) {
        Set<String> resources = getResourcePaths(path);
        Set<String> result = new HashSet<>();
        for (String r : resources) {
            String parent = new File(r).getParent();
            result.add(parent);
        }

        return result;
    }

    public static boolean resourceExists(String location) {
        Set<String> resources = getMatchingResources(location);
        return resources.size() > 0;
    }

    public static Set<String> getMatchingResources(String location) {
        try {
            Path path = Paths.get(location);

            Path parentPath = path.getParent();
            Path filenamePath = path.getFileName();

            String parent = parentPath != null ? parentPath.toString() : "";
            String filename = filenamePath != null ? filenamePath.toString() : "";

            if (parent.equals("") || filename.equals("")) {
                return Collections.emptySet();
            }

            Set<String> resources = getResourcePaths(parent);
            resources.removeIf(x -> !x.contains(filename));

            for (String resource : resources) {
                log.debug("ResourceFileManager : Found file = {}", resource);
            }
            return resources;
        } catch (InvalidPathException ie) {
            log.info("Path could not be found: {}", ie.toString());
            return Collections.emptySet();
        }
    }

    static String createExtensionRegex(Set<String> fileExtensions) {
        StringBuilder validExtensions = new StringBuilder();
        for (Iterator<String> it = fileExtensions.iterator(); it.hasNext(); ) {
            String extension = it.next();
            if (it.hasNext()) {
                validExtensions.append(extension).append("|");
            } else {
                validExtensions.append(extension);
            }
        }

        return ".*\\.(?i)(" + validExtensions + ")";
    }
}
