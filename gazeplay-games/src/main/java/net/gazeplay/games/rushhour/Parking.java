package net.gazeplay.games.rushhour;

import java.util.ArrayList;
import java.util.List;

/**
 * A parking of "Embouteillage": 6 x 6 boxes, the red car (the first vehicle) on the third line, leaving by the right
 * side. Each vehicle slides along its direction only, by as many free boxes as wanted.
 */
final class Parking {

    static final int SIZE = 6;
    static final int EXIT_LINE = 2;
    static final char EMPTY = '.';
    static final char RED = 'A';

    /**
     * A vehicle: lying (horizontal) or standing, of 2 or 3 boxes, on a line (lying) or a column (standing).
     */
    static final class Vehicle {
        final char name;
        final boolean horizontal;
        final int length;
        final int fixed;
        final int start;
        int position;

        Vehicle(final char name, final boolean horizontal, final int length, final int fixed, final int position) {
            this.name = name;
            this.horizontal = horizontal;
            this.length = length;
            this.fixed = fixed;
            this.start = position;
            this.position = position;
        }

        int line(final int box) {
            return horizontal ? fixed : position + box;
        }

        int column(final int box) {
            return horizontal ? position + box : fixed;
        }

        boolean covers(final int line, final int column) {
            for (int box = 0; box < length; box++) {
                if (line(box) == line && column(box) == column) {
                    return true;
                }
            }
            return false;
        }
    }

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final int fewestMoves;

    /**
     * @param boxes the 36 boxes, line by line from the top: "." empty, "A" the red car, other letters the other
     *              vehicles
     */
    Parking(final String boxes, final int fewestMoves) {
        if (boxes.length() != SIZE * SIZE) {
            throw new IllegalArgumentException("A parking has " + SIZE * SIZE + " boxes: " + boxes);
        }
        this.fewestMoves = fewestMoves;
        final List<Character> names = new ArrayList<>();
        for (final char name : boxes.toCharArray()) {
            if (name != EMPTY && !names.contains(name)) {
                names.add(name);
            }
        }
        names.sort(null);
        for (final char name : names) {
            int first = boxes.indexOf(name);
            final int last = boxes.lastIndexOf(name);
            final boolean horizontal = last - first < SIZE;
            final int length = horizontal ? last - first + 1 : (last - first) / SIZE + 1;
            vehicles.add(horizontal
                ? new Vehicle(name, true, length, first / SIZE, first % SIZE)
                : new Vehicle(name, false, length, first % SIZE, first / SIZE));
        }
        if (vehicles.isEmpty() || vehicles.get(0).name != RED || !vehicles.get(0).horizontal
            || vehicles.get(0).fixed != EXIT_LINE) {
            throw new IllegalArgumentException("The red car must lie on the exit line: " + boxes);
        }
    }

    List<Vehicle> getVehicles() {
        return vehicles;
    }

    Vehicle red() {
        return vehicles.get(0);
    }

    int getFewestMoves() {
        return fewestMoves;
    }

    /**
     * @return whether a box is free (inside the parking, no vehicle on it), the vehicle given aside
     */
    boolean isFree(final int line, final int column, final Vehicle aside) {
        if (line < 0 || line >= SIZE || column < 0 || column >= SIZE) {
            return false;
        }
        for (final Vehicle vehicle : vehicles) {
            if (vehicle != aside && vehicle.covers(line, column)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the positions a vehicle can slide to, in one move, nearest first on each side
     */
    List<Integer> reachable(final Vehicle vehicle) {
        final List<Integer> positions = new ArrayList<>();
        for (final int way : new int[]{-1, 1}) {
            int position = vehicle.position + way;
            while (position >= 0 && position + vehicle.length <= SIZE) {
                // The box the vehicle enters at its front (or at its back going backwards)
                final int entered = way < 0 ? position : position + vehicle.length - 1;
                final int line = vehicle.horizontal ? vehicle.fixed : entered;
                final int column = vehicle.horizontal ? entered : vehicle.fixed;
                if (!isFree(line, column, vehicle)) {
                    break;
                }
                positions.add(position);
                position += way;
            }
        }
        return positions;
    }

    /**
     * @return the box (line, column) that shows a position a vehicle can slide to: the last box it enters
     */
    static int[] markOf(final Vehicle vehicle, final int position) {
        final int box = position < vehicle.position ? position : position + vehicle.length - 1;
        return vehicle.horizontal ? new int[]{vehicle.fixed, box} : new int[]{box, vehicle.fixed};
    }

    /**
     * Slides a vehicle, if it can go there in one move.
     *
     * @return whether it moved
     */
    boolean slide(final Vehicle vehicle, final int position) {
        if (!reachable(vehicle).contains(position)) {
            return false;
        }
        vehicle.position = position;
        return true;
    }

    /**
     * @return whether the red car is at the exit
     */
    boolean isSolved() {
        return red().position == SIZE - red().length;
    }

    /**
     * Puts every vehicle back at its start.
     */
    void restart() {
        vehicles.forEach(vehicle -> vehicle.position = vehicle.start);
    }

    /**
     * @return the boxes, line by line, as in the file of the parkings
     */
    String boxes() {
        final char[] boxes = new char[SIZE * SIZE];
        java.util.Arrays.fill(boxes, EMPTY);
        for (final Vehicle vehicle : vehicles) {
            for (int box = 0; box < vehicle.length; box++) {
                boxes[vehicle.line(box) * SIZE + vehicle.column(box)] = vehicle.name;
            }
        }
        return new String(boxes);
    }
}
