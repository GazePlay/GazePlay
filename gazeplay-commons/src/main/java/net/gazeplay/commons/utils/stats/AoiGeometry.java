package net.gazeplay.commons.utils.stats;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Geometry of the areas of interest. These methods were duplicated, identically, in Stats and in AreaOfInterest.
 */
public final class AoiGeometry {

    private AoiGeometry() {
    }

    public static Double[] calculateRectangle(final Point2D[] point2D) {
        final double[] xs = new double[point2D.length];
        final double[] ys = new double[point2D.length];
        for (int i = 0; i < point2D.length; i++) {
            xs[i] = point2D[i].getX();
            ys[i] = point2D[i].getY();
        }
        return AreaOfInterestGrouper.rectangle(xs, ys);
    }

    public static Double[] calculateConvexHull(final Point2D[] points) {
        final int numberOfPoints = points.length;
        final ArrayList<Double> convexHullPoints = new ArrayList<>();
        final Vector<Point2D> hull = new Vector<>();

        // Finding the index of the lowest X value, or left-most point, in all points.
        int lowestValueIndex = 0;
        for (int i = 1; i < numberOfPoints; i++) {
            if (points[i].getX() < points[lowestValueIndex].getX()) {
                lowestValueIndex = i;
            }
        }

        int point = lowestValueIndex, q;
        do {
            hull.add(points[point]);
            q = (point + 1) % numberOfPoints;
            for (int i = 0; i < numberOfPoints; i++) {
                if (orientation(points[point], points[i], points[q]) < 0) { // Checking if the points are convex.
                    q = i;
                }
            }
            point = q;
        } while (point != lowestValueIndex);

        for (final Point2D temp : hull) {
            convexHullPoints.add(temp.getX());
            convexHullPoints.add(temp.getY());
        }

        Double[] hullPointsArray = new Double[convexHullPoints.size()];
        convexHullPoints.toArray(hullPointsArray);

        return hullPointsArray;
    }

    public static int orientation(final Point2D p1, final Point2D p2, final Point2D p3) {

        final int val = (int) ((p2.getY() - p1.getY()) * (p3.getX() - p2.getX())
            - (p2.getX() - p1.getX()) * (p3.getY() - p2.getY()));

        if (val == 0) {
            return 0;
        }

        return (val > 0) ? 1 : -1;
    }
}
