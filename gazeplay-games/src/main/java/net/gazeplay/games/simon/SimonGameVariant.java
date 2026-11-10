package net.gazeplay.games.simon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The variants of "Simon" before its review of 10/11/2026, kept so that the old variants (saved statistics, shortcuts)
 * are still read: easy (10 notes) is played in classic mode to 8 notes, normal (22 notes) to 12 notes, hard (33 notes)
 * without end, "Simon copies you" in free mode to 12 notes, multiplayer with two players without end.
 */
@Getter
@RequiredArgsConstructor
public enum SimonGameVariant {
    EASY_CLASSIC("Easy", SimonMode.CLASSIC, SimonEnd.NOTES_8),
    NORMAL_CLASSIC("Normal", SimonMode.CLASSIC, SimonEnd.NOTES_12),
    HARD_CLASSIC("Hard", SimonMode.CLASSIC, SimonEnd.ENDLESS),
    MODE2("SimonFree", SimonMode.FREE, SimonEnd.NOTES_12),
    MODE3("SimonDuo", SimonMode.DUO, SimonEnd.ENDLESS);

    private final String label;
    private final SimonMode mode;
    private final SimonEnd end;
}
