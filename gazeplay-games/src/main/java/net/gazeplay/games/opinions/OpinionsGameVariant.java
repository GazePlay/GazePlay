package net.gazeplay.games.opinions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

/**
 * Variants of the game "Opinions" (reviewed on 2/11/2026): the game plays only the images given by the user (folder
 * images/Opinions), so its only variant is {@link #MY_IMAGES}. The former values (an opinion in 3 answers, yes / no in 4
 * layouts) are no longer offered; they are kept because they are written in the games saved before, which are replayed
 * with the images of the user.
 */
@RequiredArgsConstructor
public enum OpinionsGameVariant implements UserImagesVariant {
    MY_IMAGES("OpinionsMyImages"),
    OPINIONS("Opinions"),
    ONHB("OuiNon Haut-Bas"),
    ONBH("OuiNon Bas-Haut"),
    ONGD("OuiNon Gauche-Droite"),
    ONDG("OuiNon Droite-Gauche");

    /**
     * Folder of the images given by the user, in the images folder.
     */
    static final String USER_IMAGES_FOLDER = "Opinions";

    @Getter
    private final String label;

    @Override
    public String getUserImagesFolder() {
        return USER_IMAGES_FOLDER;
    }
}
