package net.gazeplay.games.beraV2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BeraV2GameVariant {
    WORD_COMPREHENSION_V2("Word Comprehension"),
    SENTENCE_COMPREHENSION_V2("Sentence Comprehension");

    @Getter
    private final String label;
}
