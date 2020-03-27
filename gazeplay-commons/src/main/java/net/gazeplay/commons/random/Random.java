package net.gazeplay.commons.random;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Random {

    private double seed;
    private long a;
    private int c;
    private double m;

    public Random() {
        this.seed = Math.random();
        this.a = 1103515245;//multiplier
        this.c = 12345; //increment
        this. m = Math.pow(2,31);//modulus;
    }


   public BigInteger randNumber() {
        seed = (a * seed + c) % m;
        BigDecimal seedBig= new BigDecimal(seed);
        return seedBig.toBigInteger();
    }

    public int randNumberRange(int max, int min){
        int randNum = this.randNumber().intValue()%(max-min + 1) + min;
        return randNum;
    }


}
