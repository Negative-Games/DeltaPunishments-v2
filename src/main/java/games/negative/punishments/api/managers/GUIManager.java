package games.negative.punishments.api.managers;

import games.negative.punishments.api.structure.config.ConfigurableGUI;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Optional;

public abstract class GUIManager {

    @Getter
    @Setter
    private static GUIManager instance;

    public abstract ArrayList<ConfigurableGUI> getGUIs();

    public abstract Optional<ConfigurableGUI> getGUI(String input);

    public abstract void reload();

}
