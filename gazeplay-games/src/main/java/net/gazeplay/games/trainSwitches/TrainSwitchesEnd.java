package net.gazeplay.games.trainSwitches;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Aiguillages" ends (maintainer's choice of 10/11/2026): after 5, 10 or 20 trains arrived at a station, or never.
 */
@Getter
@RequiredArgsConstructor
public enum TrainSwitchesEnd {
    TRAINS_5("TrainSwitchesTrains5", 5),
    TRAINS_10("TrainSwitchesTrains10", 10),
    TRAINS_20("TrainSwitchesTrains20", 20),
    ENDLESS("TrainSwitchesEndless", 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The trains of the game, 0 for a game without end.
     */
    private final int trains;

    /**
     * @return the end of a key, 10 trains for an unknown one
     */
    public static TrainSwitchesEnd of(final String key) {
        for (final TrainSwitchesEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return TRAINS_10;
    }

    /**
     * @return the end closest to a number of trains of the former variants (typed in the window of the variants), 10
     * trains for 0 or less
     */
    public static TrainSwitchesEnd closestTo(final int trains) {
        if (trains <= 0) {
            return TRAINS_10;
        }
        return trains <= 7 ? TRAINS_5 : trains <= 15 ? TRAINS_10 : TRAINS_20;
    }
}
