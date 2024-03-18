package net.gazeplay.games.gazeplayEval.round;

import javafx.util.Pair;
import lombok.Data;
import lombok.Getter;

import java.util.Set;

@Data
public class RoundResults {
    @Getter
    private final int picturesCount;
    @Getter
    private final Set<Pair<Integer, Integer>> selectedPictures;
    @Getter
    private final long timeRound;
}
