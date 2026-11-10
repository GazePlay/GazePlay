package net.gazeplay.games.trainSwitches;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Aiguillages" (maintainer's choices of 10/11/2026): the number of stations of its circuits (one switch
 * less), read in data/trainSwitches/circuits.txt.
 */
@Getter
@RequiredArgsConstructor
public enum TrainSwitchesLevel {
    EASY("TrainSwitchesEasy", "easy", 3),
    NORMAL("TrainSwitchesNormal", "normal", 5),
    HARD("TrainSwitchesHard", "hard", 8);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * Its name in the file of the circuits.
     */
    private final String fileName;

    /**
     * The stations of each of its circuits.
     */
    private final int stations;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static TrainSwitchesLevel of(final String key) {
        for (final TrainSwitchesLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
