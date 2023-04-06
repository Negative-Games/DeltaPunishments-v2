package games.negative.punishments.core.util;

import games.negative.punishments.DeltaPunishments;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigUtils {

    private final String name;

    public ConfigUtils(String fileName) {
        this.name = fileName;
    }

    public FileConfiguration getConfig() {
        File file = new File(DeltaPunishments.getInstance().getDataFolder(), this.name + ".yml");
        return YamlConfiguration.loadConfiguration(file);
    }
}