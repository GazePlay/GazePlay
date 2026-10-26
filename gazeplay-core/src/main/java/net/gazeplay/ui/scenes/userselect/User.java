package net.gazeplay.ui.scenes.userselect;

import javafx.scene.layout.HBox;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The card of a user profile on the user selection page.
 */
@AllArgsConstructor
public class User extends HBox {

    @Getter
    private final String name;

}
