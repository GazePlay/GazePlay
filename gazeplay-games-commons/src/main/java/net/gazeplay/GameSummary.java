package net.gazeplay;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.SortedSet;

@Builder
@Slf4j
@EqualsAndHashCode
public class GameSummary {

    @Getter
    private final String nameCode;

    @Getter
    @NonNull
    private final String gameThumbnail;

    @Getter
    @Singular("category")
    private final SortedSet<GameCategories.Category> categories;

    @Getter
    private final String backgroundMusicUrl;

    @Getter
    private final String description;

    /**
     * For games in the same categories, it can happen that the nameCode is not appropriate to order the games as expected.
     * <p>
     * We may like that some games are sorted in a specific order, but their nameCode natural order does not match the order we like.
     * But we don't want to mess with the nameCode just to have a different ordering.
     * <p>
     * Introducing priority :
     * Priority is used in the comparator chain : after the categories, but before the nameCode .
     * So the priority will be used to sort games in the same categories.
     * When priorities for multiple games is equal, then the nameCode is used to order them.
     * <p>
     * Higher priority means games are sorted first (so comparator will compare in reversed order)
     */
    @Getter
    private final int priority;

    /**
     * There are some games that we want to ALWAYS be placed before the rest of the games.
     * <p>
     * This is a different property to priority, that determines the order when games are in
     * the same category.
     * <p>
     * Higher absolute priority means games are sorted first (so comparator will compare in reversed order).
     * You must give an absolute priority of > 1 for it to be moved.
     */
    @Getter
    private final int absolutePriority;
}
