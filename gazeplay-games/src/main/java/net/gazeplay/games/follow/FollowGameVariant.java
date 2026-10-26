package net.gazeplay.games.follow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "Follow", chosen in the menu.
 */
@RequiredArgsConstructor
public enum FollowGameVariant {
    FKEYEASY("KeyEasy"),
    FKEY("Key"),
    FCOIN("Coin");

    @Getter
    private final String label;
}
