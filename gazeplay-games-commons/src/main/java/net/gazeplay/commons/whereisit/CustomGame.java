package net.gazeplay.commons.whereisit;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * A game made by the user for "Where is it?": its name and its screens (see {@link CustomGames} for its folder).
 */
@Getter
public final class CustomGame {

    /**
     * The name of its folder, which does not change when the game is renamed (the shortcuts and the results name it);
     * null for a game not yet saved.
     */
    private String folder;

    private String name = "";

    private final List<CustomScreen> screens = new ArrayList<>();

    public CustomGame() {
    }

    CustomGame(final String folder) {
        this.folder = folder;
    }

    void setFolder(final String folder) {
        this.folder = folder;
    }

    public void setName(final String name) {
        this.name = name == null ? "" : name.strip();
    }

    /**
     * @return the screens that can be played: the incomplete ones are left out
     */
    public List<CustomScreen> playableScreens() {
        return screens.stream().filter(CustomScreen::isComplete).toList();
    }
}
