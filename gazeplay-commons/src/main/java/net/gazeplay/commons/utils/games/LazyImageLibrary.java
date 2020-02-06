package net.gazeplay.commons.utils.games;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javafx.scene.image.Image;
import net.gazeplay.commons.utils.RuntimeExecutionException;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LazyImageLibrary extends AbstractImageLibrary {

    private final Cache<Integer, Image> imageCache = CacheBuilder.newBuilder().maximumSize(10).weakValues().build();

    private final List<File> allFiles;

    public LazyImageLibrary(File directoryFile) {
        this(ImageUtils.listImageFiles(directoryFile), null);
    }

    public LazyImageLibrary(File directoryFile, ImageLibrary defaultImageLibrary) {
        this(ImageUtils.listImageFiles(directoryFile), defaultImageLibrary);
    }

    public LazyImageLibrary(List<File> allFiles) {
        this.allFiles = allFiles;
        setFallbackImageLibrary(null);
    }

    public LazyImageLibrary(List<File> allFiles, ImageLibrary defaultImageLibrary) {
        this.allFiles = allFiles;
        setFallbackImageLibrary(defaultImageLibrary);
    }

    @Override
    public int getImagesCount() {
        return allFiles.size();
    }

    @Override
    protected Image loadImageAtIndex(int index) {
        try {
            return imageCache.get(index, () -> {
                File file = allFiles.get(index);
                return ImageUtils.loadImage(file);
            });
        } catch (ExecutionException e) {
            throw new RuntimeExecutionException(e);
        }
    }
}
