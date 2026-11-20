package net.gazeplay.games.trainSwitches;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How the trains of "Aiguillages" come (maintainer's choice of 10/11/2026), from the easiest to the hardest: one at a
 * time with a pause before each switch (20/11/2026, after a tester's feedback: the easiest, to learn the game), one at
 * a time (the next one waits on the tunnel until the last one has arrived and "Departure" is fixed), with a pause
 * before each switch (everything stops until "Resume" is fixed), or one after the other at a steady pace.
 */
@Getter
@RequiredArgsConstructor
public enum TrainSwitchesRhythm {
    ONE_AT_A_TIME_PAUSE("TrainSwitchesOneAtATimePause", true, true),
    ONE_AT_A_TIME("TrainSwitchesOneAtATime", true, false),
    PAUSE("TrainSwitchesPause", false, true),
    CONTINUOUS("TrainSwitchesContinuous", false, false);

    /**
     * Key of translation, and value of the third row of switches of the variants.
     */
    private final String key;

    /**
     * Whether a train leaves only when the last one has arrived and "Departure" is fixed.
     */
    private final boolean oneAtATime;

    /**
     * Whether everything stops before each switch until "Resume" is fixed.
     */
    private final boolean pauses;

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
