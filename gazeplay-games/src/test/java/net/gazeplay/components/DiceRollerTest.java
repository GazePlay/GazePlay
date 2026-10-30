package net.gazeplay.components;

import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.soundsmanager.SoundManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * The die of Horses, Dice and Goose Game (30/10/2026): each face comes up with a chance of 1 in 6 (3 and 4 used to come
 * up twice as often as the other faces), and the die shows the face it tells.
 */
@ExtendWith(ApplicationExtension.class)
class DiceRollerTest {

    @Test
    void shouldShowEachFaceWithItsQuarterTurns() {
        for (int face = 1; face <= 6; face++) {
            for (int variety = 0; variety < 4; variety++) {
                int[] quarterTurns = DiceRoller.quarterTurnsShowing(face, variety);
                assertEquals(face, DiceRoller.faceShown(quarterTurns[0] * 90, quarterTurns[1] * 90),
                    "face " + face + ", variety " + variety);
            }
        }
    }

    @Test
    void shouldGiveEachFaceAsOftenAsTheOthers() throws Exception {
        DiceRoller die = WaitForAsyncUtils.asyncFx(() -> new DiceRoller(10, mock(SoundManager.class),
            new ReplayablePseudoRandom(2026))).get();
        Map<Integer, Integer> counts = new HashMap<>();
        int rolls = 6000;
        WaitForAsyncUtils.asyncFx(() -> {
            for (int i = 0; i < rolls; i++) {
                counts.merge(die.roll(null), 1, Integer::sum);
            }
        }).get();

        assertEquals(Set.of(1, 2, 3, 4, 5, 6), counts.keySet());
        // 1000 expected for each face; 3 and 4 used to come up about 1500 times, the others about 750
        counts.forEach((face, count) -> assertTrue(count > 880 && count < 1120, "face " + face + ": " + count));
    }
}
