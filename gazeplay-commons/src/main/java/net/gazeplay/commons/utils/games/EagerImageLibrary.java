package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;

import java.io.File;
import java.util.List;
import java.util.Random;

public class EagerImageLibrary implements ImageLibrary {

	private final List<Image> allImages;

	private Random random = new Random();
	
	public EagerImageLibrary(File directoryFile) {
		this.allImages = ImageUtils.loadAllImagesInDirectory(directoryFile);
	}

	@Override
	public Image pickRandomImage() {
		final int randomIndex = random.nextInt(allImages.size());
		return allImages.get(randomIndex);
	}

}
