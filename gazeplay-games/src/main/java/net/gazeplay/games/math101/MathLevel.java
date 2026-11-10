package net.gazeplay.games.math101;

import lombok.Getter;

/**
 * The levels of "Apprends les Maths" (maintainer's choices of 10/11/2026): the additions and subtractions up to 10,
 * 20 or 100; the multiplications and divisions by table (one table, the tables of 2 to 5, all the tables of 2 to 10);
 * all the operations easy (up to 10, tables of 2 to 5), medium (up to 20, all the tables) or hard (up to 100, all the
 * tables). Never a 0: neither in an operation nor in an answer.
 */
@Getter
public enum MathLevel {
    UP_TO_10("MathUpTo10", 10),
    UP_TO_20("MathUpTo20", 20),
    UP_TO_100("MathUpTo100", 100),
    TABLE_2("MathTable2", 0, 2),
    TABLE_3("MathTable3", 0, 3),
    TABLE_4("MathTable4", 0, 4),
    TABLE_5("MathTable5", 0, 5),
    TABLE_6("MathTable6", 0, 6),
    TABLE_7("MathTable7", 0, 7),
    TABLE_8("MathTable8", 0, 8),
    TABLE_9("MathTable9", 0, 9),
    TABLE_10("MathTable10", 0, 10),
    TABLES_2_TO_5("MathTables2To5", 0, 2, 3, 4, 5),
    TABLES_ALL("MathTablesAll", 0, 2, 3, 4, 5, 6, 7, 8, 9, 10),
    ALL_EASY("MathAllEasy", 10, 2, 3, 4, 5),
    ALL_MEDIUM("MathAllMedium", 20, 2, 3, 4, 5, 6, 7, 8, 9, 10),
    ALL_HARD("MathAllHard", 100, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    /**
     * The largest factor of a table: 2 × 1 to 2 × 10.
     */
    static final int FACTORS = 10;

    /**
     * Key of translation, and value of the variants (the buttons).
     */
    private final String key;

    /**
     * The largest number of the additions and subtractions (the sum, or the number subtracted from), 0 if none.
     */
    private final int upTo;

    /**
     * The tables of the multiplications and divisions, none if no multiplication.
     */
    private final int[] tables;

    MathLevel(final String key, final int upTo, final int... tables) {
        this.key = key;
        this.upTo = upTo;
        this.tables = tables;
    }

    /**
     * @return the level of a key, null for an unknown one
     */
    public static MathLevel of(final String key) {
        for (final MathLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return null;
    }
}
