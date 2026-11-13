package net.gazeplay.games.colorblend;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * Paints mixed as a painter does (maintainer's choice of 13/11/2026): yellow and blue make green, red and yellow orange,
 * red and blue purple, the three together brown; white lightens and black darkens, by their share in the pot.
 * <p>
 * The red, yellow and blue of a mix give a point of the RYB cube, scaled so that its largest part is 1, turned into a
 * color by the trilinear interpolation of Gossett and Chen ("Paint Inspired Color Mixing and Compositing for
 * Visualization", IEEE InfoVis 2004); the corners are softened for the eyes of children. The free mode and the
 * challenge use the same mix: a color to make is always the one obtained.
 */
final class PaintMix {

    /**
     * The pots of paint.
     */
    @Getter
    @RequiredArgsConstructor
    enum Pot {
        RED("ColorBlendRed"), YELLOW("ColorBlendYellow"), BLUE("ColorBlendBlue"), WHITE("ColorBlendWhite"),
        BLACK("ColorBlendBlack");

        /**
         * Key of translation of its name.
         */
        private final String key;
    }

    /**
     * The colors to make, each with its recipe: two pots in the easy and normal levels, three in the hard one.
     */
    @Getter
    enum Target {
        ORANGE("ColorBlendOrange", Pot.RED, Pot.YELLOW),
        GREEN("ColorBlendGreen", Pot.YELLOW, Pot.BLUE),
        PURPLE("ColorBlendPurple", Pot.RED, Pot.BLUE),
        PINK("ColorBlendPink", Pot.RED, Pot.WHITE),
        SKY_BLUE("ColorBlendSkyBlue", Pot.BLUE, Pot.WHITE),
        PALE_YELLOW("ColorBlendPaleYellow", Pot.YELLOW, Pot.WHITE),
        GREY("ColorBlendGrey", Pot.WHITE, Pot.BLACK),
        BURGUNDY("ColorBlendBurgundy", Pot.RED, Pot.BLACK),
        NAVY("ColorBlendNavy", Pot.BLUE, Pot.BLACK),
        BROWN("ColorBlendBrown", Pot.RED, Pot.YELLOW, Pot.BLUE),
        LIGHT_GREEN("ColorBlendLightGreen", Pot.YELLOW, Pot.BLUE, Pot.WHITE),
        MAUVE("ColorBlendMauve", Pot.RED, Pot.BLUE, Pot.WHITE),
        PEACH("ColorBlendPeach", Pot.RED, Pot.YELLOW, Pot.WHITE),
        FIR_GREEN("ColorBlendFirGreen", Pot.YELLOW, Pot.BLUE, Pot.BLACK),
        AUBERGINE("ColorBlendAubergine", Pot.RED, Pot.BLUE, Pot.BLACK);

        /**
         * Key of translation of its name.
         */
        private final String key;
        private final List<Pot> recipe;

        Target(final String key, final Pot... recipe) {
            this.key = key;
            this.recipe = List.of(recipe);
        }

        Color color() {
            return mix(recipe);
        }
    }

    /**
     * Corners of the RYB cube, as {r, y, b} → {red, green, blue}.
     */
    private static final double[][][] CORNERS = {
        {{0, 0, 0}, {1.00, 1.00, 1.00}}, {{1, 0, 0}, {0.89, 0.16, 0.16}}, {{0, 1, 0}, {0.98, 0.84, 0.15}},
        {{0, 0, 1}, {0.16, 0.40, 0.78}}, {{1, 1, 0}, {0.96, 0.55, 0.12}}, {{1, 0, 1}, {0.50, 0.20, 0.62}},
        {{0, 1, 1}, {0.18, 0.62, 0.27}}, {{1, 1, 1}, {0.42, 0.25, 0.12}},
    };

    private PaintMix() {
    }

    /**
     * @return the color of the paints poured in a pot, in any order; white for an empty pot
     */
    static Color mix(final List<Pot> pots) {
        final int[] count = counts(pots);
        final double total = pots.size();
        if (total == 0) {
            return Color.WHITE;
        }
        final double hue = Math.max(count[0], Math.max(count[1], count[2]));
        final double[] base;
        final double white;
        if (hue > 0) {
            base = ryb(count[0] / hue, count[1] / hue, count[2] / hue);
            white = count[3] / total;
        } else {
            // White and black only: a grey
            base = new double[]{1, 1, 1};
            white = 0;
        }
        final double black = count[4] / total;
        final double[] rgb = new double[3];
        for (int i = 0; i < 3; i++) {
            rgb[i] = (base[i] + (1 - base[i]) * white) * (1 - black * 0.85);
        }
        return Color.color(clamp(rgb[0]), clamp(rgb[1]), clamp(rgb[2]));
    }

    /**
     * @return the color of a single pot
     */
    static Color color(final Pot pot) {
        return mix(List.of(pot));
    }

    /**
     * @return the key of the name of a mix: a pot alone or a recipe (each pot once), else null
     */
    static String name(final List<Pot> pots) {
        if (pots.size() == 1) {
            return pots.get(0).getKey();
        }
        for (final Target target : Target.values()) {
            if (sameRecipe(pots, target.recipe)) {
                return target.key;
            }
        }
        return null;
    }

    /**
     * @return whether two lists hold the same pots, whatever their order
     */
    static boolean sameRecipe(final List<Pot> pots, final List<Pot> recipe) {
        return Arrays.equals(counts(pots), counts(recipe));
    }

    private static int[] counts(final List<Pot> pots) {
        final int[] count = new int[Pot.values().length];
        for (final Pot pot : pots) {
            count[pot.ordinal()]++;
        }
        return count;
    }

    private static double[] ryb(final double r, final double y, final double b) {
        final double[] out = new double[3];
        for (final double[][] corner : CORNERS) {
            final double w = (corner[0][0] > 0 ? r : 1 - r) * (corner[0][1] > 0 ? y : 1 - y)
                * (corner[0][2] > 0 ? b : 1 - b);
            for (int i = 0; i < 3; i++) {
                out[i] += w * corner[1][i];
            }
        }
        return out;
    }

    private static double clamp(final double value) {
        return Math.max(0, Math.min(1, value));
    }
}
