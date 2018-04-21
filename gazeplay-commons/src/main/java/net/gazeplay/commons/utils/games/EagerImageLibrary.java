package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class EagerImageLibrary implements ImageLibrary {

    private final List<Image> allImages;

    @Nullable
    private final ImageLibrary defaultImageLibrary;

    private final Random random = new Random();

    public EagerImageLibrary(File directoryFile) {
        this(ImageUtils.loadAllImagesInDirectory(directoryFile), null);
    }

    public EagerImageLibrary(File directoryFile, ImageLibrary defaultImageLibrary) {
        this(ImageUtils.loadAllImagesInDirectory(directoryFile), defaultImageLibrary);
    }

    public EagerImageLibrary(List<Image> allImages) {
        this.allImages = allImages;
        this.defaultImageLibrary = null;
    }

    public EagerImageLibrary(List<Image> allImages, ImageLibrary defaultImageLibrary) {
        this.allImages = allImages;
        this.defaultImageLibrary = defaultImageLibrary;
    }

    @Override
    public int getImagesCount() {
        return allImages.size();
    }

    public Image loadImageAtIndex(int index) {
        return allImages.get(index);
    }

    @Override
    public Image pickRandomImage() {
        // final int randomIndex = random.nextInt(allImages.size());
        // return allImages.get(randomIndex);

        return pickMultipleRandomDistinctImages(1).iterator().next();
    }

    @Override
    public Set<Image> pickMultipleRandomDistinctImages(int requestedPickCount) {
        final int distinctImagesCount = getImagesCount();
        if (distinctImagesCount < requestedPickCount) {

            final int defaultLibraryPickCount = requestedPickCount - distinctImagesCount;
            final int defaultLibraryAvailableCount = (defaultImageLibrary == null) ? 0
                    : defaultImageLibrary.getImagesCount();
            if (defaultLibraryAvailableCount < defaultLibraryPickCount) {
                throw new IllegalArgumentException(String.format(
                        "There are not enough distinct Images in the %s : requested : %d, available : %d+%d",
                        this.getClass().getSimpleName(), requestedPickCount, distinctImagesCount,
                        defaultLibraryAvailableCount));
            }
            Set<Image> result = collectRandom(distinctImagesCount, distinctImagesCount);
            result.addAll(defaultImageLibrary.pickMultipleRandomDistinctImages(defaultLibraryPickCount));
            return result;

        }
        return collectRandom(requestedPickCount, distinctImagesCount);
    }

    private Set<Image> collectRandom(int limit, int distinctImagesCount) {
        return IntStream.generate(() -> random.nextInt(distinctImagesCount)).distinct().limit(limit).boxed()
                .peek(i -> log.info("index = {}", i)).map(this::loadImageAtIndex).collect(Collectors.toSet());
    }

}
