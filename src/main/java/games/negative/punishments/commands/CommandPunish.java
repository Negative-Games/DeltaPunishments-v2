package games.negative.punishments.commands;

import games.negative.framework.commands.Command;
import games.negative.framework.commands.Context;
import games.negative.punishments.DeltaPunishments;
import games.negative.punishments.api.PunishAPI;
import games.negative.punishments.api.PunishManager;
import games.negative.punishments.api.PunishmentCacheManager;
import games.negative.punishments.api.managers.GUIManager;
import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.Punishment;
import games.negative.punishments.api.structure.config.ConfigurableGUI;
import games.negative.punishments.core.Locale;
import games.negative.punishments.menus.PunishMenu;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class CommandPunish implements Command {
    private final GUIManager guiManager;
    private final PunishManager punishManager;
    private final DeltaPunishments plugin;

    public CommandPunish(DeltaPunishments plugin) {
        this.plugin = plugin;
        punishManager = PunishAPI.getInstance().getPunishManager();
        guiManager = GUIManager.getInstance();
    }

    private boolean findWord(String input, String[] args) {
        return Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase(input));
    }

    @Override
    public void execute(@NotNull Context context) {
        String[] args = context.getArgs();

        OfflinePlayer offender = Bukkit.getOfflinePlayer(args[0]);
        if (args.length == 1) {
            PunishmentCacheManager cacheManager = plugin.getCacheManager();
            Collection<PersistentPunishment> punishments = cacheManager.getPunishments(offender.getUniqueId(), null);

            Optional<ConfigurableGUI> gui = guiManager.getGUI("punish");
            if (!gui.isPresent()) {
                System.out.println("[DeltaPunishments] Could not find GUI named `punish`");
                return;
            }

            Player player = context.getPlayer();
            assert player != null;

            new PunishMenuRunnable(gui.get(), player, offender, punishments).runTask(plugin);
            return;
        }

        Optional<Map.Entry<String, Punishment>> first = punishManager.getPunishmentMap().entrySet()
                .stream().filter(stringPunishmentEntry -> stringPunishmentEntry.getKey().equalsIgnoreCase(args[1]))
                .findFirst();

        if (!first.isPresent()) {
            Locale.INVALID_PUNISHMENT.replace("%input%", args[1]).send(context.getCommandSender());
            return;
        }

        boolean pub = findWord("-p", args);
        boolean fin = findWord("-f", args);
        punishManager.executePunishment(context.getCommandSender(), offender, first.get().getValue(), !pub, fin);
    }

    @RequiredArgsConstructor
    private static class PunishMenuRunnable extends BukkitRunnable {

        private final ConfigurableGUI gui;
        private final Player staff;
        private final OfflinePlayer offender;
        private final Collection<PersistentPunishment> punishments;

        @Override
        public void run() {
            new PunishMenu(staff, offender, gui, punishments).open(staff);
        }
    }
}
