package net.gazeplay.games.whereisit;

import lombok.Getter;
import net.gazeplay.commons.gamevariants.difficulty.Difficulty;

public enum WhereIsItGameType {
    ANIMAL_NAME("where-is-the-animal", "where-is-the-animal"),
    ANIMAL_NAME_DYNAMIC("where-is-the-animal", "where-is-the-animal-dynamic"),
    COLOR_NAME("where-is-the-color", "where-is-the-color"),
    COLOR_NAME_EASY("where-is-the-color", "where-is-the-color", Difficulty.EASY),
    LETTERS("where-is-the-letter", "where-is-the-letter"),
    NUMBERS("where-is-the-number", "where-is-the-number"),
    FLAGS("find-flag", "find-flag"),
    CUSTOMIZED("customized", "customized"),
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
