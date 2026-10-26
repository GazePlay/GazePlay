package net.gazeplay.cli;

import lombok.Data;
import picocli.CommandLine;

/**
 * Variant of the game started with {@code --game}: {@code --variant <persistent form of the variant>}, e.g.
 * "IntGameVariant:4" (see {@code IGameVariant#toString()}).
 */
@Data
public class VariantSelectionOptions {

    @CommandLine.Option(names = {"-v", "--variant"})
    private String gameVariant;
}
