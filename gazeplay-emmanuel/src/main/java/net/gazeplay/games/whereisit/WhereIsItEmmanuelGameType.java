package net.gazeplay.games.whereisit;

import lombok.Getter;
import net.gazeplay.commons.gamevariants.difficulty.Difficulty;

public enum WhereIsItEmmanuelGameType {

    FLAGS_EUROPE("whereIsTheFlag", "whereIsTheFlag", "Europe"),
    FLAGS_EUROPE_AMERICA("whereIsTheFlag", "whereIsTheFlag", "EuropeAmerica"),
    FLAGS_ALL("whereIsTheFlag", "whereIsTheFlag", "AllFlags");

    @Getter
    private final String gameName;

    @Getter
    private final String resourcesDirectoryName;

    @Getter
    private final String languageResourceLocation;

    @Getter
    private final String variant;

    WhereIsItEmmanuelGameType(String gameName, String resourcesDirectoryName, String variant) {
        this.gameName = gameName;
        this.resourcesDirectoryName = resourcesDirectoryName;
        this.languageResourceLocation = "data/" + resourcesDirectoryName + "/" + resourcesDirectoryName + ".csv";
        this.variant = variant;
    }
}
