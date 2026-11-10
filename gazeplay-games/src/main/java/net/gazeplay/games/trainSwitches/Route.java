package net.gazeplay.games.trainSwitches;

import java.util.ArrayList;
import java.util.List;

/**
 * Rails through boxes of the grid, in straight lines, the corners rounded (a quarter of a circle): the way of a train,
 * or a stretch of rails to draw. Its positions are in boxes, measured along the rails from the first box.
 */
final class Route {

    /**
     * The radius of the rounded corners, in boxes.
     */
    static final double CORNER = 0.3;

    private final List<double[]> boxes = new ArrayList<>();
    private final List<Piece> pieces = new ArrayList<>();
    private double length;

    Route(final List<int[]> points) {
        points.forEach(this::add);
    }

    /**
     * Adds a box at the end (in the same line or the same column as the last one); the corner of the last box is
     * rounded.
     */
    void add(final int[] box) {
        if (!boxes.isEmpty()) {
            final double[] last = boxes.get(boxes.size() - 1);
            if (last[0] == box[0] && last[1] == box[1]) {
                return;
            }
        }
        boxes.add(new double[]{box[0], box[1]});
        build();
    }

    /**
     * Adds the boxes of a stretch, which starts at the last box.
     */
    void append(final List<int[]> stretch) {
        stretch.forEach(this::add);
    }

    double length() {
        return length;
    }

    /**
     * @return the last box
     */
    double[] end() {
        return boxes.get(boxes.size() - 1);
    }

    private void build() {
        pieces.clear();
        length = 0;
        if (boxes.size() < 2) {
            return;
        }
        double[] from = boxes.get(0);
        for (int i = 1; i < boxes.size(); i++) {
            final double[] corner = boxes.get(i);
            final double[] in = direction(boxes.get(i - 1), corner);
            if (i == boxes.size() - 1) {
                addLine(from, corner);
                break;
            }
            final double[] out = direction(corner, boxes.get(i + 1));
            if (in[0] == out[0] && in[1] == out[1]) {
                continue;
            }
            final double[] before = {corner[0] - CORNER * in[0], corner[1] - CORNER * in[1]};
            addLine(from, before);
            final Piece arc = new Piece();
            arc.start = before;
            arc.in = in;
            arc.out = out;
            arc.center = new double[]{corner[0] - CORNER * in[0] + CORNER * out[0],
                corner[1] - CORNER * in[1] + CORNER * out[1]};
            arc.length = Math.PI / 2 * CORNER;
            add(arc);
            from = new double[]{corner[0] + CORNER * out[0], corner[1] + CORNER * out[1]};
        }
    }

    private void addLine(final double[] from, final double[] to) {
        final double size = Math.hypot(to[0] - from[0], to[1] - from[1]);
        if (size < 1e-9) {
            return;
        }
        final Piece line = new Piece();
        line.start = from;
        line.in = new double[]{(to[0] - from[0]) / size, (to[1] - from[1]) / size};
        line.length = size;
        add(line);
    }

    private void add(final Piece piece) {
        piece.from = length;
        pieces.add(piece);
        length += piece.length;
    }

    private static double[] direction(final double[] from, final double[] to) {
        return new double[]{Math.signum(to[0] - from[0]), Math.signum(to[1] - from[1])};
    }

    /**
     * @return the point at a position along the rails, and the angle of the rails there in degrees (0 to the right, 90
     * down): {x, y, angle}. Before the start, the rails go on straight backwards; after the end, the last point.
     */
    double[] at(final double position) {
        if (pieces.isEmpty()) {
            final double[] box = boxes.get(0);
            return new double[]{box[0], box[1], 0};
        }
        if (position <= 0) {
            final Piece first = pieces.get(0);
            return new double[]{first.start[0] + position * first.in[0], first.start[1] + position * first.in[1],
                angle(first.in)};
        }
        Piece piece = pieces.get(pieces.size() - 1);
        for (final Piece candidate : pieces) {
            if (position <= candidate.from + candidate.length) {
                piece = candidate;
                break;
            }
        }
        final double along = Math.min(position - piece.from, piece.length);
        if (piece.center == null) {
            return new double[]{piece.start[0] + along * piece.in[0], piece.start[1] + along * piece.in[1],
                angle(piece.in)};
        }
        final double theta = along / CORNER;
        final double cos = Math.cos(theta);
        final double sin = Math.sin(theta);
        final double x = piece.center[0] + CORNER * (-piece.out[0] * cos + piece.in[0] * sin);
        final double y = piece.center[1] + CORNER * (-piece.out[1] * cos + piece.in[1] * sin);
        final double[] tangent = {piece.out[0] * sin + piece.in[0] * cos, piece.out[1] * sin + piece.in[1] * cos};
        return new double[]{x, y, angle(tangent)};
    }

    private static double angle(final double[] direction) {
        return Math.toDegrees(Math.atan2(direction[1], direction[0]));
    }

    /**
     * @return points along the rails, every step at most (in boxes), the first and the last ones included
     */
    List<double[]> sample(final double step) {
        final List<double[]> points = new ArrayList<>();
        final int count = Math.max(1, (int) Math.ceil(length / step));
        for (int i = 0; i <= count; i++) {
            points.add(at(length * i / count));
        }
        return points;
    }

    /**
     * A straight line, or a quarter of a circle (with a center) from the direction in to the direction out.
     */
    private static final class Piece {
        private double[] start;
        private double[] in;
        private double[] out;
        private double[] center;
        private double length;
        private double from;
    }
}
