package net.gazeplay.commons.gamevariants;

import javafx.scene.image.Image;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The images of the user of a game, each decoded once (tester's feedback of 23/11/2026, point 50, then all the games of
 * "Mes images" on 27/11/2026): a game decoded the whole file of an image each time it needed it (a new target, a new
 * round), on the thread of the display, so that big photos made it jerk or stop between two rounds.
 * <p>
 * The images are decoded at the size the game asks for (keeping their proportions), kept and given again; they can be
 * decoded in the background beforehand ({@link #preload}). An image asked for while its decoding in the background is
 * not over is decoded at once, as before: the game gets the same image whenever it asks, so that its random draws, and
 * thus its replays, do not depend on the time the decoding takes. The images kept stay within a memory budget: past it,
 * the images used the longest ago are let go (one JVM per game: everything is freed at its end anyway).
 * <p>
 * To be used on the thread of JavaFX only.
 */
public final class UserImagesCache {

    /**
     * The memory the images kept may take, in bytes (a game gets a quarter of the memory of the computer).
     */
    public static final long DEFAULT_BUDGET = 200L * 1024 * 1024;

    /**
     * The images decoded in the background at most, so that the processor is not taken by them.
     */
    static final int MOST_PRELOADED = 24;

    private final double size;
    private final long budget;

    /**
     * The images by URL, the one used the longest ago first.
     */
    private final Map<String, Image> images = new LinkedHashMap<>(16, 0.75f, true);

    /**
     * @param size the largest width and height of the images, in pixels (their proportions are kept)
     */
    public UserImagesCache(final double size) {
        this(size, DEFAULT_BUDGET);
    }

    /**
     * @param size   the largest width and height of the images, in pixels (their proportions are kept)
     * @param budget the memory the images kept may take, in bytes
     */
    public UserImagesCache(final double size, final long budget) {
        this.size = size;
        this.budget = budget;
    }

    /**
     * @return the image of a file at the size of the cache: the one decoded before, otherwise decoded now (also when
     * its decoding in the background is not over); an image that cannot be read is an image in error
     * ({@link Image#isError()}), as with {@code new Image(...)}
     */
    public Image get(final File file) {
        return get(file.toURI().toString());
    }

    /**
     * @return the image of a URL at the size of the cache, as {@link #get(File)}
     */
    public Image get(final String url) {
        Image image = images.get(url);
        if (image != null && (image.getProgress() >= 1 || image.isError())) {
            return image;
        }
        if (image != null) {
            image.cancel();
        }
        image = new Image(url, size, size, true, true);
        images.put(url, image);
        trim(entry -> !entry.getKey().equals(url), 0);
        return image;
    }

    /**
     * @return the image of a URL if it is already decoded and readable, otherwise null, without decoding anything (for
     * a game that shows something else meanwhile, as Bubbles a bubble of color)
     */
    public Image ready(final String url) {
        final Image image = images.get(url);
        return image != null && image.getProgress() >= 1 && !image.isError() ? image : null;
    }

    /**
     * Starts decoding images in the background, in their order, while the memory budget allows it.
     */
    public void preload(final List<File> files) {
        preloadUrls(files.stream().map(file -> file.toURI().toString()).toList());
    }

    /**
     * Starts decoding the images of URLs in the background, as {@link #preload}.
     */
    public void preloadUrls(final List<String> urls) {
        int started = 0;
        for (final String url : urls) {
            if (images.containsKey(url)) {
                continue;
            }
            // Room made by letting go the decoded images used the longest ago, not those to preload now
            trim(entry -> !urls.contains(entry.getKey()) && entry.getValue().getProgress() >= 1, estimate());
            if (started >= MOST_PRELOADED || bytes() + estimate() > budget) {
                return;
            }
            images.put(url, new Image(url, size, size, true, true, true));
            started++;
        }
    }

    /**
     * Stops the decodings in the background and lets all the images go.
     */
    public void dispose() {
        images.values().forEach(Image::cancel);
        images.clear();
    }

    /**
     * @return the image kept for a file, as it is (perhaps still decoded in the background), null if none, for the tests
     */
    Image peek(final File file) {
        return images.get(file.toURI().toString());
    }

    /**
     * @return the number of images kept, for the tests
     */
    int count() {
        return images.size();
    }

    /**
     * @return the memory taken by the images kept, those being decoded counted at the most they can take
     */
    long bytes() {
        long total = 0;
        for (final Image image : images.values()) {
            total += image.getProgress() >= 1 && !image.isError()
                ? (long) image.getWidth() * (long) image.getHeight() * 4 : image.isError() ? 0 : estimate();
        }
        return total;
    }

    private long estimate() {
        return (long) Math.ceil(size) * (long) Math.ceil(size) * 4;
    }

    /**
     * Lets the images used the longest ago go while the budget is passed (with room for more bytes), among those that
     * may go.
     */
    private void trim(final Predicate<Map.Entry<String, Image>> mayGo, final long room) {
        final Iterator<Map.Entry<String, Image>> it = images.entrySet().iterator();
        while (bytes() + room > budget && it.hasNext()) {
            final Map.Entry<String, Image> eldest = it.next();
            if (mayGo.test(eldest)) {
                eldest.getValue().cancel();
                it.remove();
            }
        }
    }
}
