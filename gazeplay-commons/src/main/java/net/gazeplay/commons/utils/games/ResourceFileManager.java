package net.gazeplay.commons.utils.games;

import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public class ResourceFileManager {

    /**
     * @return the images found under the given resource folder (recursively), in every folder and jar of the
     * classpath, as paths relative to the classpath with "/" separators
     */
    public static Set<String> getResourcePaths(String path) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return getResourcePaths(path, loader != null ? loader : ResourceFileManager.class.getClassLoader());
    }

    /**
     * Scans the classpath with the standard API: it replaces Reflections (unmaintained), which was only used here.
     */
    static Set<String> getResourcePaths(String path, ClassLoader loader) {
        String folder = path.replace('\\', '/').replaceAll("^/+|/+$", "");
        Pattern images = Pattern.compile(createExtensionRegex(ImageUtils.supportedFilesExtensions));
        Set<String> result = new HashSet<>();
        try {
            Enumeration<URL> roots = loader.getResources(folder);
            while (roots.hasMoreElements()) {
                URL root = roots.nextElement();
                if ("file".equals(root.getProtocol())) {
                    addFolderResources(Path.of(root.toURI()), folder, images, result);
                } else {
                    URLConnection connection = root.openConnection();
                    if (connection instanceof JarURLConnection) {
                        connection.setUseCaches(false);
                        try (JarFile jar = ((JarURLConnection) connection).getJarFile()) {
                            addJarResources(jar, folder, images, result);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            log.warn("Cannot list the resources of {}", folder, e);
        }
        return result;
    }

    private static void addFolderResources(Path root, String folder, Pattern images, Set<String> result) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(Files::isRegularFile)
                .filter(file -> images.matcher(String.valueOf(file.getFileName())).matches())
                .forEach(file -> result.add(folder + "/" + root.relativize(file).toString().replace('\\', '/')));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private static void addJarResources(JarFile jar, String folder, Pattern images, Set<String> result) {
        String prefix = folder + "/";
        for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!entry.isDirectory() && name.startsWith(prefix)
                && images.matcher(name.substring(name.lastIndexOf('/') + 1)).matches()) {
                result.add(name);
            }
        }
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

            if (parent.equals("") || filename.equals(""))
                return Collections.emptySet();

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
            if (it.hasNext())
                validExtensions.append(extension).append("|");
            else
                validExtensions.append(extension);
        }

        return ".*\\.(?i)(" + validExtensions + ")";
    }
}
