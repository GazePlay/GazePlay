package net.gazeplay.commons.random;

public class ReplayablePseudoRandom {
    private double seed;
    private long multiplier;
    private long increment;
    private double modulus;

    public ReplayablePseudoRandom() {
        this.seed = System.currentTimeMillis();
        this.multiplier = 1103515245;
        this.increment = 12345;
        this.modulus = Math.pow(2, 31);
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
        int RAND_MAX = Integer.MAX_VALUE;
        return this.random() / (double) RAND_MAX;
    }

}

