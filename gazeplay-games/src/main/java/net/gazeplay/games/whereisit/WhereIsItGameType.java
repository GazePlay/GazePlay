package net.gazeplay.games.whereisit;

import lombok.Getter;
import net.gazeplay.commons.gamevariants.difficulty.Difficulty;

public enum WhereIsItGameType {
    ANIMALS("where-is-the-animal", "where-is-the-animal"),
    ANIMALS_DYNAMIC("where-is-the-animal", "where-is-the-animal-dynamic"),
    COLORS("where-is-the-color", "where-is-the-color"),
    COLORS_EASY("where-is-the-color", "where-is-the-color", Difficulty.EASY),
    LETTERS("where-is-the-letter", "where-is-the-letter"),
    NUMBERS("where-is-the-number", "where-is-the-number"),
    SHAPES("where-is-the-shape", "where-is-the-shape"),
    SHAPES_EASY("where-is-the-shape", "where-is-the-shape", Difficulty.EASY),
    SOUNDS("where-is-the-sound","where-is-the-sound"),
    SOUNDS_ANIMAL("where-is-the-sound-animals","where-is-the-sound-animals"),
    CUSTOMIZED("customized", "customized"),
    FLAGS("find-flag", "find-flag"),
    FIND_ODD("find-the-odd-one-out", "find-the-odd-one-out");

    @Getter
    private final String gameName;

    @Getter
    private final String resourcesDirectoryName;

    @Getter
    private final String languageResourceLocation;

    @Getter
    private final Difficulty difficulty;

    WhereIsItGameType(String gameName, String resourcesDirectoryName) {
        this(gameName, resourcesDirectoryName, Difficulty.NORMAL);
    }

    WhereIsItGameType(String gameName, String resourcesDirectoryName, Difficulty difficulty) {
        this.gameName = gameName;
        this.resourcesDirectoryName = resourcesDirectoryName;
        this.languageResourceLocation = "data/" + resourcesDirectoryName + "/" + resourcesDirectoryName + ".csv";
        this.difficulty = difficulty;
    }
}
