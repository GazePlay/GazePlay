package net.gazeplay.commons.random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.Random;

@ExtendWith(ApplicationExtension.class)
public class ReplayablePseudoRandomTest {

    private ReplayablePseudoRandom random, randomManualSeed;
    private Random randomJava;

    @BeforeEach
    void setUp() {
        random = new ReplayablePseudoRandom();
        randomManualSeed = new ReplayablePseudoRandom(System.currentTimeMillis());
        randomJava = new Random();
    }

    @Test
    public void nextIntTest()
    {
        int[] res = new int[7];
        int number;
        for(int i = 0 ; i <100 ; i++){
            number = random.nextInt(7);
            res[number]++;
            System.out.println("number : " + number);
        }
        System.out.println("random 0 : " + res[0]);
        System.out.println("random 1 : " + res[1]);
        System.out.println("random 2 : " + res[2]);
        System.out.println("random 3 : " + res[3]);
        System.out.println("random 4 : " + res[4]);
        System.out.println("random 5 : " + res[5]);
        System.out.println("random 6 : " + res[6]);
    }

    /*
   Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.
    */
    @Test
    public void nextDoubleTest()
    {
        int[] res = new int[10];
        int error = 0;
            for(int i = 0 ; i <100 ; i++) {
            double number = random.nextDouble();
            if(number>1.0d) {
                error++;
            } else if(number<0.1d) {
                res[0]++;
            } else if(number<0.2d) {
                res[1]++;
            } else if(number<0.3d) {
                res[2]++;
            } else if(number<0.4d) {
                res[3]++;
            } else if(number<0.5d) {
                res[4]++;
            } else if(number<0.6d) {
                res[5]++;
            } else if(number<0.7d) {
                res[6]++;
            } else if(number<0.8d) {
                res[7]++;
            } else if(number<0.9d) {
                res[8]++;
            } else if(number<1.0d) {
                res[9]++;
            }
        }
        System.out.println("below 0.1 : " + res[0]);
        System.out.println("below 0.2 : " + res[1]);
        System.out.println("below 0.3 : " + res[2]);
        System.out.println("below 0.4 : " + res[3]);
        System.out.println("below 0.5 : " + res[4]);
        System.out.println("below 0.6 : " + res[5]);
        System.out.println("below 0.7 : " + res[6]);
        System.out.println("below 0.8 : " + res[7]);
        System.out.println("below 0.9 : " + res[8]);
        System.out.println("below 1.0 : " + res[9]);
        System.out.println("error : " + error);
    }

    @Test
    public void nextBooleanTest()
    {
        int[] res = new int[2];
        boolean value;
        for(int i = 0 ; i <10 ; i++){
            value = random.nextBoolean();
            if(value){
                res[0]++;
            }else{
                res[1]++;
            }
        }
        System.out.println("random true : " + res[0]);
        System.out.println("random false : " + res[1]);

    }

    @Test
    public void nextFloatTest()
    {
        int[] res = new int[10];
        int error = 0;
        for(int i = 0 ; i <1000000 ; i++) {
            float number = random.nextFloat();
            if(number>1.0f) {
                error++;
            } else if(number<0.1f) {
                res[0]++;
            } else if(number<0.2f) {
                res[1]++;
            } else if(number<0.3f) {
                res[2]++;
            } else if(number<0.4f) {
                res[3]++;
            } else if(number<0.5f) {
                res[4]++;
            } else if(number<0.6f) {
                res[5]++;
            } else if(number<0.7f) {
                res[6]++;
            } else if(number<0.8f) {
                res[7]++;
            } else if(number<0.9f) {
                res[8]++;
            } else if(number<1.0f) {
                res[9]++;
            }
        }
        System.out.println("below 0.1 : " + res[0]);
        System.out.println("below 0.2 : " + res[1]);
        System.out.println("below 0.3 : " + res[2]);
        System.out.println("below 0.4 : " + res[3]);
        System.out.println("below 0.5 : " + res[4]);
        System.out.println("below 0.6 : " + res[5]);
        System.out.println("below 0.7 : " + res[6]);
        System.out.println("below 0.8 : " + res[7]);
        System.out.println("below 0.9 : " + res[8]);
        System.out.println("below 1.0 : " + res[9]);
        System.out.println("error : " + error);
    }


    @Test
    public void compareNextIntTest()
    {
        int number = 0;
        int javaNumb = 0;
        for(int i = 1; i < 4; i++){
            number = random.nextInt(i);
            javaNumb = randomJava.nextInt(i);
            System.out.println("Java Random generator: " + javaNumb + " Ours random number: " + number);
        }

    }

    @Test
    public void compareNextIntTestWithDifferentSeedValueTest()
    {
        int numberDefaultSeed = 0;
        int numberManualSeed = 0;
        int javaNumb = 0;

        for(int i = 1; i <4 ; i++){
            numberDefaultSeed = random.nextInt(i);
            numberManualSeed = randomManualSeed.nextInt(i);
            javaNumb = randomJava.nextInt(i);
            System.out.println("Manual seed random number: " + numberManualSeed + " D" +
                "" +
                "efault seed random number: " + numberDefaultSeed + " Java seed random number: " + javaNumb);
        }

    }

}
