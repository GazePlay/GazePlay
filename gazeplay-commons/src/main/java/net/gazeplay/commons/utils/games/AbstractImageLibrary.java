package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public abstract class AbstractImageLibrary implements ImageLibrary {

    @Nullable
    @Setter
    private ImageLibrary fallbackImageLibrary;
    @Setter
    private ReplayablePseudoRandom randomGenerator;

    @Override
    public abstract int getImagesCount();

    protected abstract Image loadImageAtIndex(int index);

    @Override
    public Image pickRandomImage() {
        return pickMultipleRandomDistinctImages(1).iterator().next();
    }

    @Override
    public Set<Image> pickMultipleRandomDistinctImages(final int requestedPickCount) {
        final int distinctImagesCount = getImagesCount();
        if (distinctImagesCount < requestedPickCount) {

            final int defaultLibraryPickCount = requestedPickCount - distinctImagesCount;
            final int defaultLibraryAvailableCount = (fallbackImageLibrary == null) ? 0
                : fallbackImageLibrary.getImagesCount();
            if (defaultLibraryAvailableCount < defaultLibraryPickCount) {
                throw new IllegalArgumentException(String.format(
                    "There are not enough distinct Images in the %s : requested : %d, available : %d+%d",
                    this.getClass().getSimpleName(), requestedPickCount, distinctImagesCount,
                    defaultLibraryAvailableCount));
            }
            final Set<Image> result = collectRandom(distinctImagesCount, distinctImagesCount);
            result.addAll(fallbackImageLibrary.pickMultipleRandomDistinctImages(defaultLibraryPickCount));
            return result;

        }
        return collectRandom(requestedPickCount, distinctImagesCount);
    }

    @Override
    public List<Image> pickAllImages() {
        final int distinctImagesCount = getImagesCount();
        List<Image> list = new LinkedList<>();
        for (int i = 0; i < distinctImagesCount; i++) {
            list.add(loadImageAtIndex(i));
        }
        return list;
    }

    private Set<Image> collectRandom(final int limit, final int distinctImagesCount) {
        return IntStream.generate(() -> randomGenerator.nextInt(distinctImagesCount)).distinct().limit(limit).boxed()
            .peek(i -> log.debug("Picking Image at random index {} in ImageLibrary", i)).map(this::loadImageAtIndex)
            .collect(Collectors.toSet());
    }

}
