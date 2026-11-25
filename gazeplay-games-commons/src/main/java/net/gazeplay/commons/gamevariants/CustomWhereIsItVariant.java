package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.whereisit.CustomGame;
import net.gazeplay.commons.whereisit.CustomGames;

/**
 * Variant of "Where is it?" (maintainer's request of 25/11/2026, point 86): a game made by the user, named by its folder
 * (see {@link CustomGames}), and the hearts (see {@link LivesVariant}); each of its screens is played once. Persistent
 * form "CustomWhereIsItVariant:folder:hearts" (the folders are plain letters and digits).
 */
@Data
public class CustomWhereIsItVariant implements IGameVariant {

    static final String TYPE = "CustomWhereIsItVariant";

    private final String folder;

    /**
     * {@link LivesVariant#WITH} or {@link LivesVariant#WITHOUT}.
     */
    private final String lives;

    public boolean hasLives() {
        return LivesVariant.WITH.equals(lives);
    }

    /**
     * @return the name of the game (its folder when it no longer exists), then its hearts
     */
    @Override
    public String getLabel(final Translator translator) {
        final CustomGame game = CustomGames.load(folder);
        return (game == null ? folder : game.getName()) + ", " + translator.translate(lives);
    }

    @Override
    public String toString() {
        return TYPE + ":" + folder + ":" + lives;
    }

    /**
     * @return the variant of its persistent form, null if it is not one
     */
    static CustomWhereIsItVariant read(final String[] parts) {
        return parts.length < 3 ? null : new CustomWhereIsItVariant(parts[1], parts[2]);
    }
}
