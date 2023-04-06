package games.negative.punishments.commands;

import games.negative.punishments.DeltaPunishments;
import games.negative.punishments.api.PunishAPI;
import games.negative.punishments.api.PunishManager;
import games.negative.punishments.api.PunishmentCacheManager;
import games.negative.punishments.api.managers.GUIManager;
import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.Punishment;
import games.negative.punishments.api.structure.config.ConfigurableGUI;
import games.negative.punishments.core.Locale;
import games.negative.punishments.core.util.Permissions;
import games.negative.punishments.menus.PunishMenu;
import games.negative.framework.command.Command;
import games.negative.framework.command.annotation.CommandInfo;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

@CommandInfo(name = "punish", aliases = "pu", args = "player")
public class CommandPunish extends Command {
    private final GUIManager guiManager;
    private final PunishManager punishManager;
    private final DeltaPunishments plugin;

    public CommandPunish() {
        setPermissionNode(Permissions.PUNISH);

        guiManager = GUIManager.getInstance();
        punishManager = PunishAPI.getInstance().getPunishManager();
        plugin = DeltaPunishments.getInstance();

        setTabComplete((sender, args) -> {
            if (args.length == 1) {
                String lastWord = args[args.length - 1];
                Player senderPlayer = sender instanceof Player ? (Player) sender : null;
                ArrayList<String> matchedPlayers = new ArrayList<>();
                sender.getServer().getOnlinePlayers().stream()
                        .filter(player -> senderPlayer == null || senderPlayer.canSee(player) && StringUtil.startsWithIgnoreCase(player.getName(), lastWord))
                        .forEach(player -> matchedPlayers.add(player.getName()));

                matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
                return matchedPlayers;
            }
            if (args.length == 2) {
                List<String> punishments = new ArrayList<>();
                punishManager.getPunishmentMap().forEach((key, value) -> punishments.add(value.getId().toLowerCase()));

                String lastWord = args[args.length - 1];
                return punishments.stream()
                        .filter(s -> sender.hasPermission(Permissions.EXECUTE_PUNISHMENT + s)
                                && StringUtil.startsWithIgnoreCase(s, lastWord))
                        .collect(Collectors.toList());
            }

            return null;
        });
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        OfflinePlayer offender = Bukkit.getOfflinePlayer(args[0]);
        if (args.length == 1) {
            PunishmentCacheManager cacheManager = plugin.getCacheManager();
            Collection<PersistentPunishment> punishments = cacheManager.getPunishments(offender.getUniqueId(), null);

            Optional<ConfigurableGUI> gui = guiManager.getGUI("punish");
            if (!gui.isPresent()) {
                System.out.println("[DeltaPunishments] Could not find GUI named `punish`");
                return;
            }

            new PunishMenuRunnable(gui.get(), (Player) sender, offender, punishments).runTask(plugin);
            return;
        }

        Optional<Map.Entry<String, Punishment>> first = punishManager.getPunishmentMap().entrySet()
                .stream().filter(stringPunishmentEntry -> stringPunishmentEntry.getKey().equalsIgnoreCase(args[1]))
                .findFirst();

        if (!first.isPresent()) {
            Locale.INVALID_PUNISHMENT.replace("%input%", args[1]).send(sender);
            return;
        }

        boolean pub = findWord("-p", args);
        boolean fin = findWord("-f", args);
        punishManager.executePunishment(sender, offender, first.get().getValue(), !pub, fin);
    }

    private boolean findWord(String input, String[] args) {
        return Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase(input));
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
