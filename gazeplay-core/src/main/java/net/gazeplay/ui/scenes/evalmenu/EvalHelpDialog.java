package net.gazeplay.ui.scenes.evalmenu;

import javafx.scene.Parent;
import javafx.stage.Stage;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.ui.menus.HelpDialog;

import java.util.List;

/**
 * The window of the help card of the menu of the evaluations (maintainer's request of 29/10/2026): how an evaluation
 * is prepared and taken, the keys of the examiner, the ways of taking it, the settings and what is recorded (a
 * {@link HelpDialog}, text in {@code data/help/eval-help-<language>.txt}).
 */
public class EvalHelpDialog extends HelpDialog {

    static final String FALLBACK_LANGUAGE = HelpDialog.FALLBACK_LANGUAGE;

    private static final String TEXT = "data/help/eval-help-";

    public EvalHelpDialog(final Stage owner, final Translator translator, final Parent menuRoot) {
        super(owner, translator, translator.translate("EvalHelp"), read(language(translator.currentLocale())),
            null, null);
        // The menu comes back when the window closes, whatever closes it
        setOnHidden(e -> {
            menuRoot.setEffect(null);
            menuRoot.setDisable(false);
        });
    }

    /**
     * @return the lines of the help in a language, or in English when there is none in that language
     */
    static List<String> read(final String language) {
        return HelpDialog.read(TEXT, language);
    }
}
