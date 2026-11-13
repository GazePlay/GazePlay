package net.gazeplay.games.colorblend;

import lombok.Getter;
import net.gazeplay.games.colorblend.PaintMix.Pot;
import net.gazeplay.games.colorblend.PaintMix.Target;

import java.util.List;

/**
 * The level of "Mélanger les couleurs" (maintainer's choices of 13/11/2026): its pots and the colors to make. Easy: red,
 * yellow and blue, two by two; normal: with white and black; hard: three pots. The free mode has a single value, the
 * five pots.
 */
@Getter
public enum ColorBlendLevel {
    EASY("ColorBlendEasy", List.of(Pot.RED, Pot.YELLOW, Pot.BLUE), List.of(Target.ORANGE, Target.GREEN, Target.PURPLE)),
    NORMAL("ColorBlendNormal", List.of(Pot.values()), List.of(Target.PINK, Target.SKY_BLUE, Target.PALE_YELLOW,
        Target.GREY, Target.BURGUNDY, Target.NAVY)),
    HARD("ColorBlendHard", List.of(Pot.values()), List.of(Target.BROWN, Target.LIGHT_GREEN, Target.MAUVE, Target.PEACH,
        Target.FIR_GREEN, Target.AUBERGINE)),
    FREE("ColorBlendAllPots", List.of(Pot.values()), List.of());

    /**
     * Key of translation, and value of the variants (the buttons).
     */
    private final String key;

    private final List<Pot> pots;

    private final List<Target> targets;

    ColorBlendLevel(final String key, final List<Pot> pots, final List<Target> targets) {
        this.key = key;
        this.pots = pots;
        this.targets = targets;
    }

    /**
     * @return the level of a key, easy for an unknown one
     */
    public static ColorBlendLevel of(final String key) {
        for (final ColorBlendLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return EASY;
    }
}
