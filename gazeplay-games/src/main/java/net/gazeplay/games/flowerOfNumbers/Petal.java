package net.gazeplay.games.flowerOfNumbers;

/**
 * The petals of the flower: the kind of representation of the numbers they take, how many values are proposed for them,
 * and how many they can hold.
 */
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

    Petal(final int numberOfObjects, final int capacity) {
        this.numberOfObjects = numberOfObjects;
        this.capacity = capacity;
    }

    public int getNumberOfObjects() {
        return numberOfObjects;
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * @return the number of values proposed for the petals before this one
     */
    public static int getTotalNumberOfObjectsBeforePetal(final Petal petal) {
        int res = 0;
        for (int index = 0; index < petal.ordinal(); index++) {
            res += Petal.values()[index].getNumberOfObjects();
        }
        return res;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
