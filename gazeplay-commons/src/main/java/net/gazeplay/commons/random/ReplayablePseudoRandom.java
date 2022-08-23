package net.gazeplay.commons.random;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Christophe El Zeinaty
 */

public class ReplayablePseudoRandom {

    @Getter
    @Setter
    private double seed;

    public ReplayablePseudoRandom() {
        this.setSeed(System.currentTimeMillis() % 100000000);
    }

    public ReplayablePseudoRandom(double seed) {
        this.setSeed(seed);
    }

    public double random() {
        long increment = 1013904223;
        long multiplier = 1664525;
        double modulus = Math.pow(2, 32);
        seed = (this.getSeed() * multiplier + increment) % modulus;
        return seed;
    }

    /*
    Generate Random number within a limit
     */
    public int nextInt(int bound) {
        return (int) (this.nextDouble() * bound);
    }

    /*
   Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.
    */
    public double nextDouble(double bound) {
        return (this.nextDouble() * bound);
    }

    /*
    Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.
     */
    public double nextDouble() {
        int randMax = Integer.MAX_VALUE;
        return (this.random() / (double) randMax) / 2;
    }

    public boolean nextBoolean() {
        int result = this.nextInt(2);
        return result != 0;
    }

    public float nextFloat() {
        int randMax = Integer.MAX_VALUE;
        return (float) (this.random() / (double) randMax) / 2;
    }

}

