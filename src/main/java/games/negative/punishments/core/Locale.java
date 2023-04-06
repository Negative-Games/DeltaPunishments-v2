package games.negative.punishments.core;

import games.negative.framework.message.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Getter
public enum Locale {
    REMOVED_FROM_DATABASE("REMOVED_FROM_DATABASE", Collections.singletonList(
            "&c&l(!) &7You have removed this punishment from the database &7(This may take some time to register...)"
    )),

    NEW_DURATION("NEW_DURATION", Arrays.asList(
            "&2&l✦ &a&lNew Duration &2&l✦",
            "&7Please time in a new duration for this punishment!",
            " ",
            "&7Example: &a1h10m",
            "&7Type `&acancel&7` to cancel!"
    )),

    MODIFIED_DURATION("MODIFIED_DURATION", Collections.singletonList(
            "&a&l(!) &7You have modified the duration of the punishment to &e%s%&7!"
    )),

    USE_VALID_NUMBER("USE_VALID_NUMBER", Collections.singletonList(
            "&c&l(!) &7Please use a valid number!"
    )),

    NO_PERM("NO_PERM", Collections.singletonList(
            "&c&l(!) &7You do not have permission to use this!"
    )),

    INVALID_PUNISHMENT("INVALID_PUNISHMENT", Collections.singletonList(
            "&c&l(!) &7Could not find punishment type of &e%input%&7..."
    )),

    SUCCESS_TEMP_BAN("SUCCESS_TEMP_BAN", Collections.singletonList(
            "&a&l(!) &7You have temporarily banned &c%offender% &7for &c%duration% &7for reason &c&l%reason%&7!"
    )),

    SUCCESS_TEMP_MUTE("SUCCESS_TEMP_MUTE", Collections.singletonList(
            "&a&l(!) &7You have temporarily muted &c%offender% &7for &c%duration% &7for reason &c&l%reason%&7!"
    )),

    SUCCESS_BAN("SUCCESS_BAN", Collections.singletonList(
            "&a&l(!) &7You have permanently banned &c%offender% &7for reason &c&l%reason%&7!"
    )),

    SUCCESS_IPBAN("SUCCESS_IPBAN", Collections.singletonList(
            "&a&l(!) &7You have blacklisted &c%offender% &7for reason &c&l%reason%&7!"
    )),

    SUCCESS_TEMP_IPBAN("SUCCESS_TEMP_IPBAN", Collections.singletonList(
            "&a&l(!) &7You have temporarily blacklisted &c%offender% &7for &c%duration% &7for reason &c&l%reason%&7!"
    )),

    SUCCESS_MUTE("SUCCESS_MUTE", Collections.singletonList(
            "&a&l(!) "
    )),

    SUCCESS_IPMUTE("SUCCESS_IPMUTE", Collections.singletonList(
            "&a&l(!) &7You have permanently ip-muted &c%offender% &7for &c%duration% &7for reason &c&l%reason%&7!"
    )),

    SUCCESS_TEMP_IPMUTE("SUCCESS_TEMP_IPMUTE", Collections.singletonList(
            "&a&l(!) &7You have temporarily ip-muted &c%offender% &7for &c%duration% &7for reason &c&l%reason%&7!"
    )),

    SUCCESS_WARN("SUCCESS_WARN", Collections.singletonList(
            "&a&l(!) &7You have warned &c%offender% &7for reason &c&l%reason%&7!"
    )),

    SUCCESS_KICK("SUCCESS_KICK", Collections.singletonList(
            "&a&l(!) &7You have kicked &c%offender% &7for reason &c&l%reason%&7!"
    )),

    RELOAD("RELOAD", Collections.singletonList(
            "&a&l(!) &7Successfully reloaded configuration files!"
    )),

    NOTES_NO_NOTES("NOTES_NO_NOTES", Collections.singletonList(
            "&c&l(!) &7This user has no notes."
    )),

    PLAYER_DOESNT_EXIST("PLAYER_DOESNT_EXIST", Collections.singletonList(
            "&c&l(!) &7The player you are requesting does not exist."
    )),

    CANNOT_ISSUE_PUNISHMENT("CANNOT_ISSUE_PUNISHMENT", Collections.singletonList(
            "&c&l(!) &7You cannot issue this type of punishment!"
    ));
    private final String id;
    private final List<String> defaultMessage;
    private Message message;

    @SneakyThrows
    public static void init(JavaPlugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "messages.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (!configFile.exists()) {
            Arrays.stream(values()).forEach(locale -> {
                String id = locale.getId();
                List<String> defaultMessage = locale.getDefaultMessage();

                config.set(id, defaultMessage);
            });

        } else {
            Arrays.stream(values()).filter(locale -> {
                String id = locale.getId();
                return (config.get(id, null) == null);
            }).forEach(locale -> config.set(locale.getId(), locale.getDefaultMessage()));

        }
        config.save(configFile);

        // Creates the message objects
        Arrays.stream(values()).forEach(locale ->
                locale.message = new Message(config.getStringList(locale.getId())
                        .toArray(new String[0])));
    }

    public void send(CommandSender sender) {
        message.send(sender);
    }

    public void send(Iterable<CommandSender> players) {
        message.send(players);
    }

    public void broadcast() {
        message.broadcast();
    }

    public Message replace(String placeholder, String replacement) {
        return message.replace(placeholder, replacement);
    }
}
