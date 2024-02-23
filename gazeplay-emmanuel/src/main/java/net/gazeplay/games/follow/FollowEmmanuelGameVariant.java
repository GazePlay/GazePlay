package net.gazeplay.games.follow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FollowEmmanuelGameVariant {
    Level1("level1"),
    Level2("level2"),
    Level3("level3");

    @Getter
    private final String label;
}
