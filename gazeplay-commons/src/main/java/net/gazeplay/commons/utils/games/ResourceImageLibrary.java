package net.gazeplay.commons.utils.games;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javafx.scene.image.Image;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.RuntimeExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ResourceImageLibrary extends AbstractImageLibrary {

    private final Cache<Integer, Image> imageCache = CacheBuilder.newBuilder().maximumSize(10).weakValues().build();

    private final List<String> allFiles;

    public ResourceImageLibrary(String resourceDirectory, ReplayablePseudoRandom randomGenerator) {
        this(resourceDirectory, null, randomGenerator);
    }

    public ResourceImageLibrary(String resourceDirectory, ImageLibrary defaultImageLibrary, ReplayablePseudoRandom randomGenerator) {
        this.allFiles = new ArrayList<>(ResourceFileManager.getResourcePaths(resourceDirectory));
        setFallbackImageLibrary(defaultImageLibrary);
        setRandomGenerator(randomGenerator);
    }

    @Override
    public int getImagesCount() {
        return allFiles.size();
    }

    @Override
    protected Image loadImageAtIndex(int index) {
        try {
            return imageCache.get(index, () -> {
                String resource = allFiles.get(index);
                return ImageUtils.loadImage(resource);
            });
        } catch (ExecutionException e) {
            throw new RuntimeExecutionException(e);
        }
    }
}
