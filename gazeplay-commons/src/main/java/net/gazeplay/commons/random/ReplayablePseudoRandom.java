package net.gazeplay.commons.random;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

public class ReplayablePseudoRandom {
    private double seed;
    private long multiplier;
    private int increment;
    private double modulus;

    public ReplayablePseudoRandom() {
        this.seed =  Math.random();
        this.multiplier = 1103515245;
        this.increment = 12345;
        this. modulus = Math.pow(2,31);
    }


   public BigInteger random() {
        seed = (multiplier * seed + increment) % modulus;
        BigDecimal seedBig= new BigDecimal(seed);
        return seedBig.toBigInteger();
    }

    public int randIntRange(double max, double min){
        return (int)(this.random().intValue()%(max-min + 1) + min);
    }

    /*
    Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.
     */
    public double nextDouble() {
        int RAND_MAX = Integer.MAX_VALUE;
        return this.random().doubleValue() / (double)RAND_MAX ;
    }

}
