package net.gazeplay.games.flowernumbers;

import java.util.ArrayList;
import java.util.List;

public class Flower {
    private final List<List<Integer>> flower;
    private int pistil;

    Flower() {
        flower = new ArrayList<>(Petal.values().length);
        for (Petal ignored : Petal.values()) {
            flower.add(new ArrayList<>(5));
        }
    }

    void init() {
        pistil = 0;
        for (List<Integer> petal : flower) {
            petal.clear();
        }
    }

    int getPistil() {
        return pistil;
    }

    void setPistil(int value) {
        pistil = value;
    }

    int get(Petal petal, int index) {
        return flower.get(petal.ordinal()).get(index);
    }

    void add(Petal petal, int value) {
        flower.get(petal.ordinal()).add(value);
    }

    void remove(Petal petal, int index) {
        flower.get(petal.ordinal()).remove(index);
    }

    void clear(Petal petal) {
        flower.get(petal.ordinal()).clear();
    }

    int getSum(Petal petal) {
        int sum = 0;
        for (int value : flower.get(petal.ordinal())) {
            sum += value;
        }
        return sum;
    }

    boolean petalIsComplete(Petal petal) {
        return getSum(petal) == pistil;
    }

    boolean isComplete() {
        for (Petal petal : Petal.values()) {
            if (getSum(petal) != pistil) {
                return false;
            }
        }
        return true;
    }
}
