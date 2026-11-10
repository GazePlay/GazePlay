package net.gazeplay.games.trainSwitches;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A circuit of "Aiguillages", read in data/trainSwitches/circuits.txt (format given at the top of the file): a grid of
 * boxes, a tunnel the trains come out of, the switches, each with two stretches of rails going out, and the stations at
 * the ends of the rails.
 */
@Getter
final class Circuit {

    static final String FILE = "data/trainSwitches/circuits.txt";
    private static Map<String, List<Circuit>> circuits;

    private final String level;
    private final int columns;
    private final int lines;
    private final int[] tunnel;
    /**
     * The stretch out of the tunnel.
     */
    private Stretch first;
    private final Map<String, Junction> junctions = new LinkedHashMap<>();
    private final List<int[]> stations = new ArrayList<>();
    private final List<Stretch> stretches = new ArrayList<>();

    private Circuit(final String level, final int columns, final int lines, final int[] tunnel) {
        this.level = level;
        this.columns = columns;
        this.lines = lines;
        this.tunnel = tunnel;
    }

    /**
     * @return the circuits of the file, by level
     */
    static synchronized Map<String, List<Circuit>> all() {
        if (circuits == null) {
            final InputStream input = Circuit.class.getClassLoader().getResourceAsStream(FILE);
            if (input == null) {
                throw new IllegalStateException("Missing " + FILE);
            }
            final List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (final IOException e) {
                throw new IllegalStateException("Cannot read " + FILE, e);
            }
            circuits = read(lines);
        }
        return circuits;
    }

    /**
     * @return the circuits of lines of the format of the file, by level
     */
    static Map<String, List<Circuit>> read(final List<String> text) {
        final Map<String, List<Circuit>> read = new HashMap<>();
        Circuit circuit = null;
        for (final String raw : text) {
            final String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            final String[] parts = line.split("\\s+");
            if (parts[0].equals("circuit")) {
                circuit = new Circuit(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), box(parts[4]));
                read.computeIfAbsent(parts[1], key -> new ArrayList<>()).add(circuit);
            } else if (circuit != null) {
                circuit.addStretch(parts);
            } else {
                throw new IllegalArgumentException("A stretch before any circuit: " + line);
            }
        }
        return read;
    }

    private static int[] box(final String text) {
        final String[] xy = text.split(",");
        return new int[]{Integer.parseInt(xy[0]), Integer.parseInt(xy[1])};
    }

    private void addStretch(final String[] parts) {
        final int[] start;
        if (parts[0].equals("T")) {
            start = tunnel;
        } else {
            final Junction from = junctions.get(parts[0]);
            if (from == null) {
                throw new IllegalArgumentException("Unknown switch " + parts[0] + " in " + String.join(" ", parts));
            }
            start = from.box;
        }
        final List<int[]> boxes = new ArrayList<>();
        boxes.add(start);
        for (int i = 1; i < parts.length - 1; i++) {
            boxes.add(box(parts[i]));
        }
        if (boxes.size() < 2) {
            throw new IllegalArgumentException("An empty stretch: " + String.join(" ", parts));
        }
        final String to = parts[parts.length - 1];
        final int[] last = boxes.get(boxes.size() - 1);
        final Stretch stretch;
        if (Character.isDigit(to.charAt(0))) {
            final int station = Integer.parseInt(to);
            while (stations.size() <= station) {
                stations.add(null);
            }
            stations.set(station, last);
            stretch = new Stretch(boxes, null, station);
        } else {
            junctions.put(to, new Junction(to, last));
            stretch = new Stretch(boxes, to, -1);
        }
        stretches.add(stretch);
        if (parts[0].equals("T")) {
            first = stretch;
        } else {
            junctions.get(parts[0]).outs.add(stretch);
        }
    }

    /**
     * @return the direction (dx, dy) of the rails at the start of a stretch
     */
    static int[] startDirection(final Stretch stretch) {
        final int[] a = stretch.boxes.get(0);
        final int[] b = stretch.boxes.get(1);
        return new int[]{Integer.signum(b[0] - a[0]), Integer.signum(b[1] - a[1])};
    }

    /**
     * @return the direction (dx, dy) of the rails at the end of a stretch
     */
    static int[] endDirection(final Stretch stretch) {
        final int[] a = stretch.boxes.get(stretch.boxes.size() - 2);
        final int[] b = stretch.boxes.get(stretch.boxes.size() - 1);
        return new int[]{Integer.signum(b[0] - a[0]), Integer.signum(b[1] - a[1])};
    }

    /**
     * @return the stretch of rails arriving at a switch
     */
    Stretch arrivingAt(final String junction) {
        for (final Stretch stretch : stretches) {
            if (junction.equals(stretch.toJunction)) {
                return stretch;
            }
        }
        return null;
    }

    /**
     * @return the stretch of rails arriving at a station
     */
    Stretch arrivingAtStation(final int station) {
        for (final Stretch stretch : stretches) {
            if (stretch.toStation == station) {
                return stretch;
            }
        }
        return null;
    }

    /**
     * Rails in straight lines through boxes, from a switch or the tunnel, to a switch or a station.
     *
     * @param boxes      the boxes, the first one being the box of the switch or the tunnel it comes from
     * @param toJunction the letter of the switch at its end, null at a station
     * @param toStation  the station at its end, -1 at a switch
     */
    record Stretch(List<int[]> boxes, String toJunction, int toStation) {
    }

    /**
     * A switch: its box, and its two stretches going out.
     */
    @Getter
    static final class Junction {
        private final String name;
        private final int[] box;
        private final List<Stretch> outs = new ArrayList<>();

        Junction(final String name, final int[] box) {
            this.name = name;
            this.box = box;
        }
    }
}
