package net.gazeplay.ui.scenes.evalmenu;

import javafx.scene.Parent;
import javafx.stage.Stage;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.ui.menus.HelpDialog;

import java.util.List;

/**
 * The window explaining the files written by a session of an evaluation, what they hold and what they are for
 * (maintainer's request of 5/11/2026; a {@link HelpDialog}, text in {@code data/help/eval-files-<language>.txt}). To be
 * kept in line with {@code EvalResultsWriter}.
 */
public class EvalFilesHelpDialog extends HelpDialog {

    private static final String TEXT = "data/help/eval-files-";

    public EvalFilesHelpDialog(final Stage owner, final Translator translator, final Parent menuRoot) {
        super(owner, translator, translator.translate("EvalFilesHelp"), read(language(translator.currentLocale())),
            null, null);
        // The menu comes back when the window closes, whatever closes it
        setOnHidden(e -> {
            menuRoot.setEffect(null);
            menuRoot.setDisable(false);
        });
    }

    /**
     * @return the lines of the text in a language, or in English when there is none in that language
     */
    static List<String> read(final String language) {
        return HelpDialog.read(TEXT, language);
    }
}
