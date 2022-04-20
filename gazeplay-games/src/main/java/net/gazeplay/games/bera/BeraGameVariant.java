package net.gazeplay.games.bera;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BeraGameVariant {
    WORD_COMPREHENSION("Word Comprehension"),
    SENTENCE_COMPREHENSION("Sentence Comprehension");

    @Getter
    private final String label;
}
