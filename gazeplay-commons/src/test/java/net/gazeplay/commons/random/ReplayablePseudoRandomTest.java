package net.gazeplay.commons.random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.Random;

@ExtendWith(ApplicationExtension.class)
public class ReplayablePseudoRandomTest {

    private ReplayablePseudoRandom random;
    private Random randomJava;

    @BeforeEach
    void setUp() {
        random = new ReplayablePseudoRandom();
        randomJava = new Random();
    }

    @Test
    public void nextIntTest()
    {
        int number = random.nextInt(10);
        System.out.println("random number : " + number);
    }

    /*
   Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.
    */
    @Test
    public void nextDoubleTest()
    {
        double number = random.nextDouble();
        System.out.println("random number : " + number);
    }

    @Test
    public void nextBooleanTest()
    {
        Boolean value = random.nextBoolean();
        System.out.println("random boolean : " + value);
    }

    @Test
    public void nextFloatTest()
    {
        float number = random.nextFloat();
        System.out.println("random number : " + number);
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

}
