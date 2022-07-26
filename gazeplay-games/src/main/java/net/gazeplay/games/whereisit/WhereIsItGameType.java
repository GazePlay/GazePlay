package net.gazeplay.games.whereisit;

import lombok.Getter;
import net.gazeplay.commons.gamevariants.difficulty.Difficulty;

public enum WhereIsItGameType {
    FIND_ODD("findTheOddOneOut", "findTheOddOneOut", "NoVariant"),

    CUSTOMIZED("whereIsIt", "customized", "NoVariant"),

    ANIMALS_ALL("whereIsTheAnimal", "whereIsTheAnimal", "AllAnimals"),
    ANIMALS_DYNAMIC("whereIsTheAnimal", "whereIsTheAnimalDynamic", "Dynamic"),

    COLORS_EASY("whereIsTheColor", "whereIsTheColor", Difficulty.EASY.toString()),
    COLORS_NORMAL("whereIsTheColor", "whereIsTheColor", Difficulty.NORMAL.toString()),
    COLORS_HARD("whereIsTheColor", "whereIsTheColor", Difficulty.HARD.toString()),

    FLAGS_ALL("whereIsTheFlag", "whereIsTheFlag", "AllFlags"),

    LETTERS_VOWELS("whereIsTheLetter", "whereIsTheLetter", "Vowels"),
    LETTERS_CONSONANTS("whereIsTheLetter", "whereIsTheLetter", "Consonants"),
    LETTERS_ALL("whereIsTheLetter", "whereIsTheLetter", "AllLetters"),

    NUMBERS("whereIsTheNumber", "whereIsTheNumber", "NoVariant"),

    SHAPES_EASY("whereIsTheShape", "whereIsTheShape", Difficulty.EASY.toString()),
    SHAPES_NORMAL("whereIsTheShape", "whereIsTheShape", Difficulty.NORMAL.toString()),
    SHAPES_HARD("whereIsTheShape", "whereIsTheShape", Difficulty.HARD.toString()),

    SOUNDS_ANIMALS("whereIsTheSoundAnimals","whereIsTheSound", "Animals"),
    SOUNDS_ALL("whereIsTheSound","whereIsTheSound", "AllSounds");

    @Getter
    private final String gameName;

    @Getter
    private final String resourcesDirectoryName;

    @Getter
    private final String languageResourceLocation;

    @Getter
    private final String variant;

    WhereIsItGameType(String gameName, String resourcesDirectoryName, String variant) {
        this.gameName = gameName;
        this.resourcesDirectoryName = resourcesDirectoryName;
        this.languageResourceLocation = "data/" + resourcesDirectoryName + "/" + resourcesDirectoryName + ".csv";
        this.variant = variant;
    }
}
