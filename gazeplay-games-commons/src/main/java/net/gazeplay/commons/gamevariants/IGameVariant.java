package net.gazeplay.commons.gamevariants;

import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;

import java.io.File;
import java.util.Objects;

/**
 * A variant of a game (a size, a level, a theme...), chosen in the menu. Its {@link #toString()} is its persistent
 * form, "Type:value:value...", saved in replayData.json and given to --variant; {@link #toGameVariant(String)} reads it
 * back.
 */
public interface IGameVariant {

    /**
     * @return the name of the variant shown in the menu
     */
    String getLabel(Translator translator);

    /**
     * @return the persistent form of the variant, read back by {@link #toGameVariant(String)}
     */
    String toString();

    /**
     * Reads a variant from its persistent form. An unknown type is the name of an evaluation (GazePlay Eval) when a
     * folder of that name exists in evals of the files folder.
     *
     * @return the variant, null when it cannot be read
     */
    static IGameVariant toGameVariant(String gameVariantTotext) {
        String[] split = gameVariantTotext.split(":");
        switch (split[0]) {
            case "StringGameVariant":
                return new StringGameVariant(split[1], split[2]);
            case "IntGameVariant":
                return new IntGameVariant(Integer.parseInt(split[1]));
            case "IntStringGameVariant":
                if(split.length>=4){
                    return new IntStringGameVariant(Integer.parseInt(split[1]), split[2], Integer.parseInt(split[3]));
                }else{
                    return new IntStringGameVariant(Integer.parseInt(split[1]), split[2]);
                }
            case "DimensionGameVariant":
                return new DimensionGameVariant(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            case "MathGameVariant":
                return new MathGameVariant(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            case "DimensionDifficultyGameVariant":
                return new DimensionDifficultyGameVariant(Integer.parseInt(split[1]), Integer.parseInt(split[2]), split[3]);
            case "EnumGameVariant":
                try {
                    Class enumClass = Class.forName(split[1]);
                    if (enumClass.isEnum()) {
                        return new EnumGameVariant<>(Enum.valueOf(enumClass, split[2]));
                    }
                    return null;
                } catch (ClassNotFoundException ignored) {
                    return null;
                }
            default:
                Configuration config = ActiveConfigurationContext.getInstance();
                File directoryPath = new File(config.getFileDir() + "\\evals\\");
                String[] content = directoryPath.list();

                if (content != null){
                    for (String s : content) {
                        if (Objects.equals(s, split[0])) {
                            return new GazeplayEvalGameVariant(split[0]);
                        }
                    }
                }

                return null;
        }
    }
}
