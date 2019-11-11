package net.gazeplay;

import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.ImageDirectoryLocator;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class ApplicationIconImageLocator {

    private static final String iconParentDirectoryPath = "data/common/images";

    private static final String iconFilename = "gazeplayicon.png";

    @Nullable
    public Image findApplicationIcon() {
        final String iconLocation = iconParentDirectoryPath + "/" + iconFilename;
        try {
            return new Image(iconLocation);
        } catch (IllegalArgumentException ie) {
            log.warn("findApplicationIcon() : error while loading image from {}", iconLocation);

            File iconImageDirectory = ImageDirectoryLocator.locateImagesDirectoryInUnpackedDistDirectory(iconParentDirectoryPath);
            if (iconImageDirectory != null) {
                try {
                    String filePath = new File(iconImageDirectory, iconLocation).getCanonicalPath();
                    return new Image(filePath);
                } catch (IOException | RuntimeException ioe) {
                    log.warn("findApplicationIcon() : error while loading image from {}", iconImageDirectory);
                }
            }
        }

        return null;
    }

}
