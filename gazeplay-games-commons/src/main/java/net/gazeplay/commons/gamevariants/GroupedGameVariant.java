package net.gazeplay.commons.gamevariants;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.gazeplay.commons.ui.Translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A variant in two groups, chosen in the window of the variants by switches at the top (the group) and at the bottom
 * (the sub-group), the buttons showing the values of the chosen groups (30/10/2026, first for Memory: what the cards
 * show at the top, hidden or visible cards at the bottom, the sizes of the grid as buttons).
 * <p>
 * A game with more choices gives more switches, under those of the sub-groups (4/11/2026, Ninja).
 * <p>
 * Persistent form: {@code GroupedGameVariant:<group>:<sub-group>:<value>:<folder of the images of the user or ->}, then
 * {@code :<switch>} for each other switch; the group, the sub-group and the other switches are keys of translations, the
 * value a size ("2x3", shown "2 × 3") or a key of translation.
 */
@Getter
@EqualsAndHashCode
public final class GroupedGameVariant implements IGameVariant {

    public static final String TYPE = "GroupedGameVariant";

    private static final String NO_FOLDER = "-";

    private final String group;

    private final String subGroup;

    private final String value;

    /**
     * The folder of the images of the user this variant plays with (see {@link UserImagesVariant}), null if none.
     */
    private final String userImagesFolder;

    /**
     * The values of the other switches, none for most games.
     */
    private final List<String> extras;

    public GroupedGameVariant(final String group, final String subGroup, final String value,
                              final String userImagesFolder, final String... extras) {
        this.group = group;
        this.subGroup = subGroup;
        this.value = value;
        this.userImagesFolder = userImagesFolder;
        this.extras = List.of(extras);
    }

    /**
     * @return the values of all the switches, in their order in the window: the group, the sub-group, then the others
     */
    public List<String> getSwitches() {
        final List<String> switches = new ArrayList<>(List.of(group, subGroup));
        switches.addAll(extras);
        return switches;
    }

    /**
     * @return the number of lines of a size ("2x3": 2), 0 for another value
     */
    public int getLines() {
        return isSize() ? Integer.parseInt(value.substring(0, value.indexOf('x'))) : 0;
    }

    /**
     * @return the number of columns of a size ("2x3": 3), 0 for another value
     */
    public int getColumns() {
        return isSize() ? Integer.parseInt(value.substring(value.indexOf('x') + 1)) : 0;
    }

    /**
     * @return whether the value is a size of grid ("2x3")
     */
    public boolean isSize() {
        return value.matches("\\d+x\\d+");
    }

    @Override
    public String getLabel(final Translator translator) {
        return isSize() ? value.replace("x", " × ") : translator.translate(value);
    }

    @Override
    public String toString() {
        final StringBuilder form = new StringBuilder(TYPE + ":" + group + ":" + subGroup + ":" + value + ":"
            + (userImagesFolder == null ? NO_FOLDER : userImagesFolder));
        extras.forEach(extra -> form.append(':').append(extra));
        return form.toString();
    }

    /**
     * @return the variant of its persistent form (without the type), null if it is not one
     */
    static GroupedGameVariant read(final String[] parts) {
        if (parts.length < 5) {
            return null;
        }
        return new GroupedGameVariant(parts[1], parts[2], parts[3], NO_FOLDER.equals(parts[4]) ? null : parts[4],
            Arrays.copyOfRange(parts, 5, parts.length));
    }
}
