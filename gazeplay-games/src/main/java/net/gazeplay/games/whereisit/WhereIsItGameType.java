package net.gazeplay.games.whereisit;

import lombok.Getter;
import net.gazeplay.commons.gamevariants.difficulty.Difficulty;

public enum WhereIsItGameType {
    ANIMALS("whereIsTheAnimal", "whereIsTheAnimal"),
    ANIMALS_DYNAMIC("whereIsTheAnimal", "whereIsTheAnimalDynamic"),
    COLORS_EASY("whereIsTheColor", "whereIsTheColor", Difficulty.EASY.toString()),
    COLORS_NORMAL("whereIsTheColor", "whereIsTheColor", Difficulty.NORMAL.toString()),
    COLORS_HARD("whereIsTheColor", "whereIsTheColor", Difficulty.HARD.toString()),
    FLAGS("whereIsTheFlag", "whereIsTheFlag"),
    LETTERS("whereIsTheLetter", "whereIsTheLetter"),
    NUMBERS("whereIsTheNumber", "whereIsTheNumber"),
    SHAPES_EASY("whereIsTheShape", "whereIsTheShape", Difficulty.EASY.toString()),
    SHAPES_NORMAL("whereIsTheShape", "whereIsTheShape", Difficulty.NORMAL.toString()),
    SHAPES_HARD("whereIsTheShape", "whereIsTheShape", Difficulty.HARD.toString()),
    SOUNDS("whereIsTheSound","whereIsTheSound"),
    SOUNDS_ANIMAL("whereIsTheSoundAnimals","whereIsTheSoundAnimals"),
    CUSTOMIZED("whereIsIt", "customized"),
    FIND_ODD("findTheOddOneOut", "findTheOddOneOut");

    @Getter
    private final String gameName;

    @Getter
    private final String resourcesDirectoryName;

    @Getter
    private final String languageResourceLocation;

    @Getter
    private final String variant;

    WhereIsItGameType(String gameName, String resourcesDirectoryName) {
        this(gameName, resourcesDirectoryName, Difficulty.NORMAL.toString());
    }

    WhereIsItGameType(String gameName, String resourcesDirectoryName, String variant) {
        this.gameName = gameName;
        this.resourcesDirectoryName = resourcesDirectoryName;
        this.languageResourceLocation = "data/" + resourcesDirectoryName + "/" + resourcesDirectoryName + ".csv";
        this.variant = variant;
    }
}
