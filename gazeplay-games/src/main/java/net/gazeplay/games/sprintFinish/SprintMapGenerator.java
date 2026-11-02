package net.gazeplay.games.sprintFinish;

import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.games.sprintFinish.SprintMap.Button;
import net.gazeplay.games.sprintFinish.SprintMap.Cell;
import net.gazeplay.games.sprintFinish.SprintMap.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Makes the maps of "Sprint Finish" at random (2/11/2026, maintainer's choice): each map follows a model taken from
 * the 17 levels of before, its places drawn at random, and is checked playable (see {@link SprintMap#isPlayable()}).
 * The models come in this order along a game, so that it gets harder: an open meadow, a hedge with a gap, a hedge with
 * a fence opened by a button, the bowl in a fenced enclosure, then logs going up and down. There are more dogs in the
 * second half of a game, and in the last third of a hard game. The cat starts on the left, its bowl is on the right.
 */
public final class SprintMapGenerator {

    /**
     * The models of the maps, in the order of a game.
     */
    enum Model { MEADOW, GAP, GATE, ENCLOSURE, LOGS }

    private static final int TRIES = 200;

    private SprintMapGenerator() {
    }

    /**
     * @return the model of the map at an index of a game of {@code count} maps
     */
    static Model modelOf(final int index, final int count) {
        return Model.values()[Math.min(Model.values().length - 1, index * Model.values().length / count)];
    }

    /**
     * @return the number of dogs of the map at an index of a game of {@code count} maps
     */
    static int dogsOf(final int index, final int count) {
        int dogs = 1 + 2 * index / count;
        if (count >= 15 && index >= 2 * count / 3) {
            dogs++;
        }
        return dogs;
    }

    /**
     * @return the map at an index of a game of {@code count} maps
     */
    public static SprintMap generate(final int index, final int count, final ReplayablePseudoRandom random) {
        final Model model = modelOf(index, count);
        final int dogs = dogsOf(index, count);
        for (int attempt = 0; attempt < TRIES; attempt++) {
            final SprintMap map = attempt(model, dogs, index, random);
            if (map != null && map.isPlayable()) {
                return map;
            }
        }
        // Never seen in the tests: an open meadow, one dog far away
        final SprintMap map = new SprintMap(new Cell(1, 2), new Cell(14, 6));
        map.getDogs().add(new Cell(13, 1));
        return map;
    }

    private static SprintMap attempt(final Model model, final int dogs, final int index,
                                     final ReplayablePseudoRandom random) {
        final Cell cat = new Cell(random.nextInt(2), 1 + random.nextInt(5));
        final SprintMap map;
        int dogsFromColumn = 6;
        switch (model) {
            case GAP -> {
                map = new SprintMap(cat, new Cell(14 + random.nextInt(2), 1 + random.nextInt(7)));
                final int column = 6 + random.nextInt(4);
                final int gap = random.nextInt(SprintMap.ROWS - 1);
                for (int row = 0; row < SprintMap.ROWS; row++) {
                    map.setHedge(column, row, row != gap && row != gap + 1);
                }
                bushes(map, 2 + random.nextInt(2), random);
            }
            case GATE -> {
                map = new SprintMap(cat, new Cell(14 + random.nextInt(2), 1 + random.nextInt(7)));
                final int column = 7 + random.nextInt(2);
                final int gate = random.nextInt(SprintMap.ROWS - 1);
                for (int row = 0; row < SprintMap.ROWS; row++) {
                    map.setHedge(column, row, row != gate && row != gate + 1);
                }
                final Cell button = new Cell(2 + random.nextInt(column - 3), 1 + random.nextInt(7));
                map.getButtons().add(new Button(button, List.of(new Cell(column, gate), new Cell(column, gate + 1)),
                    0));
                bushes(map, 1 + random.nextInt(2), random);
                // The dogs wait behind the fence
                dogsFromColumn = column + 1;
            }
            case ENCLOSURE -> {
                final int row = 2 + random.nextInt(5);
                map = new SprintMap(cat, new Cell(13, row));
                for (int column = 12; column <= 14; column++) {
                    for (int r = row - 1; r <= row + 1; r++) {
                        map.setHedge(column, r, !(column == 13 && r == row));
                    }
                }
                // The way in, on the left side, closed by a fence
                map.setHedge(12, row, false);
                final Cell button = new Cell(3 + random.nextInt(8), 1 + random.nextInt(7));
                map.getButtons().add(new Button(button, List.of(new Cell(12, row)), 1));
                bushes(map, 2 + random.nextInt(2), random);
                if (index % 2 != 0) {
                    // Later on: a hedge with a gap too
                    final int column = 7;
                    final int gap = random.nextInt(SprintMap.ROWS - 1);
                    for (int r = 0; r < SprintMap.ROWS; r++) {
                        map.setHedge(column, r, r != gap && r != gap + 1);
                    }
                }
            }
            case LOGS -> {
                map = new SprintMap(cat, new Cell(14 + random.nextInt(2), 1 + random.nextInt(7)));
                map.getLogs().add(new Log(6 + random.nextInt(2), 2, random.nextDouble()));
                if (index % 2 != 0) {
                    map.getLogs().add(new Log(9 + random.nextInt(2), 2, random.nextDouble()));
                }
                bushes(map, 2, random);
            }
            default -> {
                map = new SprintMap(cat, new Cell(14 + random.nextInt(2), 1 + random.nextInt(7)));
                bushes(map, 4 + random.nextInt(3), random);
            }
        }
        // Nothing on the cat nor on the bowl
        map.setHedge(cat.column(), cat.row(), false);
        map.setHedge(map.getDish().column(), map.getDish().row(), false);
        return placeDogs(map, dogs, dogsFromColumn, random) ? map : null;
    }

    /**
     * Puts bushes (hedges of one cell) at random, away from the start of the cat and from the bowl.
     */
    private static void bushes(final SprintMap map, final int count, final ReplayablePseudoRandom random) {
        for (int i = 0; i < count; i++) {
            final int column = 3 + random.nextInt(SprintMap.COLUMNS - 6);
            final int row = random.nextInt(SprintMap.ROWS);
            if (!map.isFence(column, row) && !map.isLogColumn(column)
                && map.getButtons().stream().noneMatch(button -> button.cell().equals(new Cell(column, row)))) {
                map.setHedge(column, row, true);
            }
        }
    }

    /**
     * Puts the dogs at random, from a column on, far enough from the cat (7 steps at least), from its bowl (not waiting
     * next to it) and from each other.
     *
     * @return false if they cannot be placed
     */
    private static boolean placeDogs(final SprintMap map, final int count, final int fromColumn,
                                     final ReplayablePseudoRandom random) {
        final int[][] distances = map.distancesFrom(map.getCat(), List.of());
        final List<Cell> placed = new ArrayList<>();
        int tries = 0;
        while (placed.size() < count && tries++ < 300) {
            final int column = fromColumn + random.nextInt(SprintMap.COLUMNS - fromColumn);
            // Not on the first row, under the count of the maps
            final int row = 1 + random.nextInt(SprintMap.ROWS - 1);
            final Cell cell = new Cell(column, row);
            if (distances[column][row] >= 7 && !map.isHedge(column, row) && !map.isFence(column, row)
                && !map.isLogColumn(column)
                && Math.abs(map.getDish().column() - column) + Math.abs(map.getDish().row() - row) >= 3
                && map.getButtons().stream().noneMatch(button -> button.cell().equals(cell))
                && placed.stream().noneMatch(other -> Math.abs(other.column() - column)
                + Math.abs(other.row() - row) < 2)) {
                placed.add(cell);
            }
        }
        map.getDogs().addAll(placed);
        return placed.size() == count;
    }
}
