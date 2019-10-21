package net.gazeplay;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;

@Builder
@Slf4j
public class GameSummary implements Comparable<GameSummary> {

    @Getter
    private final String nameCode;

    @Getter
    private final String gameThumbnail;

    @Getter
    private final GameCategories.Category category;

    @Getter
    private final String backgroundMusicUrl;

    @Getter
    private final String description;

    @Deprecated // use builder instead of constructor
    public GameSummary(String nameCode, String gameThumbnail, GameCategories.Category category) {
        this(nameCode, gameThumbnail, category, null);
    }

    @Deprecated // use builder instead of constructor
    public GameSummary(String nameCode, String gameThumbnail, GameCategories.Category category,
                       final String backgroundMusicUrl) {
        this(nameCode, gameThumbnail, category, backgroundMusicUrl, null);
    }

    @Deprecated // use builder instead of constructor
    public GameSummary(String nameCode, String gameThumbnail, GameCategories.Category category,
                       String backgroundMusicUrl, final String description) {
        this.nameCode = nameCode;
        this.gameThumbnail = gameThumbnail;
        this.category = category;
        this.backgroundMusicUrl = backgroundMusicUrl;
        this.description = description;
    }

    @Override
    public int compareTo(GameSummary o) {
        return Comparator
            .comparing(GameSummary::getCategory)
            .thenComparing(GameSummary::getNameCode)
            .compare(this, o);
    }

}
