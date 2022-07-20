package net.gazeplay.games.flowerOfNumbers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FlowerTest {
    Random random = new Random();

    Flower flower;

    @BeforeEach
    void init() {
        flower = new Flower();
        flower.init();
    }

    @Test
    void shouldInitFlower() {
        flower.init();
        assertEquals(0, flower.getPistil());
        for (Petal petal : Petal.values()) {
            for (int index = 0; index < petal.getCapacity(); index++) {
                assertEquals(0, flower.get(petal, index));
            }
        }
    }

    @Test
    void shouldSetPistil() {
        assertEquals(0, flower.getPistil());
        flower.setPistil(1000);
        assertEquals(1000, flower.getPistil());
    }

    @Test
    void shouldAddValuesOnPetals() {
        for (Petal petal : Petal.values()) {
            for (int index = 0; index < petal.getCapacity(); index++) {
                final int value = random.nextInt();
                assertEquals(0, flower.get(petal, index));
                flower.add(petal, index, value);
                assertEquals(value, flower.get(petal, index));
            }
        }
    }

    @Test
    void shouldRemoveValuesOnPetals() {
        for (Petal petal : Petal.values()) {
            for (int index = 0; index < petal.getCapacity(); index++) {
                flower.add(petal, index, random.nextInt());
            }
        }

        for (Petal petal : Petal.values()) {
            for (int index = 0; index < petal.getCapacity(); index++) {
                assertNotEquals(0, flower.get(petal, index));
                flower.remove(petal, index);
                assertEquals(0, flower.get(petal, index));
            }
        }
    }

    @Test
    void shouldGetFirstEmptyCell() {
        for (Petal petal : Petal.values()) {
            flower.add(petal, 0, random.nextInt());
        }

        for (Petal petal : Petal.values()) {
            assertEquals(petal == Petal.WORDS ? -1 : 1, flower.getIndexFirstEmptyCell(petal));
        }
    }

    @Test
    void shouldBeFull() {
        for (Petal petal : Petal.values()) {
            for (int index = 0; index < petal.getCapacity(); index++) {
                flower.add(petal, index, random.nextInt());
            }
        }

        for (Petal petal : Petal.values()) {
            assertTrue(flower.petalIsFull(petal));
        }
        assertTrue(flower.isFull());
    }

    @Test
    void shouldBeComplete() {
        flower.setPistil(1000);
        for (Petal petal : Petal.values()) {
            flower.add(petal, 0, 1000);
            assertTrue(flower.petalIsComplete(petal));
        }
        assertTrue(flower.isComplete());
    }
}
