package games.negative.punishments.commands;

import games.negative.framework.commands.Command;
import games.negative.framework.commands.Context;
import games.negative.framework.util.FileLoader;
import games.negative.punishments.DeltaPunishments;
import games.negative.punishments.api.PunishAPI;
import games.negative.punishments.api.PunishManager;
import games.negative.punishments.api.managers.GUIManager;
import games.negative.punishments.core.Locale;
import games.negative.punishments.core.util.Permissions;
import org.jetbrains.annotations.NotNull;

public class CommandPunishReload implements Command {

    private final GUIManager guiManager;
    private final PunishManager punishManager;

    public CommandPunishReload() {
        punishManager = PunishAPI.getInstance().getPunishManager();
        guiManager = GUIManager.getInstance();
    }

    @Override
    public void execute(@NotNull Context context) {
        DeltaPunishments plugin = DeltaPunishments.getInstance();
        FileLoader.loadFile(plugin, "messages.yml");
        FileLoader.loadFile(plugin, "permissions.yml");
        FileLoader.loadFile(plugin, "punishments.yml");
        FileLoader.loadFile(plugin, "guis.yml");

        Locale.init(plugin);
        Permissions.loadPermissions();
        punishManager.reload();
        guiManager.reload();

        Locale.RELOAD.send(context.getCommandSender());
    }
}
