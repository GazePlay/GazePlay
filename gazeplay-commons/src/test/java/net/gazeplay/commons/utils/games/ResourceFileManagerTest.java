package net.gazeplay.commons.utils.games;

import com.google.common.collect.Sets;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
class ResourceFileManagerTest {

    @Test
    void canGetAllFilesInResourcePackage() {
        Set<String> set = ResourceFileManager.getResourcePaths("data/biboule/images");
        assertEquals(1, set.size());
        set = ResourceFileManager.getResourcePaths("data/common/default/images");
        assertEquals(2, set.size());
        set = ResourceFileManager.getResourcePaths("data/whereisit");
        assertEquals(35, set.size());
        set = ResourceFileManager.getResourcePaths("data");
        assertEquals(38, set.size());
    }

    @Test
    void canGetAllImmediateFoldersInResourcePackage() {
        String sep = File.separator;

        Set<String> set = ResourceFileManager.getResourceFolders("data/biboule/images");
        assert (set.iterator().next().equals(String.format("data%1$sbiboule%1$simages", sep)));
        set = ResourceFileManager.getResourceFolders("data/common/default/images");
        assert (set.iterator().next().equals(String.format("data%1$scommon%1$sdefault%1$simages", sep)));
        set = ResourceFileManager.getResourceFolders("data");
        assert (set.contains(String.format("data%1$scommon%1$sdefault%1$simages", sep)));
        assert (set.contains(String.format("data%1$sbiboule%1$simages", sep)));
    }

    @Test
    void canGetAllFilesInResourcePackageWithBackslashes() {
        Set<String> set = ResourceFileManager.getResourcePaths("data\\biboule\\images");
        assert (set.size() == 1);
        set = ResourceFileManager.getResourcePaths("data\\biboule");
        assert (set.size() == 1);
    }

    @Test
    void canLoadAnImageFromResources() {
        Set<String> set = ResourceFileManager.getResourcePaths("data/biboule/images");
        Image image = new Image(set.iterator().next());
        assert (!image.getUrl().equals(""));
    }

    @Test
    void canConstructRegexForFileExtensions() {
        Set<String> extensions = Collections
            .unmodifiableSet(Sets.newHashSet("jpg", "jpeg", "png"));
        String result = ResourceFileManager.createExtensionRegex(extensions);
        String expected = ".*\\.(?i)(jpg|png|jpeg)";

        assert (expected.equals(result));
    }

    @Test
    void canFindFileInResources() {
        assert (ResourceFileManager.resourceExists("data/biboule/images/gazeplayClassicLogo.png"));
    }

    @Test
    void canRetrieveAllMatchingFiles() {
        Set<String> result = ResourceFileManager.getMatchingResources("data/common/bear.jpg");
        assert (result.size() == 1);
    }

    @Test
    void receiveNullForInvalidPath() {
        Set<String> result = ResourceFileManager.getMatchingResources("wrongpath");
        assert (result.size() == 0);
    }

    /**
     * In the distributions the resources are in jars, not in folders as during the other tests.
     */
    @Test
    void canGetAllFilesInAJar(@TempDir File tempDir) throws IOException {
        File jar = new File(tempDir, "resources.jar");
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar))) {
            for (String entry : List.of("data/", "data/game/", "data/game/images/", "data/game/images/a.png",
                "data/game/images/B.JPG", "data/game/images/notes.txt", "data/game/images/sub/", "data/game/images/sub/c.gif",
                "data/gameOther/d.png", "other/e.png")) {
                out.putNextEntry(new JarEntry(entry));
                out.closeEntry();
            }
        }

        try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toURI().toURL()}, null)) {
            assertEquals(Set.of("data/game/images/a.png", "data/game/images/B.JPG", "data/game/images/sub/c.gif"),
                ResourceFileManager.getResourcePaths("data/game/images", loader));
            assertEquals(Set.of("data/game/images/a.png", "data/game/images/B.JPG", "data/game/images/sub/c.gif"),
                ResourceFileManager.getResourcePaths("data\\game", loader));
            assertEquals(Set.of(), ResourceFileManager.getResourcePaths("missing", loader));
        }
    }

    @Test
    void shouldReturnTheSameFilesAsBefore() {
        // Values given by the former implementation (Reflections 0.10.2) on the test classpath
        assertEquals(Set.of("data/common/default/images/gazeplayClassicLogo.png", "data/common/default/images/bear.jpg"),
            ResourceFileManager.getResourcePaths("data/common/default/images"));
    }

}
