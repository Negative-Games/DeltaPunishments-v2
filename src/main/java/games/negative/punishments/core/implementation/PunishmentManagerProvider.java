package games.negative.punishments.core.implementation;

import games.negative.punishments.DeltaPunishments;
import games.negative.punishments.api.PunishDataManager;
import games.negative.punishments.api.PunishManager;
import games.negative.punishments.api.PunishmentCacheManager;
import games.negative.punishments.api.structure.PersistentPunishment;
import games.negative.punishments.api.structure.Punishment;
import games.negative.punishments.core.Locale;
import games.negative.punishments.core.util.ConfigUtils;
import games.negative.punishments.core.util.Permissions;
import com.google.common.collect.Maps;
import games.negative.framework.message.Message;
import games.negative.framework.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PunishmentManagerProvider implements PunishManager {
    private final DeltaPunishments plugin;
    private final PunishmentCacheManager cacheManager;
    private final PunishDataManager dataManager;
    private final Map<String, Punishment> punishments;

    public PunishmentManagerProvider() {
        this.punishments = Maps.newHashMap();
        this.plugin = DeltaPunishments.getInstance();

        this.cacheManager = plugin.getCacheManager();
        this.dataManager = new PunishmentDataManagerProvider();

        reload();
    }

    @Override
    public Optional<Punishment> findPunishment(@NotNull String input) {
        return punishments.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(input) ||
                        entry.getValue().getReason().equalsIgnoreCase(input))
                .map(Map.Entry::getValue).findFirst();
    }

    @Override
    public void executePunishment(CommandSender staff, OfflinePlayer offender, Punishment punishment, boolean silent, boolean skipToFinal) {
        String perm = Permissions.EXECUTE_PUNISHMENT + punishment.getId();
        if (!staff.hasPermission(perm)) {
            Locale.CANNOT_ISSUE_PUNISHMENT.send(staff);
            return;
        }

        if (staff instanceof Player)
            ((Player) staff).closeInventory();

        UUID uuid = offender.getUniqueId();
        Collection<PersistentPunishment> punishments = cacheManager.getPunishments(uuid, null);
        int amountOfPunishments = (int) punishments.stream().filter(entry -> entry.getReason().equalsIgnoreCase(punishment.getReason()))
                .filter(entry -> {
                    long date = entry.getTime();
                    long forgivenessOffset = punishment.getForgivenessOffset();
                    if (forgivenessOffset == -1)
                        return true;

                    long forgivenessDate = (date + forgivenessOffset);
                    return System.currentTimeMillis() < forgivenessDate;
                }).count();

        List<String> stackables = punishment.getStackables();
        String rawOffense;
        try {
            rawOffense = stackables.get(amountOfPunishments);
        } catch (Exception exception) {
            rawOffense = stackables.get(stackables.size() - 1);
        }
        if (skipToFinal) // Will skip to final offense if this is true
            rawOffense = stackables.get(stackables.size() - 1);

        String[] commandLines = rawOffense.split(";");
        String punishInfo = commandLines[0];
        String[] offense = punishInfo.split(":");
        String type = offense[0];

        String length = null;
        try {
            length = offense[1];
        } catch (Exception ignored) {
        }

        final String offenderName = offender.getName();
        boolean isPerm = (length != null && length.equalsIgnoreCase("perm"));
        String cmd = getCorrectType(type, isPerm) + " " + offenderName
                + ((length == null || length.isEmpty()) ? "" : !isPerm ? " " + length : "")
                + " " + punishment.getReason() + (silent ? " -s" : "");

        sendSuccessMessage(staff, offender, type, length, punishment.getReason());

        new PunishmentExecutorTask(staff, cmd, commandLines, offenderName).runTask(plugin);
    }

    @RequiredArgsConstructor
    private static class PunishmentExecutorTask extends BukkitRunnable {

        private final CommandSender staff;
        private final String cmd;
        private final String[] commandLines;
        private final String offenderName;


        @Override
        public void run() {
            ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
            if (staff instanceof Player)
                ((Player) staff).performCommand(cmd);
            else
                Bukkit.dispatchCommand(consoleSender, cmd);

            // Removes first one
            String[] newCommandLines = Arrays.copyOfRange(commandLines, 1, commandLines.length);
            for (String command : newCommandLines) {
                command = command.replaceAll("%player%", offenderName);
                Bukkit.dispatchCommand(consoleSender, command);
            }
        }
    }

    @Override
    public Map<String, Punishment> getPunishmentMap() {
        return punishments;
    }

    private void sendSuccessMessage(CommandSender staff, OfflinePlayer offender, String type, String length, String reason) {
        Message message = getSuccessMessage(type, length);
        if (message == null || message.getMessage().isEmpty())
            return;

        boolean perm = (length != null && length.equalsIgnoreCase("perm"));

        String actualLength = null;
        if (!perm && length != null) {
            Long aLong = TimeUtil.longFromString(length);
            long current = System.currentTimeMillis();
            actualLength = TimeUtil.format(current + aLong, current);
        }

        message.replace("%offender%", offender.getName())
                .replace("%duration%", (length == null ? "" : (!perm ? actualLength : "perm")))
                .replace("%reason%", reason).send(staff);
    }

    private Message getSuccessMessage(String type, String length) {
        boolean perm = (length != null && length.equalsIgnoreCase("perm"));

        if (equals(type, "ban") && perm)
            return Locale.SUCCESS_BAN.getMessage();
        if (equals(type, "ban") && !perm)
            return Locale.SUCCESS_TEMP_BAN.getMessage();
        if (equals(type, "ipban") && perm)
            return Locale.SUCCESS_IPBAN.getMessage();
        if (equals(type, "ipban") && !perm)
            return Locale.SUCCESS_TEMP_IPBAN.getMessage();
        if (equals(type, "mute") && perm)
            return Locale.SUCCESS_MUTE.getMessage();
        if (equals(type, "mute") && !perm)
            return Locale.SUCCESS_TEMP_MUTE.getMessage();
        if (equals(type, "ipmute") && perm)
            return Locale.SUCCESS_IPMUTE.getMessage();
        if (equals(type, "ipmute") && !perm)
            return Locale.SUCCESS_TEMP_IPMUTE.getMessage();
        if (equals(type, "kick"))
            return Locale.SUCCESS_KICK.getMessage();
        if (equals(type, "warn"))
            return Locale.SUCCESS_WARN.getMessage();

        return null;
    }

    private String getCorrectType(String type, boolean perm) {
        switch (type.toLowerCase()) {
            case "ban": {
                if (!perm)
                    return "tempban";
                else
                    return "ban";
            }

            case "mute": {
                if (!perm)
                    return "tempmute";
                else
                    return "mute";
            }

            case "ipban": {
                if (!perm)
                    return "tempipban";
                else
                    return "ipban";
            }

            case "ipmute": {
                if (!perm)
                    return "tempipmute";
                else
                    return "ipmute";
            }

        }

        return type;
    }

    private boolean equals(String s1, String s2) {
        return s1.equalsIgnoreCase(s2);
    }

    @Override
    public void reload() {
        // Gets the file named "punishments"
        FileConfiguration config = new ConfigUtils("punishments").getConfig();
        // Loops through all the punishment entries
        ConfigurationSection section = config.getConfigurationSection("");

        // For-Each the section and create the punishment objects
        punishments.clear();
        for (String id : section.getKeys(false)) {
            String identifier = id.toLowerCase();
            String reason = config.getString(id + ".reason");
            String forgiveness = config.getString(id + ".forgiveness-offset", "none");
            long forgivenessOffset = -1;
            if (!forgiveness.equalsIgnoreCase("none"))
                forgivenessOffset = TimeUtil.longFromString(forgiveness);

            Punishment punishment = new Punishment(identifier, reason, forgivenessOffset);
            punishment.setStackables(config.getStringList(id + ".stackables"));

            punishments.put(identifier, punishment);
        }
    }

    @Override
    public @Nullable PunishDataManager getDataManager() {
        return dataManager;
    }

}
