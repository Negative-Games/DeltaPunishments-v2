package games.negative.punishments.core.util;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Make a Locale version of this?
 */
public class Permissions {

    public static String HISTORY;
    public static String PUNISH;
    public static String DATABASE;
    public static String EXECUTE_PUNISHMENT;
    public static String RELOAD;
    public static String NOTES;

    public static void loadPermissions() {
        FileConfiguration c = new ConfigUtils("permissions").getConfig();
        HISTORY = c.getString("HISTORY");
        PUNISH = c.getString("PUNISH");
        DATABASE = c.getString("REMOVE_FROM_DATABASE");
        EXECUTE_PUNISHMENT = c.getString("EXECUTE_PUNISHMENT");
        RELOAD = c.getString("RELOAD", "deltapunishments.reload");
        NOTES = c.getString("NOTES", "deltapunishments.notes");
    }

}
