package games.negative.punishments.commands;

import games.negative.punishments.DeltaPunishments;
import games.negative.punishments.api.PunishAPI;
import games.negative.punishments.api.PunishManager;
import games.negative.punishments.api.managers.GUIManager;
import games.negative.punishments.core.Locale;
import games.negative.punishments.core.util.Permissions;
import games.negative.framework.command.Command;
import games.negative.framework.command.annotation.CommandInfo;
import games.negative.framework.util.FileLoader;
import org.bukkit.command.CommandSender;

@CommandInfo(name = "punishreload", aliases = {"preload"})
public class CommandPunishReload extends Command {

    private final GUIManager guiManager;
    private final PunishManager punishManager;

    public CommandPunishReload() {
        punishManager = PunishAPI.getInstance().getPunishManager();
        guiManager = GUIManager.getInstance();

        setPermission(Permissions.RELOAD);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        DeltaPunishments plugin = DeltaPunishments.getInstance();
        FileLoader.loadFile(plugin, "messages.yml");
        FileLoader.loadFile(plugin, "permissions.yml");
        FileLoader.loadFile(plugin, "punishments.yml");
        FileLoader.loadFile(plugin, "guis.yml");

        Locale.init(plugin);
        Permissions.loadPermissions();
        punishManager.reload();
        guiManager.reload();

        Locale.RELOAD.send(sender);
    }
}
