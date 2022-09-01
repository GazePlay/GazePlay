package net.gazeplay.games.whereisit;

import lombok.Getter;
import net.gazeplay.commons.gamevariants.difficulty.Difficulty;

public enum WhereIsItGameType {
    FIND_ODD("findTheOddOneOut", "findTheOddOneOut", "NoVariant"),

    CUSTOMIZED("whereIsIt", "customized", "NoVariant"),

    ANIMALS_FARM("whereIsTheAnimal", "whereIsTheAnimal", "Farm"),
    ANIMALS_FOREST("whereIsTheAnimal", "whereIsTheAnimal", "Forest"),
    ANIMALS_SAVANNA("whereIsTheAnimal", "whereIsTheAnimal", "Savanna"),
    ANIMALS_BIRDS("whereIsTheAnimal", "whereIsTheAnimal", "Birds"),
    ANIMALS_MARITIME("whereIsTheAnimal", "whereIsTheAnimal", "Maritime"),
    ANIMALS_ALL("whereIsTheAnimal", "whereIsTheAnimal", "AllAnimals"),
    ANIMALS_DYNAMIC("whereIsTheAnimal", "whereIsTheAnimalDynamic", "Dynamic"),

    COLORS_EASY("whereIsTheColor", "whereIsTheColor", Difficulty.EASY.toString()),
    COLORS_NORMAL("whereIsTheColor", "whereIsTheColor", Difficulty.NORMAL.toString()),
    COLORS_HARD("whereIsTheColor", "whereIsTheColor", Difficulty.HARD.toString()),

    FLAGS_MOST_FAMOUS("whereIsTheFlag", "whereIsTheFlag", "MostFamous"),
    FLAGS_AFRICA("whereIsTheFlag", "whereIsTheFlag", "Africa"),
    FLAGS_AMERICA("whereIsTheFlag", "whereIsTheFlag", "America"),
    FLAGS_ASIA("whereIsTheFlag", "whereIsTheFlag", "Asia"),
    FLAGS_EUROPE("whereIsTheFlag", "whereIsTheFlag", "Europe"),
    FLAGS_ALL("whereIsTheFlag", "whereIsTheFlag", "AllFlags"),

    LETTERS_VOWELS("whereIsTheLetter", "whereIsTheLetter", "Vowels"),
    LETTERS_CONSONANTS("whereIsTheLetter", "whereIsTheLetter", "Consonants"),
    LETTERS_ALL("whereIsTheLetter", "whereIsTheLetter", "AllLetters"),

    NUMBERS("whereIsTheNumber", "whereIsTheNumber", "NoVariant"),

    SHAPES_EASY("whereIsTheShape", "whereIsTheShape", Difficulty.EASY.toString()),
    SHAPES_NORMAL("whereIsTheShape", "whereIsTheShape", Difficulty.NORMAL.toString()),
    SHAPES_HARD("whereIsTheShape", "whereIsTheShape", Difficulty.HARD.toString()),

    SOUNDS_ANIMALS("whereIsTheSound","whereIsTheSound", "Animals"),
    SOUNDS_INSTRUMENTS("whereIsTheSound","whereIsTheSound", "Instruments"),
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
