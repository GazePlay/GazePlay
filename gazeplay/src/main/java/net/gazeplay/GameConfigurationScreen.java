package net.gazeplay;

/**
 * This interface describes a configuration screen that a game can create in order to get some settings needed before
 * its launch
 * 
 * @author medard
 */
public interface GameConfigurationScreen {

    /**
     * Display the configuration screen. It is up to the implementing class to get all information it needs in order to
     * display itself.
     */
    public void displayConfigurationScreen();
}
