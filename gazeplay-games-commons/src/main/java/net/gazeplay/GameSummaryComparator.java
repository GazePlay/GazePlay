package net.gazeplay;

import net.gazeplay.commons.ui.Translator;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Order of the games in the menu: the alphabetical order of their names shown in the language chosen (maintainer's
 * request of 20/11/2026, done on 24/11/2026), with the rules of that language (an accent or a ligature does not move a
 * name: « Échelle » with the E, « Œuf » as « Oeuf »; no difference between capitals and small letters). It used to be
 * an absolute priority, then the categories, then a priority, then the name; the categories are filters of the menu,
 * not sections, so they no longer order it.
 */
public class GameSummaryComparator implements Comparator<GameSummary> {

    private final Translator translator;

    private final Collator collator;

    public GameSummaryComparator(final Translator translator) {
        this.translator = translator;
        final Locale locale = translator.currentLocale();
        this.collator = Collator.getInstance(locale == null ? Locale.getDefault() : locale);
        // Accents count only between names otherwise equal; capitals never
        this.collator.setStrength(Collator.SECONDARY);
    }

    @Override
    public int compare(final GameSummary o1, final GameSummary o2) {
        final int byName = collator.compare(name(o1), name(o2));
        // Two equal names (never in the catalog): an order that does not change from one start to another
        return byName != 0 ? byName : String.valueOf(o1.getNameCode()).compareTo(String.valueOf(o2.getNameCode()));
    }

    private String name(final GameSummary summary) {
        final String code = summary.getNameCode();
        if (code == null) {
            return "";
        }
        final String translated = translator.translate(code);
        return translated == null ? code : translated;
    }
}
