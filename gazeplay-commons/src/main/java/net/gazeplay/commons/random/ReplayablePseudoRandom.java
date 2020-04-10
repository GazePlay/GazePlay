package net.gazeplay.commons.random;

import java.math.BigInteger;

public class ReplayablePseudoRandom {
    private BigInteger seed;
    private BigInteger multiplier;
    private BigInteger increment;
    private BigInteger modulus;

    public ReplayablePseudoRandom() {
        this.seed = BigInteger.valueOf(System.currentTimeMillis());
        this.multiplier = BigInteger.valueOf(25214903917L);
        this.increment = BigInteger.valueOf(11);
        this. modulus = BigInteger.ONE.shiftLeft(48);
    }

   public double random() {
       seed = seed.multiply(multiplier).add(increment).mod(modulus);
       return seed.doubleValue();
    }

    public int randIntRange(double min, double max){
        return (int)(this.random() % (max-min + 1) + min);
    }

    /*
    Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.
     */
    public double nextDouble() {
        int RAND_MAX = Integer.MAX_VALUE;
        return this.random() / (double)RAND_MAX ;
    }

}
