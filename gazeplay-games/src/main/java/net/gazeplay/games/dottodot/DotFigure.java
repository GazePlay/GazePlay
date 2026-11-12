package net.gazeplay.games.dottodot;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * A figure of "Point par point" (data/dottodot/figures.json, made by gradle/images/make_dottodot_figures.py): its name
 * (a key of translation, "a fish"), its level (EASY, NORMAL or HARD), the width of its box on its height, its fill
 * color, and its dots in order, from 0 to 1 in its box.
 */
public record DotFigure(String name, String level, double aspect, String color, List<double[]> points) {

    static final String FILE = "data/dottodot/figures.json";

    /**
     * @return all the figures
     */
    public static List<DotFigure> all() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(
            DotFigure.class.getClassLoader().getResourceAsStream(FILE)), StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, new TypeToken<List<DotFigure>>() {
            }.getType());
        } catch (final java.io.IOException e) {
            throw new IllegalStateException("Figures not read: " + FILE, e);
        }
    }
}
