package net.gazeplay.ui.scenes.configuration;

import mslinks.ShellLink;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameShortcutTest {

    private static final String LAUNCHER = "net.gazeplay.GazePlayLauncher";

    /**
     * A jar of the distribution: all the jars of its folder, the class path of the JVM being too long for a shortcut.
     */
    @Test
    void shouldUseAllTheJarsOfTheDistribution() {
        File jar = new File(new File("GazePlay Learning", "lib"), "gazeplay-1.11.10.jar");
        assertEquals(new File(jar.getParentFile(), "*").getPath(), GameShortcut.classpath(jar, "current"));
        assertEquals("current", GameShortcut.classpath(new File("build", "classes"), "current"));
    }

    /**
     * A profile with a space stays one argument: it was not in quotes.
     */
    @Test
    void shouldQuoteTheArguments() {
        assertEquals("--option -cp \"lib dir\\*\" " + LAUNCHER + " --user \"Jean Marc\" --game \"Ninja\"",
            GameShortcut.arguments("lib dir\\*", List.of("--option"), "Jean Marc", "Ninja", null));
    }

    @Test
    void shouldStartTheDefaultProfileAndTheVariant() {
        assertEquals("-cp \"cp\" " + LAUNCHER + " --default-user --game \"Ninja\" --variant \""
                + new DimensionGameVariant(2, 3) + "\"",
            GameShortcut.arguments("cp", List.of(), "", "Ninja", new DimensionGameVariant(2, 3)));
    }

    @Test
    void shouldRemoveTheCharactersRefusedInAFileName() {
        assertEquals("Ninja - 2-3 - a-b-c.lnk", GameShortcut.fileName("Ninja - 2:3 - a/b?c"));
    }

    /**
     * The folder is created if missing, and the shortcut starts javaw, without a terminal.
     */
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldCreateAShortcutStartingJavaw(@TempDir File folder) throws Exception {
        File shortcut = GameShortcut.create(new File(folder, "raccourcis"), "Ninja", "Jean Marc", "Ninja", null);

        assertTrue(shortcut.isFile());
        ShellLink link = new ShellLink(shortcut.getAbsolutePath());
        assertTrue(link.resolveTarget().toLowerCase().endsWith("javaw.exe"), link.resolveTarget());
        assertTrue(link.getCMDArgs().endsWith("--user \"Jean Marc\" --game \"Ninja\""), link.getCMDArgs());
    }
}
