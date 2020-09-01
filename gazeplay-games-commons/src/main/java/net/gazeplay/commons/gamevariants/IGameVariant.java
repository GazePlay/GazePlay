package net.gazeplay.commons.gamevariants;

import net.gazeplay.commons.ui.Translator;

/**
 * This is a basically marker interface, but it comes also with a label in order to recognise it by a text label
 */
public interface IGameVariant {

    String getLabel(Translator translator);
}
