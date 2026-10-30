package net.gazeplay.commons.gamevariants;

import net.gazeplay.commons.eval.EvalLibrary;
import net.gazeplay.commons.ui.Translator;

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
     * Reads a variant from its persistent form. An unknown type is the name of an imported evaluation (GazePlay Eval,
     * former persistent form) when a folder of that name exists in the evaluations folder.
     *
     * @return the variant, null when it cannot be read
     */
    static IGameVariant toGameVariant(String gameVariantTotext) {
        String[] split = gameVariantTotext.split(":");
        switch (split[0]) {
            case GazeplayEvalGameVariant.TYPE:
                return new GazeplayEvalGameVariant(gameVariantTotext.substring(GazeplayEvalGameVariant.TYPE.length() + 1));
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
            case GroupedGameVariant.TYPE:
                return GroupedGameVariant.read(split);
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
                if (EvalLibrary.names().contains(gameVariantTotext)) {
                    return new GazeplayEvalGameVariant(gameVariantTotext);
                }
                return null;
        }
    }
}
