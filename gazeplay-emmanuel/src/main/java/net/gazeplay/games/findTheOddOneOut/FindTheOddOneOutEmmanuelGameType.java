package net.gazeplay.games.findTheOddOneOut;

import lombok.Getter;

public enum FindTheOddOneOutEmmanuelGameType {

    FIND_ODD("findTheOddOneOut", "findTheOddOneOut", "NoVariant");

    @Getter
    private final String gameName;

    @Getter
    private final String resourcesDirectoryName;

    @Getter
    private final String languageResourceLocation;

    @Getter
    private final String variant;

    FindTheOddOneOutEmmanuelGameType(String gameName, String resourcesDirectoryName, String variant) {
        this.gameName = gameName;
        this.resourcesDirectoryName = resourcesDirectoryName;
        this.languageResourceLocation = "data/" + resourcesDirectoryName + "/" + resourcesDirectoryName + ".csv";
        this.variant = variant;
    }
}
