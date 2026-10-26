package net.gazeplay.commons.random;

import lombok.Getter;
import lombok.Setter;

/**
 * Pseudo-random numbers that can be drawn again: a linear congruential generator whose seed is saved in the results of
 * the game ("gameSeed" of replayData.json). The replay of a game creates a generator with the same seed, and so gets
 * the same targets, cards, positions... as the game played.
 *
 * @author Christophe El Zeinaty
 */
public class ReplayablePseudoRandom {

    @Getter @Setter
    private double seed;

    /**
     * A generator with a seed taken from the clock.
     */
    public ReplayablePseudoRandom() {
        this.setSeed(System.currentTimeMillis() % 100000000);
    }

    /**
     * A generator giving again the sequence of a saved seed.
     */
    public ReplayablePseudoRandom(double seed) {
        this.setSeed(seed);
    }

    /**
     * @return the next raw value of the sequence, from 0 to 2^32 (it becomes the new seed)
     */
    public double random() {
        long increment = 1013904223;
        long multiplier = 1664525;
        double modulus = Math.pow(2, 32);
        seed = (this.getSeed() * multiplier + increment) % modulus;
        return seed;
    }

    /**
     * @return the next number of the sequence, from 0 (included) to {@code bound} (excluded)
     */
    public int nextInt(int bound) {
        return (int) (this.nextDouble() * bound);
    }

    /**
     * @return the next number of the sequence, from 0 to {@code bound}
     */
    public double nextDouble(double bound) {
        return (this.nextDouble() * bound);
    }

    /**
     * @return the next number of the sequence, from 0 to 1
     */
    public double nextDouble() {
        int randMax = Integer.MAX_VALUE;
        return (this.random() / (double) randMax) / 2;
    }

    /**
     * @return true or false, with the same probability
     */
    public boolean nextBoolean() {
        int result = this.nextInt(2);
        return result != 0;
    }

    /**
     * @return the next number of the sequence, from 0 to 1
     */
    public float nextFloat() {
        int randMax = Integer.MAX_VALUE;
        return (float) (this.random() / (double) randMax) / 2;
    }

}

