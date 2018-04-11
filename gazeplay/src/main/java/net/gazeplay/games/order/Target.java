package net.gazeplay.games.order;

import java.util.List;
import javafx.scene.image.Image;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.stats.Stats;

/**
 *
 * @author vincent
 */
public class Target extends Portrait {
    private final Stats stats;
    private final int num;

    public Target(int initialRadius, RandomPositionGenerator randomPositionGenerator, Stats stats,
            List<Image> availableImages, int num) {
        super(initialRadius, randomPositionGenerator, availableImages);
        this.stats = stats;
        this.num = num;
    }

}
