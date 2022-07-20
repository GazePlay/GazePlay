package net.gazeplay.games.flowerOfNumbers;

public enum Petal {
    DIGITS(9, 4),
    WORDS(20, 1),
    FINGERS(5, 4),
    DICE(6, 4),
    MONEY(4, 4);

    /**
     * How many number objects are associated with this petal.
     */
    private final int numberOfObjects;

    /**
     * How many number objects the petal can contain.
     */
    private final int capacity;

    Petal(int numberOfObjects, int capacity) {
        this.numberOfObjects = numberOfObjects;
        this.capacity = capacity;
    }

    int getNumberOfObjects() {
        return numberOfObjects;
    }

    int getCapacity() {
        return capacity;
    }
}
