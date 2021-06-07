package net.gazeplay.games.follow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FollowGameVariant {
    NORMAL("normal"),
    CANARD("canard");

    @Getter
    private final String label;
}
