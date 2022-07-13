package net.gazeplay.games.flowerOfNumbers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Flower {
    private final List<int[]> flower;
    private int pistil;

    public Flower() {
        flower = new ArrayList<>(Petal.values().length);
        for (Petal petal : Petal.values()) {
            flower.add(new int[petal.getCapacity()]);
        }
    }

    /**
     * Initialize the flower: empty all the petal's arrays and initialize the value of the pistil to 0.
     */
    public void init() {
        pistil = 0;
        for (int[] petal : flower) {
            Arrays.fill(petal, 0);
        }
    }

    /**
     * Return the value of the pistil, is the number to find on petals.
     *
     * @return the value of the pistil
     */
    public int getPistil() {
        return pistil;
    }

    /**
     * Set the value of the pistil, this new value will be the one to find on petals.
     *
     * @param value the new value of the pistil
     */
    public void setPistil(int value) {
        pistil = value;
    }

    /**
     * Return the value in the array from the specified petal at the specified index.
     *
     * @param petal the petal in which get the value
     * @param index the index of the value to get
     * @return the value on petal's array
     */
    public int get(Petal petal, int index) {
        return flower.get(petal.ordinal())[index];
    }

    /**
     * Add a value in the array from the specified petal at the specified index.
     *
     * @param petal the petal in which add the value
     * @param index the index where to add the value
     * @param value the value to add
     */
    public void add(Petal petal, int index, int value) {
        flower.get(petal.ordinal())[index] = value;
    }

    /**
     * Remove the value in the array from the specified petal at the specified index.
     *
     * @param petal the petal in which remove the value
     * @param index the index of the value to remove
     */
    public void remove(Petal petal, int index) {
        flower.get(petal.ordinal())[index] = 0;
    }

    /**
     * Return the index of the first empty cell in the array from the specified petal.
     *
     * @param petal the petal in which get the index
     * @return the index of the first empty cell
     */
    public int getIndexFirstEmptyCell(Petal petal) {
        int index = 0;
        while (index < petal.getCapacity() && flower.get(petal.ordinal())[index] != 0) {
            index++;
        }
        return index < petal.getCapacity() ? index : -1;
    }

    /**
     * Return the size of the array from the specified petal.
     *
     * @param petal the petal to calculate the size
     * @return the size of it array
     */
    private int size(Petal petal) {
        int size = 0;
        for (int index = 0; index < flower.get(petal.ordinal()).length; index++) {
            if (flower.get(petal.ordinal())[index] != 0) {
                size++;
            }
        }
        return size;
    }

    /**
     * Return the sum of the values in the array from the specified petal.
     *
     * @param petal the petal in which do the sum
     * @return the sum of the values of it array
     */
    private int getSum(Petal petal) {
        int sum = 0;
        for (int value : flower.get(petal.ordinal())) {
            sum += value;
        }
        return sum;
    }

    /**
     * Return {@code true} if the specified petal is full.
     * <p>
     * A petal is full if its array contains {@code petal.getCapacity()} elements.
     *
     * @param petal the petal to check
     * @return {@code true} if the petal is full, {@code false} else
     * @see Petal
     */
    public boolean petalIsFull(Petal petal) {
        return size(petal) == petal.getCapacity();
    }

    /**
     * Return {@code true} if the specified petal is complete.
     * <p>
     * A petal is complete if the sum of values in its array equals the value of the pistil.
     *
     * @param petal the petal to check
     * @return {@code true} if the petal is complete, {@code false} else
     */
    public boolean petalIsComplete(Petal petal) {
        return getSum(petal) == pistil;
    }

    /**
     * Return {@code true} if the flower is full.
     * <p>
     * The flower is full if all its petals are full,
     * so if all petal's arrays contain {@code petal.getCapacity()} elements.
     *
     * @return {@code true} if the flower is full, {@code false} else
     * @see Petal
     */
    public boolean isFull() {
        for (Petal petal : Petal.values()) {
            if (!petalIsFull(petal)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return {@code true} if the flower is full.
     * <p>
     * The flower is complete if all its petals are complete,
     * so if all sums of values in petal's arrays equal the value of the pistil.
     *
     * @return {@code true} if the flower is complete, {@code false} else
     */
    public boolean isComplete() {
        for (Petal petal : Petal.values()) {
            if (!petalIsComplete(petal)) {
                return false;
            }
        }
        return true;
    }
}
