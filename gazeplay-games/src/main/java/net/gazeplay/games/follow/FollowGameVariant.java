package net.gazeplay.games.follow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FollowGameVariant {
    FKEYEASY("KeyEasy"),
    FKEY("Key"),
    FCOIN("Coin");

    @Getter
    private final String label;
}
