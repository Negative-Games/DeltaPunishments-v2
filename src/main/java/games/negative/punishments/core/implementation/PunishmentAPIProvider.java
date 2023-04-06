package games.negative.punishments.core.implementation;

import games.negative.punishments.api.PunishAPI;
import games.negative.punishments.api.PunishManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PunishmentAPIProvider extends PunishAPI {

    private final PunishManager punishManager;
    public PunishmentAPIProvider(JavaPlugin plugin, boolean notesEnabled) {
        setInstance(this);
        punishManager = new PunishmentManagerProvider();
        new GUIManagerProvider();

    }

    @Override
    public PunishManager getPunishManager() {
        return punishManager;
    }

}
