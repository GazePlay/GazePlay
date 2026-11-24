package net.gazeplay;

import java.util.EnumSet;
import java.util.Set;

/**
 * The settings of the menu of a game screen (Esc) that can act on a game. A game says which ones act on it, for its
 * variant ({@link GameLifeCycle#menuSettings()}): the others are greyed in the menu (maintainer's request of
 * 17/11/2026: an option that does nothing must not look as if it did something).
 */
public enum MenuSetting {

    /** How long the gaze must stay on an element to select it. */
    FIXATION_LENGTH,

    /** The speed of what moves (animation speed ratio). */
    ANIMATION_SPEED,

    /** The size of the elements (only Ninja reads it: the other games size their elements themselves). */
    ELEMENT_SIZE,

    /**
     * The thickness of the ring of a fixation: the width of its line, never its diameter, in every ring of the games
     * (round or around a card, {@code RingThickness}; 18/11/2026).
     */
    RING_THICKNESS,

    /** The color of the ring of a fixation (every ring of the games, the main color of the menus by default). */
    RING_COLOR,

    /**
     * When the gaze leaves a moving target during its fixation: the fixation starts again from zero, or keeps its
     * progress (20/11/2026). Only for the fixations of moving targets: Ninja, Bubbles, Cream pie with moving targets,
     * Divisor.
     */
    MOVING_FIXATION,

    /**
     * The buttons of the game, light or dark whatever the theme (24/11/2026): only for the games with buttons
     * ({@code GameStyle.BUTTON_STYLE} or {@code -gp-button}), applied at once.
     */
    GAME_BUTTONS;

    /**
     * @return the settings given, in a set that can be changed
     */
    public static Set<MenuSetting> of(final MenuSetting... settings) {
        final Set<MenuSetting> set = EnumSet.noneOf(MenuSetting.class);
        set.addAll(java.util.List.of(settings));
        return set;
    }

    /**
     * @return a fixation with the shared ring: its length, the thickness and the color of the ring, and the settings
     * given
     */
    public static Set<MenuSetting> fixationWithSharedRing(final MenuSetting... others) {
        final Set<MenuSetting> set = of(others);
        set.addAll(java.util.List.of(FIXATION_LENGTH, RING_THICKNESS, RING_COLOR));
        return set;
    }

    /**
     * @return a fixation with a ring drawn by the game: the same settings as {@link #fixationWithSharedRing} since
     * 18/11/2026, the thickness acting on every ring (the size used to act on the shared ring only)
     */
    public static Set<MenuSetting> fixationWithOwnRing(final MenuSetting... others) {
        return fixationWithSharedRing(others);
    }
}
