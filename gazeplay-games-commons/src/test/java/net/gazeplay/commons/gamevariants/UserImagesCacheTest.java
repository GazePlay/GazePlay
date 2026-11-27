package net.gazeplay.commons.gamevariants;

import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The images of the user decoded once (27/11/2026, tester's feedback point 50): at the size asked, given again,
 * decoded in the background within a memory budget, an image not ready decoded at once, the images used the longest ago
 * let go past the budget, an unreadable file in error, everything let go at the end.
 */
@ExtendWith(ApplicationExtension.class)
class UserImagesCacheTest {

    @TempDir
    Path folder;

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private List<File> images(final int count, final int width, final int height) throws Exception {
        final List<File> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final File file = folder.resolve("image" + i + ".png").toFile();
            ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", file);
            files.add(file);
        }
        return files;
    }

    @Test
    void shouldDecodeAnImageOnceAtTheSizeAsked() throws Exception {
        final File file = images(1, 800, 400).getFirst();
        final UserImagesCache cache = new UserImagesCache(200);
        fx(() -> {
            final Image image = cache.get(file);
            assertEquals(200, image.getWidth());
            assertEquals(100, image.getHeight());
            assertSame(image, cache.get(file));
            assertSame(image, cache.get(file.toURI().toString()));
            cache.dispose();
            assertEquals(0, cache.count());
            assertNotSame(image, cache.get(file), "decoded again after the end");
            return null;
        });
    }

    @Test
    void shouldDecodeInTheBackgroundAndGiveTheSameImageWhetherReadyOrNot() throws Exception {
        final List<File> files = images(3, 300, 300);
        final UserImagesCache cache = new UserImagesCache(100);
        final List<Image> preloaded = fx(() -> {
            cache.preload(files);
            assertEquals(3, cache.count());
            // Asked before its decoding is over: decoded at once, complete
            final Image first = cache.get(files.getFirst());
            assertEquals(1, first.getProgress());
            assertEquals(100, first.getWidth());
            return List.of(cache.get(files.get(1)), cache.get(files.get(2)));
        });
        for (final Image image : preloaded) {
            assertEquals(1, image.getProgress());
            assertFalse(image.isError());
        }
        // Once decoded in the background, given as it is
        final File late = images(4, 300, 300).get(3);
        final Image background = fx(() -> {
            cache.preload(List.of(late));
            return cache.peek(late);
        });
        assertNotNull(background);
        final long until = System.currentTimeMillis() + 10_000;
        while (background.getProgress() < 1 && System.currentTimeMillis() < until) {
            WaitForAsyncUtils.sleep(20, TimeUnit.MILLISECONDS);
        }
        assertSame(background, fx(() -> cache.get(late)));
        fx(() -> {
            cache.dispose();
            return null;
        });
    }

    @Test
    void shouldStayWithinItsMemoryBudget() throws Exception {
        final List<File> files = images(5, 100, 100);
        // Room for 2 images of 100 x 100 (40 000 bytes each)
        final UserImagesCache cache = new UserImagesCache(100, 90_000);
        fx(() -> {
            cache.preload(files);
            assertEquals(2, cache.count(), "the background decoding stops at the budget");
            cache.dispose();
            for (final File file : files) {
                cache.get(file);
                assertTrue(cache.bytes() <= 90_000);
            }
            assertEquals(2, cache.count());
            // The images used the longest ago let go: the last two are kept
            final Image kept = cache.get(files.get(4));
            assertSame(kept, cache.get(files.get(4)));
            return null;
        });
    }

    @Test
    void shouldGiveAnImageReadyOnlyOnceDecodedWithoutDecodingIt() throws Exception {
        final File file = images(1, 300, 300).getFirst();
        final String url = file.toURI().toString();
        final UserImagesCache cache = new UserImagesCache(100);
        fx(() -> {
            assertNull(cache.ready(url));
            assertEquals(0, cache.count(), "nothing decoded");
            final Image image = cache.get(file);
            assertSame(image, cache.ready(url));
            return null;
        });
    }

    @Test
    void shouldMakeRoomForAnImageToPreloadByLettingTheOldestGo() throws Exception {
        final List<File> files = images(3, 100, 100);
        // Room for 2 images of 100 x 100, as the hidden pictures of Bubbles: the one before, the one of now, the next
        final UserImagesCache cache = new UserImagesCache(100, 90_000);
        fx(() -> {
            cache.get(files.get(0));
            cache.get(files.get(1));
            cache.preload(List.of(files.get(2)));
            assertNull(cache.peek(files.get(0)), "the oldest let go");
            assertNotNull(cache.peek(files.get(1)));
            assertNotNull(cache.peek(files.get(2)), "the next one decoded in the background");
            cache.dispose();
            return null;
        });
    }

    @Test
    void shouldGiveAnImageInErrorForAFileThatIsNotAnImage() throws Exception {
        final File text = folder.resolve("notes.png").toFile();
        Files.writeString(text.toPath(), "not an image");
        final UserImagesCache cache = new UserImagesCache(100);
        fx(() -> {
            final Image image = cache.get(text);
            assertTrue(image.isError());
            assertSame(image, cache.get(text));
            assertEquals(0, cache.bytes());
            return null;
        });
    }
}
