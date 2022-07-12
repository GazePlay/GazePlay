package net.gazeplay.games.flowerOfNumbers;

import java.util.ArrayList;
import java.util.List;

public class Flower {
    private final static int wordPetalCapacity = 1;
    private final static int petalCapacity = 4;

    private final List<List<Integer>> flower;
    private int pistil;

    Flower() {
        flower = new ArrayList<>(Petal.values().length);
        for (Petal petal : Petal.values()) {
            if (petal == Petal.WORDS) {
                flower.add(new ArrayList<>(wordPetalCapacity));
            } else {
                flower.add(new ArrayList<>(petalCapacity));
            }
        }
    }

    /**
     * Initialize the flower: empty all the petal's lists and initialize the value of the pistil to 0.
     */
    void init() {
        pistil = 0;
        for (List<Integer> petal : flower) {
            petal.clear();
        }
    }

    /**
     * Return the value of the pistil, is the number to find on petals.
     *
     * @return the value of the pistil
     */
    int getPistil() {
        return pistil;
    }

    /**
     * Set the value of the pistil, this new value will be the one to find on petals.
     *
     * @param value the new value of the pistil
     */
    void setPistil(int value) {
        pistil = value;
    }

    /**
     * Return the value in the list from the specified petal at the specified index.
     *
     * @param petal the petal in which get the value
     * @param index the index of the value to get
     * @return the value on petal's list
     */
    int get(Petal petal, int index) {
        return flower.get(petal.ordinal()).get(index);
    }

    /**
     * Add a value in the list from the specified petal.
     *
     * @param petal the petal in which add the value
     * @param value the value to add
     */
    void add(Petal petal, int value) {
        flower.get(petal.ordinal()).add(value);
    }

    /**
     * Remove the value in the list from the specified petal.
     *
     * @param petal the petal in which remove the value
     * @param value the value to remove
     */
    void remove(Petal petal, int value) {
        flower.get(petal.ordinal()).remove((Integer) value);
    }

    /**
     * Return the size of the list from the specified petal.
     *
     * @param petal the petal to calculate the size
     * @return the size of it list
     */
    int size(Petal petal) {
        return flower.get(petal.ordinal()).size();
    }

    /**
     * Return the sum of the values in the list from the specified petal.
     *
     * @param petal the petal in which do the sum
     * @return the sum of the values of it list
     */
    int getSum(Petal petal) {
        int sum = 0;
        for (int value : flower.get(petal.ordinal())) {
            sum += value;
        }
        return sum;
    }

    /**
     * Return {@code true} if the specified petal is full.
     * <p>
     * A petal is full if its list contains {@value petalCapacity} elements or {@value wordPetalCapacity} for the petal of words.
     *
     * @param petal the petal to check
     * @return {@code true} if the petal is full, {@code false} else
     */
    boolean petalIsFull(Petal petal) {
        return size(petal) == (petal == Petal.WORDS ? wordPetalCapacity : petalCapacity);
    }

    /**
     * Return {@code true} if the specified petal is complete.
     * <p>
     * A petal is complete if the sum of values in its list equals the value of the pistil.
     *
     * @param petal the petal to check
     * @return {@code true} if the petal is complete, {@code false} else
     */
    boolean petalIsComplete(Petal petal) {
        return getSum(petal) == pistil;
    }

    /**
     * Return {@code true} if the flower is full.
     * <p>
     * The flower is full if all its petals are full,
     * so if all petal's lists contain {@value petalCapacity} elements and {@value wordPetalCapacity} for the petal of words.
     *
     * @return {@code true} if the flower is full, {@code false} else
     */
    boolean isFull() {
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
     * so if all sums of values in petal's lists equal the value of the pistil.
     *
     * @return {@code true} if the flower is complete, {@code false} else
     */
    boolean isComplete() {
        for (Petal petal : Petal.values()) {
            if (!petalIsComplete(petal)) {
                return false;
            }
        }
        return true;
    }
}
