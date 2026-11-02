package net.gazeplay.games.flowerOfNumbers;

/**
 * The petals of the flower: the kind of representation of the numbers they take, how many values are proposed for them,
 * how many they can hold, their color (also the border of the card of their values) and their direction from the
 * center of the flower (degrees, clockwise from the right; the top petal at -90).
 */
public enum Petal {
    DIGITS(9, 4, "#00AE4F", -90),
    WORDS(20, 1, "#D166A6", 198),
    FINGERS(5, 4, "#00A3E0", 126),
    DICE(6, 4, "#8CC63F", 54),
    MONEY(4, 4, "#F07C98", -18);

    /**
     * How many number objects are associated with this petal.
     */
    private final int numberOfObjects;

    /**
     * How many number objects the petal can contain.
     */
    private final int capacity;

    private final String color;

    private final double angle;

    Petal(final int numberOfObjects, final int capacity, final String color, final double angle) {
        this.numberOfObjects = numberOfObjects;
        this.capacity = capacity;
        this.color = color;
        this.angle = angle;
    }

    public int getNumberOfObjects() {
        return numberOfObjects;
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * @return the color of the petal (CSS)
     */
    public String getColor() {
        return color;
    }

    /**
     * @return the direction of the petal from the center of the flower, in degrees, clockwise from the right
     */
    public double getAngle() {
        return angle;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
