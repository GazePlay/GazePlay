package net.gazeplay.commons.gamevariants;

import net.gazeplay.commons.ui.Translator;

/**
 * This is a basic marker interface, but it comes also with a label in order to recognise it by a text label
 */
public interface IGameVariant {
    String getLabel(Translator translator);
    String toString();

    static IGameVariant toGameVariant(String gameVariantToText) {
        String[] split = gameVariantToText.split(":");
        switch (split[0]) {
            case "StringGameVariant":
                return new StringGameVariant(split[1], split[2]);
            case "IntGameVariant":
                return new IntGameVariant(Integer.parseInt(split[1]));
            case "IntStringGameVariant":
                return new IntStringGameVariant(Integer.parseInt(split[1]), split[2]);
            case "DimensionGameVariant":
                return new DimensionGameVariant(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            case "MathGameVariant":
                return new MathGameVariant(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            case "DimensionDifficultyGameVariant":
                return new DimensionDifficultyGameVariant(Integer.parseInt(split[1]), Integer.parseInt(split[2]), split[3]);
            case "EnumGameVariant":
                try {
                    Class enumClass = Class.forName(split[1]);
                    return enumClass.isEnum() ? new EnumGameVariant<>(Enum.valueOf(enumClass, split[2])) : null;
                } catch (ClassNotFoundException ignored) {
                    return null;
                }
            default:
                return null;
        }
    }
}
