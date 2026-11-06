package net.gazeplay.ui.scenes.configuration;

import mslinks.ShellLink;
import net.gazeplay.GazePlayLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Windows shortcut that starts a game (and a variant) directly, created from the settings. It starts javaw, without a
 * terminal, like the shortcuts of the installer; it used to start a launch script, whose name had changed.
 */
public final class GameShortcut {

    private GameShortcut() {
    }

    /**
     * @param user the profile, empty for the default one
     * @return the shortcut created in the folder, which is created if missing
     */
    public static File create(File folder, String label, String user, String game, IGameVariant variant)
        throws IOException {
        Files.createDirectories(folder.toPath());
        File code = codeLocation();
        File bin = new File(code.getParentFile().getParentFile(), "bin");
        File icon = new File(bin, "gazeplayicon.ico");

        ShellLink link = ShellLink.createLink(
                System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw.exe")
            .setCMDArgs(arguments(classpath(code, System.getProperty("java.class.path")),
                GazePlayLauncher.jvmOptions(), user, game, variant));
        link.setWorkingDir(bin.isDirectory() ? bin.getAbsolutePath() : new File("").getAbsolutePath());
        // The icon is installed by the installer only, not with the .zip distribution
        if (icon.isFile()) {
            link.setIconLocation(icon.getAbsolutePath());
            link.getHeader().setIconIndex(0);
        }

        File shortcut = new File(folder, fileName(label));
        link.saveTo(shortcut.getAbsolutePath());
        return shortcut;
    }

    /**
     * @return the jar of GazePlay in a distribution, the folder of the classes when running from the sources
     */
    private static File codeLocation() throws IOException {
        try {
            return new File(GazePlayLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IOException("Cannot find the location of GazePlay", e);
        }
    }

    /**
     * @return the classpath of the shortcut: all the jars of the folder of GazePlay. The class path of this JVM is the
     *   list of these jars in full, too long for a shortcut; it is only used when running from the sources
     */
    static String classpath(File code, String currentClasspath) {
        return code.getName().endsWith(".jar") ? new File(code.getParentFile(), "*").getPath() : currentClasspath;
    }

    /**
     * @return the arguments of javaw: those of the JVM of a game started from the menu, in the quotes of a command line
     */
    static String arguments(String classpath, List<String> jvmOptions, String user, String game, IGameVariant variant) {
        List<String> arguments = new ArrayList<>(jvmOptions);
        arguments.addAll(List.of("-cp", quoted(classpath), GazePlayLauncher.class.getName()));
        arguments.addAll(user == null || user.isEmpty() ? List.of("--default-user") : List.of("--user", quoted(user)));
        arguments.addAll(List.of("--game", quoted(game)));
        if (variant != null) {
            arguments.addAll(List.of("--variant", quoted(variant.toString())));
        }
        return String.join(" ", arguments);
    }

    private static String quoted(String argument) {
        return '"' + argument + '"';
    }

    /**
     * @return the name of the shortcut, without the characters Windows refuses in a file name (a variant can have some)
     */
    static String fileName(String label) {
        return label.replaceAll("[\\\\/:*?\"<>|]", "-") + ".lnk";
    }
}
