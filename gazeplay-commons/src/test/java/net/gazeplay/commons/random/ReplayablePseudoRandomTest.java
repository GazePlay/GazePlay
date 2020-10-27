package net.gazeplay.commons.random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class ReplayablePseudoRandomTest {

    private ReplayablePseudoRandom random;

    public ReplayablePseudoRandomTest() {
    }

    @BeforeEach
    void setUp() {
        random = new ReplayablePseudoRandom();
    }

    @Test
    public void nextIntTestDistribution() {
        int[] res = new int[6];
        int number;
        for (int i = 0; i < 1000; i++) {
            number = random.nextInt(6);
            res[number]++;
        }

        // each possibility should at least appear half of the perfect distribution ( =(1/6)/2 = 0.08... )
        assertTrue(res[0] / 1000d > 0.08);
        assertTrue(res[1] / 1000d > 0.08);
        assertTrue(res[2] / 1000d > 0.08);
        assertTrue(res[3] / 1000d > 0.08);
        assertTrue(res[4] / 1000d > 0.08);
        assertTrue(res[5] / 1000d > 0.08);
    }

    /*
   Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.
    */
    @Test
    public void nextDoubleTestDistribution() {
        int[] res = new int[10];
        int error = 0;
        for (int i = 0; i < 1000; i++) {
            double number = random.nextDouble();
            if (number > 1.0d) {
                error++;
            } else if (number < 0.1d) {
                res[0]++;
            } else if (number < 0.2d) {
                res[1]++;
            } else if (number < 0.3d) {
                res[2]++;
            } else if (number < 0.4d) {
                res[3]++;
            } else if (number < 0.5d) {
                res[4]++;
            } else if (number < 0.6d) {
                res[5]++;
            } else if (number < 0.7d) {
                res[6]++;
            } else if (number < 0.8d) {
                res[7]++;
            } else if (number < 0.9d) {
                res[8]++;
            } else if (number < 1.0d) {
                res[9]++;
            }
        }


        // each possibility should at least appear half of the perfect distribution ( =(1/10)/2 = 0.05)
        assertTrue(res[0] / 1000d > 0.05);
        assertTrue(res[1] / 1000d > 0.05);
        assertTrue(res[2] / 1000d > 0.05);
        assertTrue(res[3] / 1000d > 0.05);
        assertTrue(res[4] / 1000d > 0.05);
        assertTrue(res[5] / 1000d > 0.05);
        assertTrue(res[6] / 1000d > 0.05);
        assertTrue(res[7] / 1000d > 0.05);
        assertTrue(res[8] / 1000d > 0.05);
        assertTrue(res[9] / 1000d > 0.05);
        assertEquals(0, error);
    }

    @Test
    public void nextBooleanTestDistribution() {
        int[] res = new int[2];
        boolean value;
        for (int i = 0; i < 100; i++) {
            value = random.nextBoolean();
            if (value) {
                res[0]++;
            } else {
                res[1]++;
            }
        }

        // each possibility should at least appear half of the perfect distribution ( =(1/2)/2 = 0.25)
        assertTrue(res[0] / 100d > 0.25);
        assertTrue(res[1] / 100d > 0.25);

    }

    @Test
    public void nextFloatTestDistribution() {
        int[] res = new int[10];
        int error = 0;
        for (int i = 0; i < 1000; i++) {
            float number = random.nextFloat();
            if (number > 1.0f) {
                error++;
            } else if (number < 0.1f) {
                res[0]++;
            } else if (number < 0.2f) {
                res[1]++;
            } else if (number < 0.3f) {
                res[2]++;
            } else if (number < 0.4f) {
                res[3]++;
            } else if (number < 0.5f) {
                res[4]++;
            } else if (number < 0.6f) {
                res[5]++;
            } else if (number < 0.7f) {
                res[6]++;
            } else if (number < 0.8f) {
                res[7]++;
            } else if (number < 0.9f) {
                res[8]++;
            } else if (number < 1.0f) {
                res[9]++;
            }
        }
        // each possibility should at least appear half of the perfect distribution ( =(1/10)/2 = 0.05)
        assertTrue(res[0] / 1000d > 0.05);
        assertTrue(res[1] / 1000d > 0.05);
        assertTrue(res[2] / 1000d > 0.05);
        assertTrue(res[3] / 1000d > 0.05);
        assertTrue(res[4] / 1000d > 0.05);
        assertTrue(res[5] / 1000d > 0.05);
        assertTrue(res[6] / 1000d > 0.05);
        assertTrue(res[7] / 1000d > 0.05);
        assertTrue(res[8] / 1000d > 0.05);
        assertTrue(res[9] / 1000d > 0.05);
        assertEquals(0, error);
    }

    @Test
    public void randomShouldBeDifferentWithDifferentSeeds() {

        double time = System.currentTimeMillis();

        double seed1 = time % 100000000;
        ReplayablePseudoRandom random1 = new ReplayablePseudoRandom(seed1);

        String firstRandom = "";
        for (int i = 0; i < 10; i++) {
            float number = random1.nextInt(2);
            firstRandom = firstRandom + number;
        }

        double seed2 = (time + 1) % 100000000;
        ReplayablePseudoRandom random2 = new ReplayablePseudoRandom(seed2);

        String secondRandom = "";
        for (int i = 0; i < 10; i++) {
            float number = random2.nextInt(2);
            secondRandom = secondRandom + number;
        }

        double seed3 = (time + 2) % 100000000;
        ReplayablePseudoRandom random3 = new ReplayablePseudoRandom(seed3);

        String thirdRandom = "";
        for (int i = 0; i < 10; i++) {
            float number = random3.nextInt(2);
            thirdRandom = thirdRandom + number;
        }

        assertNotEquals(firstRandom, secondRandom);
        assertNotEquals(firstRandom, thirdRandom);
        assertNotEquals(thirdRandom, secondRandom);
    }

    @Test
    public void randomShouldBeSameWithDifferentSeeds() {

        double seed = System.currentTimeMillis() % 100000000;

        ReplayablePseudoRandom random1 = new ReplayablePseudoRandom(seed);
        ReplayablePseudoRandom random2 = new ReplayablePseudoRandom(seed);

        String firstRandom = "";
        for (int i = 0; i < 10000; i++) {
            float number = random1.nextInt(2);
            firstRandom = firstRandom + number;
        }

        String secondRandom = "";
        for (int i = 0; i < 10000; i++) {
            float number = random2.nextInt(2);
            secondRandom = secondRandom + number;
        }

        assertEquals(firstRandom, secondRandom);
    }

    @Test
    public void randomShouldNotFollowASequenceAtLeastFor1000FirstTrial() {

        double number = random.random();
        for (int i = 0; i < 1000; i++) {
            assertNotEquals(number, random.random());
        }

    }


}
