package net.gazeplay.commons.utils.games;

import com.google.common.collect.Sets;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.Collections;
import java.util.Set;

@ExtendWith(ApplicationExtension.class)
class ResourceFileManagerTest {

    @Test
    void canGetAllFilesInResourcePackage() {
        Set<String> set = ResourceFileManager.getResourcePaths("data/biboule/images");
        assert (set.size() == 1);
        set = ResourceFileManager.getResourcePaths("data/common/default/images");
        assert (set.size() == 2);
        set = ResourceFileManager.getResourcePaths("data");
        assert (set.size() == 3);
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

}
