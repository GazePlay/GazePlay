package net.gazeplay.games.trainSwitches;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How the trains of "Aiguillages" come (maintainer's choice of 10/11/2026): one at a time (the next one leaves when the
 * last one has arrived), with a pause before each switch (everything stops until "Resume" is fixed), or one after the
 * other at a steady pace.
 */
@Getter
@RequiredArgsConstructor
public enum TrainSwitchesRhythm {
    ONE_AT_A_TIME("TrainSwitchesOneAtATime"),
    PAUSE("TrainSwitchesPause"),
    CONTINUOUS("TrainSwitchesContinuous");

    /**
     * Key of translation, and value of the third row of switches of the variants.
     */
    private final String key;

    /**
     * @return the rhythm of a key, one at a time for an unknown one
     */
    public static TrainSwitchesRhythm of(final String key) {
        for (final TrainSwitchesRhythm rhythm : values()) {
            if (rhythm.key.equals(key)) {
                return rhythm;
            }
        }
        return ONE_AT_A_TIME;
    }
}
