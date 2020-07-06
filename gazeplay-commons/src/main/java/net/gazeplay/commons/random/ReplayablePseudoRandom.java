package net.gazeplay.commons.random;

/**
 * @author Christophe El Zeinaty
 */

public class ReplayablePseudoRandom {
    private double seed;
    private long multiplier;
    private long increment;
    private double modulus;

    public ReplayablePseudoRandom() {
        this.seed = System.currentTimeMillis();
        this.multiplier = 1664525;
        this.increment = 1013904223;
        this.modulus = Math.pow(2, 32);
    }

    public ReplayablePseudoRandom(double seed) {
        this.seed = seed;
        this.multiplier = 1664525;
        this.increment = 1013904223;
        this.modulus = Math.pow(2, 32);
    }

    public double random() {
        seed = (seed * multiplier + increment) % modulus;
        return seed;
    }

    /*
    Generate Random number within a limit
     */
    public int nextInt(int bound) {
        return (int) (this.random() % bound);
    }

    /*
    Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.
     */
    public double nextDouble() {
        int randMax = Integer.MAX_VALUE;
        return this.random() / (double) randMax;
    }

    public boolean nextBoolean() {
        int result = this.nextInt(2);
        return result != 0;
    }

    public float nextFloat() {
        int randMax = Integer.MAX_VALUE;
        return (float) (this.random() / (double) randMax);
    }
}

