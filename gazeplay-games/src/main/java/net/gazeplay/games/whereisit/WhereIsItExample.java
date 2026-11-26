package net.gazeplay.games.whereisit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;

/**
 * How the question of "Where is the shape?", "the color?" and "the number?" shows an example (tester's feedback of
 * 23/11/2026, point 36; maintainer's choices of 26/11/2026: her three names, the games whose pictures are simple but
 * not the letter, which the question already shows): the picture of the right answer at the right of the sentence
 * while the question is asked, then with it in the band, kept while choosing (learning) or gone once the pictures are
 * shown (training), or no example (mastery, the game of before).
 * The easiest first, chosen when the window of the variants opens (maintainer's rule of 20/11/2026).
 */
@Getter
@RequiredArgsConstructor
public enum WhereIsItExample {
    LEARNING("WhereIsItLearning"),
    TRAINING("WhereIsItTraining"),
    MASTERY("WhereIsItMastery");

    /**
     * Key of translation, and value of the switch of the variants.
     */
    private final String key;

    /**
     * @return whether the example is shown with the question
     */
    public boolean shownWithQuestion() {
        return this != MASTERY;
    }

    /**
     * @return the way of a variant: its switch, or no example for a variant without it (a game of the family without
     *   this switch, a variant of before)
     */
    public static WhereIsItExample of(final IGameVariant variant) {
        if (variant instanceof GroupedGameVariant grouped) {
            for (final WhereIsItExample example : values()) {
                if (grouped.getExtras().contains(example.key)) {
                    return example;
                }
            }
        }
        return MASTERY;
    }
}
