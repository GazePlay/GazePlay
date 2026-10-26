package net.gazeplay;

/**
 * Declares a game. Each implementation (class named ...SpecSource) is listed in games-catalog.txt by the build, and the
 * menu creates one per line to find the games.
 */
public interface GameSpecSource {
    GameSpec getGameSpec();
}
