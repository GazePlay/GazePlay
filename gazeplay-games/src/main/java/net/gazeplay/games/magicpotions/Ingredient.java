package net.gazeplay.games.magicpotions;

/**
 * The ingredients of "Recettes magiques" (24/11/2026): emoji of OpenMoji, CC BY-SA 4.0
 * (gradle/images/make_magicrecipes_images.py), in data/potions/images/ingredients. Their names are not shown: the
 * recipe is in pictures.
 */
public enum Ingredient {
    MUSHROOM, FEATHER, STAR, FLOWER, LEAF, APPLE, CRYSTAL, MOON, SHELL, CARROT, SNOWFLAKE, CLOVER;

    /**
     * @return the path of the picture
     */
    public String picture() {
        return "data/potions/images/ingredients/" + name().toLowerCase(java.util.Locale.ROOT) + ".png";
    }
}
