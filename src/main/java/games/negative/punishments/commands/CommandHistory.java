package games.negative.punishments.commands;

import games.negative.framework.commands.Command;
import games.negative.framework.commands.Context;
import games.negative.punishments.DeltaPunishments;
import games.negative.punishments.api.managers.GUIManager;
import games.negative.punishments.api.structure.config.ConfigurableGUI;
import games.negative.punishments.core.structure.PersistentOfflinePlayer;
import games.negative.punishments.menus.history.HistoryMenu;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class CommandHistory implements Command {
    private final GUIManager guiManager;
    private FloodgateApi bedrockAPI;
    private final DeltaPunishments plugin;

    public CommandHistory(DeltaPunishments plugin) {
        this.plugin = plugin;
        guiManager = GUIManager.getInstance();

        try {
            bedrockAPI = FloodgateApi.getInstance();
        } catch (NoClassDefFoundError ignored) {
            Bukkit.getLogger().log(Level.INFO, "Could not find GreyserMC plugin, disabling dependency.");
        }

    }

    @Override
    public void execute(@NotNull Context context) {
        Player player = context.getPlayer();
        assert player != null;

        String[] args = context.getArgs();
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
