package net.gazeplay.games.cakes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of "La Fabrique à gâteaux" of before the review, kept to read the old shortcuts: easy, normal and hard,
 * read as copying the model at the same level (maintainer's choice of 12/11/2026).
 */
@RequiredArgsConstructor
public enum CakeGameVariant {
    ONELAYER("Easy"),
    THREELAYERS("Normal"),
    THREELAYERSHIDEN("Hard");

    @Getter
    private final String label;
}
