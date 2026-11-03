package net.gazeplay.games.cassebrique;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * What is behind the bricks of "Casse-brique" (maintainer's choice of 3/11/2026): a soft background drawn by the game,
 * or an image of the user, found little by little as the bricks break (the images of the user are a variant of their
 * own, folder images/CasseBrique; the game used to show an image of the old shared folder "opinions").
 */
@Getter
@RequiredArgsConstructor
public enum CasseBriqueBackground {
    DRAWN("CasseBriqueDrawn"),
    MY_IMAGES("CasseBriqueMyImages");

    /**
     * The folder of the images of the user of the game.
     */
    public static final String USER_IMAGES_FOLDER = net.gazeplay.commons.gamevariants.UserImagesVariant.IMAGES;

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the background of a key, drawn for an unknown one
     */
    public static CasseBriqueBackground of(final String key) {
        for (final CasseBriqueBackground background : values()) {
            if (background.key.equals(key)) {
                return background;
            }
        }
        return DRAWN;
    }
}
