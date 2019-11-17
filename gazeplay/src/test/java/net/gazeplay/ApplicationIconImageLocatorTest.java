package net.gazeplay;

import javafx.scene.image.Image;
import net.gazeplay.commons.utils.ApplicationIconImageLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.Assert.assertNotNull;

@ExtendWith(ApplicationExtension.class)
class ApplicationIconImageLocatorTest {

    @Test
    void canFindTheApplicationIcon() {
        Image icon = new ApplicationIconImageLocator().findApplicationIcon();
        assertNotNull(icon);
    }

}
