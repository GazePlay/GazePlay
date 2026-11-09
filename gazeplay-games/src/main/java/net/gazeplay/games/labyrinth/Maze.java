package net.gazeplay.games.labyrinth;

import lombok.Getter;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A labyrinth of boxes with walls between them, drawn at random (review of 9/11/2026): a "perfect" labyrinth, where one
 * path only joins two boxes, dug from a box by going on at random and coming back when stuck. It keeps the order in which
 * the walls were opened, for the animation of the digging.
 */
final class Maze {

    /**
     * A direction: its move in lines and columns, and the angle of a picture facing it (0: up).
     */
    enum Direction {
        UP(-1, 0, 0), RIGHT(0, 1, 90), DOWN(1, 0, 180), LEFT(0, -1, 270);

        final int lines;
        final int columns;
        final double angle;

        Direction(final int lines, final int columns, final double angle) {
            this.lines = lines;
            this.columns = columns;
            this.angle = angle;
        }
    }

    @Getter
    private final int lines;
    @Getter
    private final int columns;
    /**
     * Whether the wall on the right of a box (or under it) is open.
     */
    private final boolean[][] rightOpen;
    private final boolean[][] downOpen;
    /**
     * The walls opened, in order: line, column, and 0 for the wall on the right of the box, 1 for the wall under it.
     */
    @Getter
    private final List<int[]> openings = new ArrayList<>();

    Maze(final int lines, final int columns, final ReplayablePseudoRandom random) {
        this.lines = lines;
        this.columns = columns;
        rightOpen = new boolean[lines][columns];
        downOpen = new boolean[lines][columns];
        final boolean[][] visited = new boolean[lines][columns];
        final Deque<int[]> path = new ArrayDeque<>();
        final int[] start = {random.nextInt(lines), random.nextInt(columns)};
        visited[start[0]][start[1]] = true;
        path.push(start);
        final List<Direction> choices = new ArrayList<>();
        while (!path.isEmpty()) {
            final int[] box = path.peek();
            choices.clear();
            for (final Direction direction : Direction.values()) {
                final int line = box[0] + direction.lines;
                final int column = box[1] + direction.columns;
                if (inside(line, column) && !visited[line][column]) {
                    choices.add(direction);
                }
            }
            if (choices.isEmpty()) {
                path.pop();
                continue;
            }
            final Direction direction = choices.get(random.nextInt(choices.size()));
            open(box[0], box[1], direction);
            final int[] next = {box[0] + direction.lines, box[1] + direction.columns};
            visited[next[0]][next[1]] = true;
            path.push(next);
        }
    }

    private void open(final int line, final int column, final Direction direction) {
        switch (direction) {
            case RIGHT -> {
                rightOpen[line][column] = true;
                openings.add(new int[]{line, column, 0});
            }
            case LEFT -> {
                rightOpen[line][column - 1] = true;
                openings.add(new int[]{line, column - 1, 0});
            }
            case DOWN -> {
                downOpen[line][column] = true;
                openings.add(new int[]{line, column, 1});
            }
            case UP -> {
                downOpen[line - 1][column] = true;
                openings.add(new int[]{line - 1, column, 1});
            }
        }
    }

    boolean inside(final int line, final int column) {
        return line >= 0 && line < lines && column >= 0 && column < columns;
    }

    /**
     * @return whether one can go from a box in a direction (inside the labyrinth, no wall)
     */
    boolean canGo(final int line, final int column, final Direction direction) {
        if (!inside(line, column) || !inside(line + direction.lines, column + direction.columns)) {
            return false;
        }
        return switch (direction) {
            case RIGHT -> rightOpen[line][column];
            case LEFT -> rightOpen[line][column - 1];
            case DOWN -> downOpen[line][column];
            case UP -> downOpen[line - 1][column];
        };
    }

    /**
     * @return the number of moves from a box to each box
     */
    int[][] distancesFrom(final int line, final int column) {
        final int[][] distances = new int[lines][columns];
        for (final int[] row : distances) {
            java.util.Arrays.fill(row, -1);
        }
        final Deque<int[]> queue = new ArrayDeque<>();
        distances[line][column] = 0;
        queue.add(new int[]{line, column});
        while (!queue.isEmpty()) {
            final int[] box = queue.poll();
            for (final Direction direction : Direction.values()) {
                if (canGo(box[0], box[1], direction)) {
                    final int nextLine = box[0] + direction.lines;
                    final int nextColumn = box[1] + direction.columns;
                    if (distances[nextLine][nextColumn] < 0) {
                        distances[nextLine][nextColumn] = distances[box[0]][box[1]] + 1;
                        queue.add(new int[]{nextLine, nextColumn});
                    }
                }
            }
        }
        return distances;
    }
}
