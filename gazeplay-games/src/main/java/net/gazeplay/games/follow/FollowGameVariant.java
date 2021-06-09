package net.gazeplay.games.follow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FollowGameVariant {
    FKEY("FKEY"),
    FCOIN("FCOIN");

    @Getter
    private final String label;
}
