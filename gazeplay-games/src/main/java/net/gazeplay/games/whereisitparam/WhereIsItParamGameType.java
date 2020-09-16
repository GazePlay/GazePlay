package net.gazeplay.games.whereisitparam;

import lombok.Getter;
import net.gazeplay.commons.gamevariants.difficulty.Difficulty;

public enum WhereIsItParamGameType {
    CUSTOMIZED("customized", "customized");

    @Getter
    private final String gameName;

    @Getter
    private final String resourcesDirectoryName;

    @Getter
    private final String languageResourceLocation;

    @Getter
    private final Difficulty difficulty;

    WhereIsItParamGameType(String gameName, String resourcesDirectoryName) {
        this(gameName, resourcesDirectoryName, Difficulty.NORMAL);
    }

    WhereIsItParamGameType(String gameName, String resourcesDirectoryName, Difficulty difficulty) {
        this.gameName = gameName;
        this.resourcesDirectoryName = resourcesDirectoryName;
        this.languageResourceLocation = "data/" + resourcesDirectoryName + "/" + resourcesDirectoryName + ".csv";
        this.difficulty = difficulty;
    }
}
