package net.gazeplay.games.follow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FollowGameVariant {
    FKEYEASY("FKEYEASY"),
    FKEY("FKEY"),
    FCOIN("FCOIN");

    @Getter
    private final String label;
}
