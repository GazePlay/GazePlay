package net.gazeplay.games.flowerOfNumbers;

public enum Petal {
    DIGITS(4),
    WORDS(1),
    FINGERS(4),
    DICE(4),
    MONEY(4);

    private final int capacity;

    Petal(int capacity) {
        this.capacity = capacity;
    }

    int getCapacity() {
        return capacity;
    }
}
