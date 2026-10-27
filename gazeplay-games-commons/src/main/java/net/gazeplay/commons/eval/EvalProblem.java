package net.gazeplay.commons.eval;

import net.gazeplay.commons.ui.Translator;

import java.util.Arrays;
import java.util.List;

/**
 * A problem found in an evaluation, as a translation key and its arguments ({0}, {1}... in the translation).
 *
 * @param error true when the evaluation cannot be played, false for a warning
 */
public record EvalProblem(boolean error, String key, List<String> args) {

    public static EvalProblem error(String key, Object... args) {
        return new EvalProblem(true, key, toStrings(args));
    }

    public static EvalProblem warning(String key, Object... args) {
        return new EvalProblem(false, key, toStrings(args));
    }

    private static List<String> toStrings(Object[] args) {
        return Arrays.stream(args).map(String::valueOf).toList();
    }

    /**
     * @return the text of the problem in the language of the interface
     */
    public String format(Translator translator) {
        return format(translator.translate(key));
    }

    /**
     * @return the pattern with {0}, {1}... replaced by the arguments
     */
    public String format(String pattern) {
        String text = pattern;
        for (int i = 0; i < args.size(); i++) {
            text = text.replace("{" + i + "}", args.get(i));
        }
        return text;
    }

    @Override
    public String toString() {
        return (error ? "error " : "warning ") + key + " " + args;
    }
}
