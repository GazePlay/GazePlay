package net.gazeplay.games.cassebrique;

import lombok.Getter;

/**
 * The drawings of bricks of "Casse-brique" (kept by the maintainer on 3/11/2026): a block of all the colors, an alien
 * (it was "Space Invader", the name and the figure of an existing game: renamed and drawn otherwise), a rainbow and a
 * smile. Each row is a line of bricks: a digit is the color of a brick (1 to 7, see {@link CasseBrique#COLORS}), a
 * dot an empty cell. Since 20/11/2026, each drawing is drawn again for each level, on its grid of bricks (see
 * {@link CasseBriqueLevel}): the fewer the bricks, the bigger.
 */
@Getter
public enum CasseBriqueMap {
    BLOC("CasseBriqueBloc", null, null, null, null),
    ALIEN("CasseBriqueAlien",
        new String[]{
            "335533"},
        new String[]{
            "...55...",
            ".333333.",
            ".3.33.3.",
            "..3333.."},
        new String[]{
            "......5......",
            "...3333333...",
            "..33.333.33..",
            "..333333333..",
            "..33.....33..",
            "...3333333..."},
        new String[]{
            ".......5.......",
            "......333......",
            "...333333333...",
            "..33..333..33..",
            "..33..333..33..",
            "..33333333333..",
            "...33.....33...",
            "....3333333...."}),
    ARC("CasseBriqueArc",
        new String[]{
            "765432"},
        new String[]{
            "..7777..",
            ".766667.",
            "76555567",
            "65....56"},
        new String[]{
            ".....777.....",
            "...7766677...",
            "..765555567..",
            ".76544444567.",
            "7654333334567",
            "654.......456"},
        new String[]{
            "......777......",
            "....7766677....",
            "...765555567...",
            "..76544444567..",
            ".7654333334567.",
            "765432222234567",
            "65432.....23456",
            "5432.......2345"}),
    SMILEY("CasseBriqueSmiley",
        new String[]{
            "555555"},
        new String[]{
            ".55..55.",
            "........",
            "5......5",
            ".555555."},
        new String[]{
            "...55...55...",
            "...55...55...",
            ".............",
            ".5.........5.",
            "..55.....55..",
            "....55555...."},
        new String[]{
            "....55...55....",
            "....55...55....",
            "....55...55....",
            "...............",
            ".5...........5.",
            "..5.........5..",
            "...55.....55...",
            ".....55555....."});

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    private final String[][] drawings;

    CasseBriqueMap(final String key, final String[]... drawings) {
        this.key = key;
        this.drawings = drawings;
    }

    /**
     * @return the lines of bricks of the drawing for a level, as many lines and columns as the level has (the block: all
     *   its bricks, the colors in diagonal stripes)
     */
    public String[] rows(final CasseBriqueLevel level) {
        final String[] drawn = drawings[level.ordinal()];
        if (drawn != null) {
            return drawn;
        }
        final String[] rows = new String[level.getLines()];
        for (int line = 0; line < rows.length; line++) {
            final StringBuilder row = new StringBuilder();
            for (int column = 0; column < level.getColumns(); column++) {
                row.append((char) ('1' + (line + column) % CasseBrique.COLORS.length));
            }
            rows[line] = row.toString();
        }
        return rows;
    }

    /**
     * @return the drawing of a key, the block for an unknown one
     */
    public static CasseBriqueMap of(final String key) {
        for (final CasseBriqueMap map : values()) {
            if (map.key.equals(key)) {
                return map;
            }
        }
        return BLOC;
    }
}
