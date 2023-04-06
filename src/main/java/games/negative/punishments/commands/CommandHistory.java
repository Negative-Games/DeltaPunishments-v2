package games.negative.punishments.commands;

import games.negative.punishments.DeltaPunishments;
import games.negative.punishments.api.managers.GUIManager;
import games.negative.punishments.api.structure.config.ConfigurableGUI;
import games.negative.punishments.core.structure.PersistentOfflinePlayer;
import games.negative.punishments.core.util.Permissions;
import games.negative.punishments.menus.history.HistoryMenu;
import games.negative.framework.command.Command;
import games.negative.framework.command.annotation.CommandInfo;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@CommandInfo(name = "history", aliases = {"hist"}, args = "player", playerOnly = true)
public class CommandHistory extends Command {
    private final GUIManager guiManager;
    private FloodgateApi bedrockAPI;
    private final DeltaPunishments plugin;

    public CommandHistory() {
        this.plugin = DeltaPunishments.getInstance();
        setPermissionNode(Permissions.HISTORY);

        guiManager = GUIManager.getInstance();

        try {
            bedrockAPI = FloodgateApi.getInstance();
        } catch (NoClassDefFoundError ignored) {
            Bukkit.getLogger().log(Level.INFO, "Could not find GreyserMC plugin, disabling dependency.");
        }

    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (bedrockAPI == null) {
            PersistentOfflinePlayer offender = PersistentOfflinePlayer.ofOfflinePlayer(Bukkit.getOfflinePlayer(args[0]));
            new HistoryMenuRunnable(player, offender).runTask(plugin);
        } else {
            CompletableFuture<Long> xuidFor = bedrockAPI.getXuidFor(args[0]);
            xuidFor.whenComplete((id, throwable) -> {
                if (id == null) {
                    // Assuming regular Java player
                    PersistentOfflinePlayer offender = PersistentOfflinePlayer.ofOfflinePlayer(Bukkit.getOfflinePlayer(args[0]));
                    new HistoryMenuRunnable(player, offender).runTask(plugin);
                    return;
                }
                CompletableFuture<String> gamertagFor = bedrockAPI.getGamertagFor(id);
                gamertagFor.whenComplete((tag, throwable1) -> bedrockAPI.getUuidFor(tag)
                        .whenComplete((uuid, throwable2) -> {

                            PersistentOfflinePlayer offender = PersistentOfflinePlayer.ofFloodgatePlayer(uuid, tag);
                            new HistoryMenuRunnable(player, offender).runTask(plugin);

                        }));
            });
        }
    }


    @RequiredArgsConstructor
    private class HistoryMenuRunnable extends BukkitRunnable {
        private final Player staff;
        private final PersistentOfflinePlayer offender;

        @Override
        public void run() {
            // Load History menu
            Optional<ConfigurableGUI> gui = guiManager.getGUI("history");
            if (!gui.isPresent()) {
                System.out.println("[DeltaPunishments] Could not find GUI named `history`");
                return;
            }

            new HistoryMenu(staff, offender, gui.get()).open(staff);
        }
    }


}
