package net.gazeplay.games.cassebrique;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The drawings of bricks of "Casse-brique" (kept by the maintainer on 3/11/2026): a block of all the colors, an alien
 * (it was "Space Invader", the name and the figure of an existing game: renamed and drawn otherwise), a rainbow and a
 * smile. Each row is a line of bricks: a digit is the color of a brick (1 to 7, see {@link CasseBrique#COLORS}), a
 * dot an empty cell.
 */
@Getter
@RequiredArgsConstructor
public enum CasseBriqueMap {
    BLOC("CasseBriqueBloc", new String[]{
        "1234567123456712345",
        "2345671234567123456",
        "3456712345671234567",
        "4567123456712345671",
        "5671234567123456712",
        "6712345671234567123",
        "7123456712345671234",
        "1234567123456712345"}),
    ALIEN("CasseBriqueAlien", new String[]{
        ".........5.........",
        "........333........",
        ".....333333333.....",
        "....33333333333....",
        "...333..333..333...",
        "...333..333..333...",
        "...3333333333333...",
        "....33.......33....",
        ".....333333333.....",
        "..................."}),
    ARC("CasseBriqueArc", new String[]{
        "...................",
        ".........7.........",
        "........767........",
        ".......76567.......",
        "......7654567......",
        ".....765434567.....",
        "....76543234567....",
        "...7654321234567...",
        "..765432...234567..",
        ".765432.....234567."}),
    SMILEY("CasseBriqueSmiley", new String[]{
        ".................",
        ".................",
        "......5...5......",
        "......5...5......",
        "......5...5......",
        ".................",
        ".....5.....5.....",
        "......55555......",
        "................."});

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The lines of bricks.
     */
    private final String[] rows;

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
