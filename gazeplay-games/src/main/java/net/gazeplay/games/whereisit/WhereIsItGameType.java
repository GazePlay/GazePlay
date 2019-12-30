package net.gazeplay.games.whereisit;

import lombok.Getter;

public enum WhereIsItGameType {
    ANIMALNAME("where-is-the-animal", "where-is-the-animal"), COLORNAME("where-is-the-color",
        "where-is-the-color"), LETTERS("where-is-the-letter", "where-is-the-letter"), NUMBERS(
        "where-is-the-number", "where-is-the-number"), FLAGS("find-flag", "find-flag"), CUSTOMIZED(
        "customized", "customized"), FINDODD("find-the-odd-one-out", "find-the-odd-one-out");

    @Getter
    private final String gameName;

    @Getter
    private final String resourcesDirectoryName;

    @Getter
    private final String languageResourceLocation;

    WhereIsItGameType(String gameName, String resourcesDirectoryName) {
        this.gameName = gameName;
        this.resourcesDirectoryName = resourcesDirectoryName;
        this.languageResourceLocation = "data/" + resourcesDirectoryName + "/" + resourcesDirectoryName + ".csv";
    }
}
