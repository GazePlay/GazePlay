package net.gazeplay.ui.scenes.gamemenu;

import javafx.stage.Stage;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.ui.menus.HelpDialog;

import java.util.List;

/**
 * The help of a game (maintainer's request of 30/10/2026; written for the games already reviewed, then for each game
 * reviewed): its principle, how to play, how to win, its variants. In {@code data/help/games/<name code>-<language>.txt}
 * (the name code without its spaces), read by a {@link HelpDialog}. It is opened by the "i" button of the window of the
 * variants; a game without variants shows it in a window with a button starting the game.
 */
public final class GameHelp {

    static final String FOLDER = "data/help/games/";

    private GameHelp() {
    }

    /**
     * @return the lines of the help of a game in the language of the translator, null when the game has none
     */
    static List<String> lines(final String nameCode, final Translator translator) {
        return HelpDialog.read(FOLDER + nameCode.replace(" ", "") + "-",
            HelpDialog.language(translator.currentLocale()));
    }

    /**
     * @return whether the game has a help
     */
    static boolean exists(final String nameCode) {
        return HelpDialog.read(FOLDER + nameCode.replace(" ", "") + "-", HelpDialog.FALLBACK_LANGUAGE) != null;
    }

    /**
     * @param launch what starts the game, null for the help only
     * @return the window of the help of a game, over the window or the page given (not shown yet)
     */
    public static HelpDialog dialog(final Stage owner, final Translator translator, final String nameCode,
                                    final String title, final Runnable launch) {
        final HelpDialog dialog = new HelpDialog(owner, translator, title, lines(nameCode, translator),
            translator.translate("GameLaunch"), launch);
        dialog.setAlwaysOnTop(true);
        return dialog;
    }
}
