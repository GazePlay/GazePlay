package net.gazeplay.games.whereisitconfigurable;

import lombok.Getter;
import net.gazeplay.commons.gamevariants.difficulty.Difficulty;

public enum WhereIsItConfigurableGameType {
    CUSTOMIZED("customized", "customized");

    @Getter
    private final String gameName;

    @Getter
    private final String resourcesDirectoryName;

    @Getter
    private final String languageResourceLocation;

    @Getter
    private final Difficulty difficulty;

    WhereIsItConfigurableGameType(String gameName, String resourcesDirectoryName) {
        this(gameName, resourcesDirectoryName, Difficulty.NORMAL);
    }

    WhereIsItConfigurableGameType(String gameName, String resourcesDirectoryName, Difficulty difficulty) {
        this.gameName = gameName;
        this.resourcesDirectoryName = resourcesDirectoryName;
        this.languageResourceLocation = "data/" + resourcesDirectoryName + "/" + resourcesDirectoryName + ".csv";
        this.difficulty = difficulty;
    }
}
